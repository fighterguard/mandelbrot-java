package mandeljava;

import java.awt.image.BufferedImage;
import java.util.concurrent.ArrayBlockingQueue;

public class Renderer implements Runnable{
  
  private int id;
  private Ranges currentRanges;
  private Factors currentFactors;
  private int totalIterations, width, height, totalColors;
  private int[] colorTable;
  private BufferedImage QI;
  private ArrayBlockingQueue<ProgressMessage> q;
  private boolean isJulia;
  private Complex juliaCenter;
  
  Renderer(int id, Ranges r, int w, int h, int ti, int tc, int[] ct, BufferedImage qi, ArrayBlockingQueue<ProgressMessage> q, boolean j, Complex jc){
    this.setValues(id, r, w, h, ti, tc, ct, qi, q, j, jc);
  }
  
  private void setValues(int id, Ranges r, int w, int h, int ti, int tc, int[] ct, BufferedImage qi, ArrayBlockingQueue<ProgressMessage> q, boolean j, Complex jc){
    this.id = id;
    this.currentRanges = r;
    this.width = w;
    this.height = h;
    this.totalIterations = ti;
    this.totalColors = tc;
    this.colorTable = ct;
    this.QI = qi;
    this.q = q;
    this.isJulia = j;
    this.juliaCenter = jc;
  }

  public void renderFrame() {
    currentFactors = calculateFactors(currentRanges, width, height);

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
      try{
        this.q.add(new ProgressMessage(ProgressMessage.TYPE_PROGRESS, id, null, i));
      }catch(Exception e){
        
      }
    }
    
    long endTime = System.currentTimeMillis();
    long elapsedTime = endTime - startTime;
    System.out.println("Rendering took " + elapsedTime);
    this.q.add(new ProgressMessage(ProgressMessage.TYPE_DONE, id, QI, width));
  }
  
  public void renderJulia() {
    currentFactors = calculateFactors(currentRanges, width, height);

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < width; ++i) {
      for (int j = 0; j < height; ++j) {
        Complex z = Main.translatePixelToComplex(i, j, currentFactors);
        boolean outOfBounds = false;
        Complex c = juliaCenter;
        int completedIterations = 0;
        for (int k = 0; k < totalIterations; k++) {
          Complex result = iterate(z, c);
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
      try{
        this.q.add(new ProgressMessage(ProgressMessage.TYPE_PROGRESS, id, null, i));
      }catch(Exception e){
        
      }
    }
    
    long endTime = System.currentTimeMillis();
    long elapsedTime = endTime - startTime;
    System.out.println("Rendering took " + elapsedTime);
    this.q.add(new ProgressMessage(ProgressMessage.TYPE_DONE, id, QI, width));
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
    if(this.isJulia){
      this.renderJulia();
    }else{
      this.renderFrame();
    }
  }
}
