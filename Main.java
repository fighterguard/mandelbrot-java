package mandeljava;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;

class Main {

  //Variable declaration
  static final int X = 800, Y = 800;
  static final int inputRowHeight = 30;
  static BufferedImage I = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);
  static int totalIterations = 400;
  static double centerX = 0;//-0.743643887037151;//-0.1529051455399985; //-0.5803827923341388d;
  static double centerY = 0;//0.131825904205330;//1.039718512720433; //-0.652903457310135d;
  static double radius = 2;//0.000000000051299 //7.120538316886522e-10;
  static int startingIterationsValue = 400;
  static int finalIterationsValue = 800;
  static double startingRadiusValue = 2.0d;
  static double finalRadiusValue = 0.01d;
  static int stepsValue = 20;
  static double zoomFactor;
  static double detailFactor;
  static final int totalColors = 800;
  static int[] colorTable = new int[totalColors];
  static boolean saveEachValue = false;
  static boolean stopAnimationValue = false;
  static boolean animatingValue = false;
  static Ranges currentRanges;
  static Factors currentFactors;
  static MainCanvas canvas = new MainCanvas();
  static int fileCounter = 1;
  static ArrayBlockingQueue<ProgressMessage> threadCompletionQueue = new ArrayBlockingQueue<ProgressMessage>(20);
  static boolean[] threadsFinished = {false, false, false, false};
  static final int[][] startingColors = {
    {255, 255, 255}, // white
    {0, 0, 0}, // black
    {255, 255, 0}, // yellow
    {255, 127, 0}, // orange
    {127, 63, 0}, // dark orange
    {0, 0, 0}, // black
    {0, 0, 127}, // dark blue
    {0, 0, 255}, // blue
    {0, 255, 255}, // cyan
    {255, 255, 255}, // white
  };
  static BufferedImage[] quadrantImages = {
    new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB),
    new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB),
    new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB),
    new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB)
  };
  private static final Dimension[] quadrants = {
    new Dimension(
      400,
      0
    ),
    new Dimension(
      0,
      0
    ),
    new Dimension(
      0,
      400
    ),
    new Dimension(
      400,
      400
    )
  };

  //Components
  private static final LabeledInput centerXInput = new LabeledInput("Real Part");
  private static final LabeledInput centerYInput = new LabeledInput("Imaginary Part");
  private static final LabeledInput radiusInput = new LabeledInput("Radius");
  private static final LabeledInput iterationsInput = new LabeledInput("Iterations");
  private static final LabeledInput filenamePrefix = new LabeledInput("File name");
  private static final LabeledInput startingRadius = new LabeledInput("Starting Radius");
  private static final LabeledInput finalRadius = new LabeledInput("Final Radius");
  private static final LabeledInput startingIterations = new LabeledInput("Starting Iterations");
  private static final LabeledInput finalIterations = new LabeledInput("Final Iterations");
  private static final LabeledInput steps = new LabeledInput("Steps");
  private static final Checkbox saveEach = new Checkbox("Save each frame as JPEG");
  private static final Button updateButton = new Button("Update");
  private static final Button saveAsImageButton = new Button("Save as JPEG");
  private static final Button startAnimation = new Button("Start");
  private static final Button stopAnimation = new Button("Stop");
  private static final JProgressBar progressBar = new JProgressBar(0, 1600);
  private static InputMap inputMap = progressBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
  
  //Methods
  static public void main(String[] args) {
    filenamePrefix.setText("mandelbrot-capture");
    centerXInput.setText(Double.toString(centerX));
    centerYInput.setText(Double.toString(centerY));
    radiusInput.setText(Double.toString(radius));
    iterationsInput.setText(Integer.toString(totalIterations));
    updateButton.addActionListener((ActionEvent e) -> {
      if (e.getSource() == updateButton) {
        Main.updateFrame();
      }
    });
    saveAsImageButton.addActionListener((ActionEvent e) -> {
      if (e.getSource() == saveAsImageButton) {
        saveAsImage();
      }
    });
    startAnimation.addActionListener((ActionEvent e) -> {
       stopAnimationValue = false;
       animatingValue = true;
       startingRadiusValue = Double.parseDouble(startingRadius.getText());
       startingIterationsValue = Integer.parseInt(startingIterations.getText());
       radiusInput.setText(Double.toString(startingRadiusValue));
       iterationsInput.setText(Integer.toString(startingIterationsValue));
       calculateZoomAndDetailFactors();
       renderNextAnimation();
    });
    stopAnimation.addActionListener((ActionEvent e) -> {
       stopAnimationValue = true;
    });
    saveEach.addItemListener((ItemEvent e) -> {
      saveEachValue = e.getStateChange() == ItemEvent.SELECTED;
    });
    
    Action updateFrame = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Main.updateFrame();
      }
    };
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "enterPressed");
    progressBar.getActionMap().put("enterPressed", updateFrame);

    fillColorList(startingColors);

    Frame f = new Frame("Mandelbrot Set");
    f.setLayout(null);
    f.setSize(new Dimension(X + 320, Y + 50));
    canvas.setBounds(5, 38, X, Y);
    progressBar.setStringPainted(true);
    f.add(canvas);
    
    startingRadius.setText("2");
    finalRadius.setText("0.01");
    startingIterations.setText("400");
    finalIterations.setText("800");
    steps.setText("20");
    
    int row = 2;
    
    centerXInput.setBounds(X + 40, inputRowHeight * row++, 250, 20);
    centerYInput.setBounds(X + 40, inputRowHeight * row++, 250, 20);
    radiusInput.setBounds(X + 40, inputRowHeight * row++, 250, 20);
    iterationsInput.setBounds(X + 40, inputRowHeight * row++, 250, 20);
    updateButton.setBounds(X + 40, inputRowHeight * row++, 50, 20);
    row++;
    startingRadius.setBounds(X + 40, inputRowHeight * row++, 250, 20);
    finalRadius.setBounds(X + 40, inputRowHeight * row++, 250, 20);
    startingIterations.setBounds(X + 40, inputRowHeight * row++, 250, 20);
    finalIterations.setBounds(X + 40, inputRowHeight * row++, 250, 20);
    steps.setBounds(X + 40, inputRowHeight * row++, 250, 20);
    saveEach.setBounds(X + 40, inputRowHeight * row++, 180, 20);
    startAnimation.setBounds(X + 40, inputRowHeight * row, 100, 20);
    stopAnimation.setBounds(X + 160, inputRowHeight * row++, 100, 20);
    filenamePrefix.setBounds(X + 40, inputRowHeight * row++, 250, 20);
    saveAsImageButton.setBounds(X + 40, inputRowHeight * row++, 100, 20);
    progressBar.setBounds(X + 40, inputRowHeight * row++, 150, 20);
    
    f.add(centerXInput);
    f.add(centerYInput);
    f.add(radiusInput);
    f.add(iterationsInput);
    f.add(updateButton);
    f.add(filenamePrefix);
    f.add(saveAsImageButton);
    f.add(startingRadius);
    f.add(finalRadius);
    f.add(startingIterations);
    f.add(finalIterations);
    f.add(steps);
    f.add(saveEach);
    f.add(startAnimation);
    f.add(stopAnimation);
    f.add(progressBar);
    f.setVisible(true);
    f.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    renderFrame();
  }
  
  private static void updateFrame(){
    centerX = Double.parseDouble(centerXInput.getText());
    centerY = Double.parseDouble(centerYInput.getText());
    radius = Double.parseDouble(radiusInput.getText());
    totalIterations = Integer.parseInt(iterationsInput.getText());
    renderFrame();
  }
  
  private static void resetThreadCompletion(){
    for (int i = 0; i < threadsFinished.length; i++) {
      threadsFinished[i] = false;
    }
  }
  
  private static void markAsComplete(int index){
    threadsFinished[index] = true;
  }
  
  private static boolean allThreadsFinished(){
    boolean result = true;
    for (int i = 0; i < threadsFinished.length; i++) {
      result = result && threadsFinished[i];
      if(!result){
        break;
      }
    }
    return result;
  }
  
  public static void updateProgress(int[] progressCounters){
    int total = 0;
    for (int value : progressCounters) {
      total += value;
    }
    progressBar.setValue(total);
  }
  
  private static void renderDone(){
    canvas.paint(canvas.getGraphics());
    progressBar.setValue(1600);
    resetThreadCompletion();
    System.out.println("Rendering done!!!");
    if(saveEachValue){
      saveAsImage();
    }
    if(animatingValue && !stopAnimationValue){
      renderNextAnimation();
    }
  }
  
  private static void renderNextAnimation(){
    totalIterations = Integer.parseInt(iterationsInput.getText());
    radius = Double.parseDouble(radiusInput.getText());
    updateFrame();
    if (radius > finalRadiusValue) {
      radius *= zoomFactor;
      if (totalIterations < finalIterationsValue) {
        totalIterations = (int)Math.round(totalIterations * detailFactor);
      }
      iterationsInput.setText(Integer.toString(totalIterations));
      radiusInput.setText(Double.toString(radius));
    }else{
      animatingValue = false;
      stopAnimationValue = true;
    }
  }
  
  private static void renderFrame(){
    currentRanges = calculateRanges(radius, centerX, centerY);
    currentFactors = calculateFactors(currentRanges, X, Y);
    Ranges[] quadrantRanges = calculateQuadrantRanges(centerX, centerY, radius);
    
    Thread observer;
    observer = new Thread(() -> {
      Graphics g = I.getGraphics();
      boolean done = Main.allThreadsFinished();
      int[] progress = {0,0,0,0};
      try{
        while(!done){
          ProgressMessage next = threadCompletionQueue.take();
          progress[next.id] = next.progress;
          if(next.type == ProgressMessage.TYPE_PROGRESS){
            Main.updateProgress(progress);
          }else if(next.type == ProgressMessage.TYPE_DONE){
            Main.markAsComplete(next.id);
            g.drawImage(next.I, quadrants[next.id].width, quadrants[next.id].height, null);
          }
          done = Main.allThreadsFinished();
        }
      }catch(InterruptedException e){
        
      }
      threadCompletionQueue.clear();
      Main.renderDone();
    });
    
    observer.start();
    for (int i = 0; i < quadrantRanges.length; i++) {
      Ranges quadrantRange = quadrantRanges[i];
      Renderer renderer = new Renderer(i, quadrantRange, 400, 400, totalIterations, totalColors, colorTable, quadrantImages[i], threadCompletionQueue);
      Thread t = new Thread(renderer);
      t.start();
    }
  }

  private static Ranges calculateRanges(double radius, double x, double y) {
    Ranges ranges = new Ranges(
    x - radius,
    x + radius,
    y - radius,
    y + radius
    );
    return ranges;
  }

  private static Factors calculateFactors(Ranges ranges, int width, int height) {
    Factors factors = new Factors();
    factors.xFactor = width / (ranges.maxRangeX - ranges.minRangeX);
    factors.xScale = ((factors.xFactor * (ranges.maxRangeX + ranges.minRangeX)) - width) / 2;
    factors.yFactor = (-1 * height) / (ranges.maxRangeY - ranges.minRangeY);
    factors.yScale = ((factors.yFactor * (ranges.maxRangeY + ranges.minRangeY)) - height) / 2;
    return factors;
  }

    private static void calculateZoomAndDetailFactors(){
      finalRadiusValue = Double.parseDouble(finalRadius.getText());
      startingRadiusValue = Double.parseDouble(startingRadius.getText());
      finalIterationsValue = Integer.parseInt(finalIterations.getText());
      startingIterationsValue = Integer.parseInt(startingIterations.getText());
      stepsValue = Integer.parseInt(steps.getText());
      zoomFactor = Math.pow(finalRadiusValue / startingRadiusValue, (1.0d / (double)stepsValue));
      detailFactor = Math.pow(finalIterationsValue / startingIterationsValue, (1.0d / (double)stepsValue));
    }

  private static Ranges[] calculateQuadrantRanges(double x, double y, double r) {
    Ranges[] ranges = new Ranges[4];
    ranges[0] = new Ranges(
      x,
      x + r,
      y,
      y + r
    );
    ranges[1] = new Ranges(
      x - r,
      x,
      y,
      y + r
    );
    ranges[2] = new Ranges(
      x - r,
      x,
      y - r,
      y
    );
    ranges[3] = new Ranges(
      x,
      x + r,
      y - r,
      y
    );
    return ranges;
  }

  public static Complex translatePixelToComplex(int x, int y, Factors factors) {
    return new Complex((x + factors.xScale) / factors.xFactor, (y + factors.yScale) / factors.yFactor);
  }

  private static double calculateProportionFactor(int a, int b, double n) {
    return (b - a) / n;
  }

  private static void fillColorList(int[][] startingColors) {
    double segmentSize = (double) totalColors / (double) (startingColors.length - 1);
    double[][] proportionFactors = new double[startingColors.length - 1][3];
    for (int i = 1; i < startingColors.length; i++) {
      proportionFactors[i - 1][0] = calculateProportionFactor(startingColors[i - 1][0], startingColors[i][0], segmentSize);
      proportionFactors[i - 1][1] = calculateProportionFactor(startingColors[i - 1][1], startingColors[i][1], segmentSize);
      proportionFactors[i - 1][2] = calculateProportionFactor(startingColors[i - 1][2], startingColors[i][2], segmentSize);
    }
    for (int i = 0; i < totalColors; i++) {
      colorTable[i] = calculateColorValue(i, proportionFactors, segmentSize, startingColors);
    }
  }

  private static int calculateColorValue(int completed, double[][] proportionFactors, double segmentSize, int[][] startingColors) {
    int red, green, blue;
    int currentSegment = (int) (completed / segmentSize);
    double segmentPortion = completed - (currentSegment * segmentSize);
    red = (int) (startingColors[currentSegment][0] + segmentPortion * proportionFactors[currentSegment][0]);
    green = (int) (startingColors[currentSegment][1] + segmentPortion * proportionFactors[currentSegment][1]);
    blue = (int) (startingColors[currentSegment][2] + segmentPortion * proportionFactors[currentSegment][2]);
    return (red * 65536) + (green * 256) + (blue);
  }

  private static void saveAsImage() {
    String filenameSuffix = String.format("%05d", fileCounter);
    String filename = filenamePrefix.getText() + "-" + filenameSuffix;
    File f = new File(filename + ".jpg");

    try {
      ImageIO.write(I, "jpeg", f);
      fileCounter++;
    } catch (IOException e) {
      System.out.println(e.toString());
    }
  }

  public static void updateInputs(int x, int y, int r) {
    Complex cen = translatePixelToComplex(x, y, currentFactors);
    Complex rad = translatePixelToComplex(r + x, y, currentFactors);
    centerXInput.setText(Double.toString(cen.re()));
    centerYInput.setText(Double.toString(cen.im()));
    radiusInput.setText(Double.toString(Math.abs(rad.re() - cen.re())));
  }
}
