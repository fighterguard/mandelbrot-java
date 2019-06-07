package mandeljava;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

class Main {

  static final int X = 800, Y = 800;
  static BufferedImage I = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);
  static int totalIterations = 400;
  static double centerX = 0;//-0.743643887037151;//-0.1529051455399985; //-0.5803827923341388d;
  static double centerY = 0;//0.131825904205330;//1.039718512720433; //-0.652903457310135d;
  static double radius = 2;//0.000000000051299 //7.120538316886522e-10;
  static final int totalColors = 400;
  static int[] colorTable = new int[totalColors];
  static Ranges currentRanges;
  static Factors currentFactors;
  static MainCanvas canvas = new MainCanvas();
  static int fileCounter = 1;
  static final int[][] startingColors = {
    {0, 0, 0}, // black
    {0, 0, 255}, // blue
    {0, 255, 255}, // cyan
    {255, 255, 255}, // white
    {0, 0, 0}, // black
  };
  static BufferedImage[] quadrantImages = {
    new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB),
    new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB),
    new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB),
    new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB)
  };

  public static final LabeledInput centerXInput = new LabeledInput("Real Part");
  public static final LabeledInput centerYInput = new LabeledInput("Imaginary Part");
  public static final LabeledInput radiusInput = new LabeledInput("Radius");
  public static final LabeledInput iterationsInput = new LabeledInput("Iterations");
  public static final LabeledInput filenamePrefix = new LabeledInput("File name");
  public static final Button updateButton = new Button("Update");
  public static final Button saveAsImageButton = new Button("Save as JPEG");

  static public void main(String[] args) {
    filenamePrefix.setText("mandelbrot-capture");
    centerXInput.setText(Double.toString(centerX));
    centerYInput.setText(Double.toString(centerY));
    radiusInput.setText(Double.toString(radius));
    iterationsInput.setText(Integer.toString(totalIterations));
    updateButton.addActionListener((ActionEvent e) -> {
      if (e.getSource() == updateButton) {
        centerX = Double.parseDouble(centerXInput.getText());
        centerY = Double.parseDouble(centerYInput.getText());
        radius = Double.parseDouble(radiusInput.getText());
        totalIterations = Integer.parseInt(iterationsInput.getText());
        renderFrame();
      }
    });
    saveAsImageButton.addActionListener((ActionEvent e) -> {
      if (e.getSource() == saveAsImageButton) {
        String filenameSuffix = String.format("%05d", fileCounter);
        saveAsImage(I, filenamePrefix.getText() + "-" + filenameSuffix);
      }
    });

    fillColorList(startingColors);

    Frame f = new Frame("Mandelbrot Set");
    f.setLayout(null);
    f.setSize(new Dimension(X + 320, Y + 50));
    canvas.setBounds(5, 38, X, Y);
    f.add(canvas);
    centerXInput.setBounds(X + 40, 50, 250, 20);
    centerYInput.setBounds(X + 40, 100, 250, 20);
    radiusInput.setBounds(X + 40, 150, 250, 20);
    iterationsInput.setBounds(X + 40, 200, 250, 20);
    updateButton.setBounds(X + 40, 250, 50, 20);
    filenamePrefix.setBounds(X + 40, 300, 250, 20);
    saveAsImageButton.setBounds(X + 40, 350, 100, 20);
    f.add(centerXInput);
    f.add(centerYInput);
    f.add(radiusInput);
    f.add(iterationsInput);
    f.add(updateButton);
    f.add(filenamePrefix);
    f.add(saveAsImageButton);
    f.setVisible(true);
    f.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    renderFrame();
  }
  
  private static void renderFrame(){
    currentRanges = calculateRanges(radius, centerX, centerY);
    currentFactors = calculateFactors(currentRanges, X, Y);
    Ranges[] quadrantRanges = calculateQuadrantRanges(centerX, centerY, radius);
    for (int i = 0; i < quadrantRanges.length; i++) {
      Ranges quadrantRange = quadrantRanges[i];
      Renderer renderer = new Renderer(i, quadrantRange, 400, 400, totalIterations, totalColors, colorTable, I, quadrantImages[i], canvas);
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

  private static void saveAsImage(BufferedImage image, String filename) {
    File f = new File(filename + ".jpg");

    try {
      ImageIO.write(image, "jpeg", f);
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
