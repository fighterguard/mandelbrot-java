package mandeljava;

import java.awt.image.BufferedImage;

public class ProgressMessage {
  public int id;
  public BufferedImage I;
  public static final int TYPE_PROGRESS = 0;
  public static final int TYPE_DONE = 1;
  public int type;
  public int progress;

  public ProgressMessage(int type, int id, BufferedImage I, int progress){
    this.id = id;
    this.I = I;
    this.type = type;
    this.progress = progress;
  }
}
