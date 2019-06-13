package mandeljava;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class ColorGradientPreview extends Canvas{
  
  private ArrayList<int[]> colors;
  private final int width = 280;
  private final int height = 50;
  
  @Override
  public void paint(Graphics g){
    Graphics2D g2d = (Graphics2D) g;
    float segmentSize = width/(colors.size()-1);
    for (int i = 1; i < colors.size(); i++) {
      int[] colorArray1 = colors.get(i-1);
      int[] colorArray2 = colors.get(i);
      float x1 = segmentSize * (i -1);
      float x2 = segmentSize * (i);
      Color color1 = new Color(colorArray1[0], colorArray1[1], colorArray1[2]);
      Color color2 = new Color(colorArray2[0], colorArray2[1], colorArray2[2]);
      GradientPaint gradient = new GradientPaint(x1, 0, color1, x2, 0, color2);
      g2d.setPaint(gradient);
      g2d.fillRect((int)x1, 0, (int)x2, height);
    }
  }
  
  public void update(ArrayList<int[]> colors){
    this.colors = colors;
    this.paint(this.getGraphics());
  }
}
