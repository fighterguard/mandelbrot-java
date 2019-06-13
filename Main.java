package mandeljava;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;

class Main {

  //Variable declaration
  static final int X = 800, Y = 800;
  static final int inputRowHeight = 30;
  static final int colorRowHeight = 50;
  static BufferedImage I = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);
  static int totalIterations = 400;
  static double centerX = 0;//-0.743643887037151;//-0.1529051455399985; //-0.5803827923341388d;
  static double centerY = 0;//0.131825904205330;//1.039718512720433; //-0.652903457310135d;
  static double radius = 2;//0.000000000051299 //7.120538316886522e-10;
  static int selectedColorIndex;
  static int startingIterationsValue = 400;
  static int finalIterationsValue = 800;
  static double startingRadiusValue = 2.0d;
  static double finalRadiusValue = 0.01d;
  static int stepsValue = 20;
  static double zoomFactor;
  static double detailFactor;
  static int totalColors = 100;
  static int[] colorTable;
  static boolean saveEachValue = false;
  static boolean viewJuliaValue = false;
  static boolean stopAnimationValue = false;
  static boolean animatingValue = false;
  static Ranges currentRanges;
  static Factors currentFactors;
  static MainCanvas canvas = new MainCanvas();
  static int fileCounter = 1;
  static ArrayBlockingQueue<ProgressMessage> threadCompletionQueue = new ArrayBlockingQueue<ProgressMessage>(100);
  static boolean[] threadsFinished = {false, false, false, false};
  static final ArrayList<int[]> startingColors = new ArrayList<int[]>();
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
  private static final Ranges[] juliaRanges = {
    new Ranges(0,2,0,2),
    new Ranges(-2,0,0,2),
    new Ranges(-2,0,-2,0),
    new Ranges(0,2,-2,0)
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
  private static final LabeledInput totalColorsInput = new LabeledInput("Total Colors");
  private static final Checkbox saveEach = new Checkbox("Save each frame as JPEG");
  private static final Checkbox viewJulia = new Checkbox("View Julia set");
  private static final Button updateButton = new Button("Update");
  private static final Button saveAsImageButton = new Button("Save as JPEG");
  private static final Button startAnimation = new Button("Start");
  private static final Button stopAnimation = new Button("Stop");
  private static final Button updateColorButton = new Button("Update");
  private static final Button removeColorButton = new Button("Remove");
  private static final Button addNewColorButton = new Button("Add New");
  private static final Button applyColorsButton = new Button("Apply Colors");
  private static final JProgressBar progressBar = new JProgressBar(0, 1600);
  private static final ColorSlider redSlider = new ColorSlider("Red", 0, 255, 255);
  private static final ColorSlider greenSlider = new ColorSlider("Green", 0, 255, 255);
  private static final ColorSlider blueSlider = new ColorSlider("Blue", 0, 255, 255);
  private static final JPanel centerPanel = new JPanel();
  private static final JPanel animationPanel = new JPanel();
  private static final JPanel colorEditorPanel = new JPanel();
  private static final JPanel colorPreviewWindow = new JPanel();
  private static final JPanel gradientrPreviewWindow = new JPanel();
  private static final ColorGradientPreview colorGradient = new ColorGradientPreview();
  private static ColorList colorList;
  
  //Methods
  static public void main(String[] args) {
    startingColors.add(new int[]{0, 0, 255}); // blue
    startingColors.add(new int[]{255, 255, 255}); // white
    startingColors.add(new int[]{255, 170, 0}); // orange
    startingColors.add(new int[]{0, 0, 0}); // black
    startingColors.add(new int[]{0, 0, 255}); // blue
    colorList = new ColorList(startingColors);
    
    filenamePrefix.setText("mandelbrot-capture");
    centerXInput.setText(Double.toString(centerX));
    centerYInput.setText(Double.toString(centerY));
    radiusInput.setText(Double.toString(radius));
    iterationsInput.setText(Integer.toString(totalIterations));
    
    //Event Listeners
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
    updateColorButton.addActionListener((ActionEvent e) -> {
      if (e.getSource() == updateColorButton) {
        updateColor();
      }
    });
    removeColorButton.addActionListener((ActionEvent e) -> {
      if (e.getSource() == removeColorButton) {
        removeColor();
      }
    });
    addNewColorButton.addActionListener((ActionEvent e) -> {
      if (e.getSource() == addNewColorButton) {
        addNewColor();
      }
    });
    applyColorsButton.addActionListener((ActionEvent e) -> {
      if (e.getSource() == applyColorsButton) {
        fillColorTable();
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
    viewJulia.addItemListener((ItemEvent e) -> {
      viewJuliaValue = e.getStateChange() == ItemEvent.SELECTED;
    });
    colorList.addColorSelectedListener((ColorSelectedEvent e) -> {
      editColor(e.getIndex());
    });
    
    InputMap inputMap = progressBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    Action updateFrame = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Main.updateFrame();
      }
    };
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "enterPressed");
    progressBar.getActionMap().put("enterPressed", updateFrame);
    
    redSlider.addChangeListener((ChangeEvent e) -> {
      updateColorPreview();
    });
    
    greenSlider.addChangeListener((ChangeEvent e) -> {
      updateColorPreview();
    });
    
    blueSlider.addChangeListener((ChangeEvent e) -> {
      updateColorPreview();
    });
    
    startingRadius.setText("2");
    finalRadius.setText("0.01");
    startingIterations.setText("400");
    finalIterations.setText("800");
    steps.setText("20");
    totalColorsInput.setText(Integer.toString(totalColors));
    updateColorButton.setEnabled(false);
    removeColorButton.setEnabled(false);

    Frame f = new Frame("Mandelbrot Set");
    f.setLayout(null);
    f.setSize(new Dimension(X + 720, Y + 50));
    canvas.setBounds(5, 38, X, Y);
    progressBar.setStringPainted(true);
    f.add(canvas);

    fillColorTable();
    setCenterPanel();
    setAnimationPanel();
    setColorEditorPanel();
    
    saveAsImageButton.setBounds(X + 20, 230, 100, 20);
    progressBar.setBounds(X + 20, 540, 150, 20);
    
    int row = 5;
    
    colorList.setBounds(X + 300, (colorRowHeight * row) + 60, colorList.getPreferredSize().width, colorList.getPreferredSize().height);colorPreviewWindow.setLayout(null);
    gradientrPreviewWindow.setLayout(null);
    gradientrPreviewWindow.setBounds(X + 390, 360, 282, 52);
    gradientrPreviewWindow.setBorder(BorderFactory.createLineBorder(Color.black));
    colorGradient.setBounds(1, 1, 300, 50);
    gradientrPreviewWindow.add(colorGradient);
    totalColorsInput.setBounds(X + 360, 330, 250, 20);
    applyColorsButton.setBounds(X + 390, 420, 100, 20);
    
    f.add(centerPanel);
    f.add(animationPanel);
    f.add(saveAsImageButton);
    f.add(progressBar);
    f.add(colorEditorPanel);
    f.add(colorList);
    f.add(gradientrPreviewWindow);
    f.add(applyColorsButton);
    f.add(totalColorsInput);
    f.setVisible(true);
    f.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    renderFrame();
    updateColorPreview();
    colorGradient.update(startingColors);
  }
  
  private static void setCenterPanel(){
    centerPanel.setLayout(null);
    centerPanel.setBackground(Color.WHITE);
    centerPanel.setBounds(X + 10, 40, 280, 180);
    centerPanel.setBorder(BorderFactory.createTitledBorder("Rendering parameters"));
    int row = 1;
    
    centerXInput.setBounds(10, inputRowHeight * row++, 250, 20);
    centerYInput.setBounds(10, inputRowHeight * row++, 250, 20);
    radiusInput.setBounds(10, inputRowHeight * row++, 250, 20);
    iterationsInput.setBounds(10, inputRowHeight * row++, 250, 20);
    updateButton.setBounds(10, inputRowHeight * row, 50, 20);
    viewJulia.setBounds(110, inputRowHeight * row, 150, 20);
    centerPanel.add(centerXInput);
    centerPanel.add(centerYInput);
    centerPanel.add(radiusInput);
    centerPanel.add(iterationsInput);
    centerPanel.add(updateButton);
    centerPanel.add(viewJulia);
  }
  
  private static void setAnimationPanel(){
    animationPanel.setLayout(null);
    animationPanel.setBackground(Color.WHITE);
    animationPanel.setBounds(X + 10, 260, 280, 270);
    animationPanel.setBorder(BorderFactory.createTitledBorder("Animation parameters"));
    int row = 1;
    startingRadius.setBounds(10, inputRowHeight * row++, 250, 20);
    finalRadius.setBounds(10, inputRowHeight * row++, 250, 20);
    startingIterations.setBounds(10, inputRowHeight * row++, 250, 20);
    finalIterations.setBounds(10, inputRowHeight * row++, 250, 20);
    steps.setBounds(10, inputRowHeight * row++, 250, 20);
    saveEach.setBounds(10, inputRowHeight * row++, 180, 20);
    filenamePrefix.setBounds(10, inputRowHeight * row++, 250, 20);
    startAnimation.setBounds(10, inputRowHeight * row, 100, 20);
    stopAnimation.setBounds(120, inputRowHeight * row++, 100, 20);
    animationPanel.add(startingRadius);
    animationPanel.add(finalRadius);
    animationPanel.add(startingIterations);
    animationPanel.add(finalIterations);
    animationPanel.add(steps);
    animationPanel.add(saveEach);
    animationPanel.add(startAnimation);
    animationPanel.add(stopAnimation);
    animationPanel.add(filenamePrefix);
  }
  
  private static void setColorEditorPanel(){
    colorEditorPanel.setLayout(null);
    colorEditorPanel.setBackground(Color.WHITE);
    colorEditorPanel.setBounds(X + 300, 40, 370, 270);
    colorEditorPanel.setBorder(BorderFactory.createTitledBorder("Color editor"));
    colorPreviewWindow.setLayout(null);
    colorPreviewWindow.setBackground(Color.WHITE);
    colorPreviewWindow.setBounds(220, 20, 130, 130);
    colorPreviewWindow.setBorder(BorderFactory.createLineBorder(Color.black));
    int row  = 0;
    redSlider.setBounds(10, (colorRowHeight * row++) + 20, 320, 50);
    greenSlider.setBounds(10, (colorRowHeight * row++) + 20, 320, 50);
    blueSlider.setBounds(10, (colorRowHeight * row++) + 20, 320, 50);
    updateColorButton.setBounds(40, (colorRowHeight * row) + 20, 80, 20);
    removeColorButton.setBounds(40, (colorRowHeight * row) + 50, 80, 20);
    addNewColorButton.setBounds(40, (colorRowHeight * row) + 80, 80, 20);
    colorEditorPanel.add(colorPreviewWindow);
    colorEditorPanel.add(redSlider);
    colorEditorPanel.add(greenSlider);
    colorEditorPanel.add(blueSlider);
    colorEditorPanel.add(updateColorButton);
    colorEditorPanel.add(removeColorButton);
    colorEditorPanel.add(addNewColorButton);
  }
  
  private static void updateColorPreview(){
      colorPreviewWindow.setBackground(new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue()));
  }
  
  private static void updateFrame(){
    centerX = Double.parseDouble(centerXInput.getText());
    centerY = Double.parseDouble(centerYInput.getText());
    radius = Double.parseDouble(radiusInput.getText());
    totalIterations = Integer.parseInt(iterationsInput.getText());
    if(viewJuliaValue){
      renderJulia();
    }else{
      renderFrame();
    }
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
      Renderer renderer = new Renderer(i, quadrantRange, 400, 400, totalIterations, totalColors, colorTable, quadrantImages[i], threadCompletionQueue, false, null);
      Thread t = new Thread(renderer);
      t.start();
    }
  }
  
  
  
  private static void renderJulia(){
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
    for (int i = 0; i < juliaRanges.length; i++) {
      Ranges quadrantRange = juliaRanges[i];
      Renderer renderer = new Renderer(i, quadrantRange, 400, 400, totalIterations, totalColors, colorTable, quadrantImages[i], threadCompletionQueue, true, new Complex(centerX, centerY));
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

  private static void fillColorTable() {
    totalColors = Integer.parseInt(totalColorsInput.getText());
    colorTable = new int[totalColors];
    double segmentSize = (double) totalColors / (double) (startingColors.size() - 1);
    double[][] proportionFactors = new double[startingColors.size() - 1][3];
    for (int i = 1; i < startingColors.size(); i++) {
      proportionFactors[i - 1][0] = calculateProportionFactor(startingColors.get(i - 1)[0], startingColors.get(i)[0], segmentSize);
      proportionFactors[i - 1][1] = calculateProportionFactor(startingColors.get(i - 1)[1], startingColors.get(i)[1], segmentSize);
      proportionFactors[i - 1][2] = calculateProportionFactor(startingColors.get(i - 1)[2], startingColors.get(i)[2], segmentSize);
    }
    for (int i = 0; i < totalColors; i++) {
      colorTable[i] = calculateColorValue(i, proportionFactors, segmentSize, startingColors);
    }
  }

  private static int calculateColorValue(int completed, double[][] proportionFactors, double segmentSize, ArrayList<int[]> startingColors) {
    int red, green, blue;
    int currentSegment = (int) (completed / segmentSize);
    double segmentPortion = completed - (currentSegment * segmentSize);
    red = (int) (startingColors.get(currentSegment)[0] + segmentPortion * proportionFactors[currentSegment][0]);
    green = (int) (startingColors.get(currentSegment)[1] + segmentPortion * proportionFactors[currentSegment][1]);
    blue = (int) (startingColors.get(currentSegment)[2] + segmentPortion * proportionFactors[currentSegment][2]);
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

  private static void editColor(int index) {
    selectedColorIndex = index;
    int[] selectedColor = startingColors.get(index);
    colorPreviewWindow.setBackground(new Color(selectedColor[0], selectedColor[1], selectedColor[2]));
    redSlider.setValue(selectedColor[0]);
    greenSlider.setValue(selectedColor[1]);
    blueSlider.setValue(selectedColor[2]);
    updateColorButton.setEnabled(true);
    removeColorButton.setEnabled(true);
  }

  private static void updateColor() {
    int[] updatedColor = startingColors.get(selectedColorIndex);
    updatedColor[0] = redSlider.getValue();
    updatedColor[1] = greenSlider.getValue();
    updatedColor[2] = blueSlider.getValue();
    updateColorButton.setEnabled(false);
    removeColorButton.setEnabled(false);
    colorList.clearSelection();
    colorList.fillColorList(startingColors);
    colorGradient.update(startingColors);
  }

  private static void removeColor() {
    startingColors.remove(selectedColorIndex);
    updateColorButton.setEnabled(false);
    removeColorButton.setEnabled(false);
    colorList.clearSelection();
    colorList.fillColorList(startingColors);
    colorGradient.update(startingColors);
  }

  private static void addNewColor() {
    startingColors.add(new int[]{
      redSlider.getValue(),
      greenSlider.getValue(),
      blueSlider.getValue()
    });
    updateColorButton.setEnabled(false);
    removeColorButton.setEnabled(false);
    colorList.clearSelection();
    colorList.fillColorList(startingColors);
    colorList.setSize(colorList.getPreferredSize().width, colorList.getPreferredSize().height);
    colorGradient.update(startingColors);
  }
}
