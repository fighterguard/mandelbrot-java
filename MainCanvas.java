package mandeljava;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;

class MainCanvas extends Canvas implements MouseInputListener {

  private int centerClickX;
  private int centerClickY;
  private int radius;
  
  MainCanvas(){
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  @Override
  public void paint(Graphics g) {
    g.drawImage(Main.I, 0, 0, Color.red, null);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    Graphics g = this.getGraphics();
    g.drawImage(Main.I, 0, 0, Color.red, null);
    g.setColor(Color.red);
    g.drawRect(this.centerClickX - this.radius, this.centerClickY - this.radius, this.radius * 2, this.radius * 2);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    this.centerClickX = e.getX();
    this.centerClickY = e.getY();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    Main.updateInputs(this.centerClickX, this.centerClickY, this.radius);
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    int xDist = e.getX() - this.centerClickX;
    int yDist = e.getY() - this.centerClickY;

    this.radius = (int) Math.abs(Math.round(Math.hypot(xDist, yDist)));

    Graphics g = this.getGraphics();
    g.drawImage(Main.I, 0, 0, Color.red, null);
    g.setColor(Color.red);
    g.drawRect(this.centerClickX - this.radius, this.centerClickY - this.radius, this.radius * 2, this.radius * 2);
  }

  @Override
  public void mouseMoved(MouseEvent e) {
  }
}
