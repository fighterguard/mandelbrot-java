package mandeljava;

public class ColorSelectedEvent{
  
  private int index;
  
  public ColorSelectedEvent(int index){
    this.index = index;
  }
  
  public int getIndex(){
    return this.index;
  }
}
