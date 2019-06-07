package mandeljava;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Renderer implements Runnable{
  
  private int id;
  private Ranges currentRanges;
  private Factors currentFactors;
  private int totalIterations, width, height, totalColors;
  private int[] colorTable;
  private BufferedImage I, QI;
  private MainCanvas canvas;
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
  
  Renderer(int id, Ranges r, int w, int h, int ti, int tc, int[] ct, BufferedImage bi, BufferedImage qi, MainCanvas mc){
    this.setValues(id, r, w, h, ti, tc, ct, bi, qi, mc);
  }
  
  final void setValues(int id, Ranges r, int w, int h, int ti, int tc, int[] ct, BufferedImage bi, BufferedImage qi, MainCanvas mc){
    this.id = id;
    this.currentRanges = r;
    this.width = w;
    this.height = h;
    this.totalIterations = ti;
    this.totalColors = tc;
    this.colorTable = ct;
    this.I = bi;
    this.QI = qi;
    this.canvas = mc;
  }

  public void renderFrame() {
    currentFactors = calculateFactors(currentRanges, width, height);
    Graphics g = canvas.getGraphics();

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < width; ++i) {
      for (int j = 0; j < height; ++j) {
        Complex iteratee = Main.translatePixelToComplex(i, j, currentFactors);
        boolean outOfBounds = false;
        Complex z = new Complex(0.0d, 0.0d);
        int completedIterations = 0;
        for (int k = 0; k < totalIterations; k++) {
          Complex result = iterate(z, iteratee);
          completedIterations++;
          if (result.abs() > 2.0d) {
            outOfBounds = true;
            break;
          }
          z = result;
        }
        if (outOfBounds) {
          QI.setRGB(i, j, colorTable[(completedIterations - 1) % totalColors]);
        } else {
          QI.setRGB(i, j, 0x000000);
        }
      }
    }
    
    long endTime = System.currentTimeMillis();
    long elapsedTime = endTime - startTime;
    System.out.println("Rendering took " + elapsedTime);
    I.getGraphics().drawImage(QI, quadrants[id].width, quadrants[id].height, null);
    canvas.paint(g);
  }

  private static Complex iterate(Complex z, Complex c) {
    return z.times(z).plus(c);
  }

  private static Factors calculateFactors(Ranges ranges, int width, int height) {
    Factors factors = new Factors();
    factors.xFactor = width / (ranges.maxRangeX - ranges.minRangeX);
    factors.xScale = ((factors.xFactor * (ranges.maxRangeX + ranges.minRangeX)) - width) / 2;
    factors.yFactor = (-1 * height) / (ranges.maxRangeY - ranges.minRangeY);
    factors.yScale = ((factors.yFactor * (ranges.maxRangeY + ranges.minRangeY)) - height) / 2;
    return factors;
  }

  @Override
  public void run() {
    this.renderFrame();
  }
}
