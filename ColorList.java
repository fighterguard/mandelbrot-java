package mandeljava;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

public class ColorList extends JPanel implements ColorSelectedListener{
  
  private final int rowHeight = ColorItem.preferredHeight;
  private static ButtonGroup group;
  private final ArrayList<ColorSelectedListener>listeners;
  
  public ColorList(ArrayList<int[]> startingColors){
    this.listeners = new ArrayList<ColorSelectedListener>();
    this.setLayout(null);
    this.setBackground(Color.WHITE);
    this.setBorder(BorderFactory.createTitledBorder("Color list"));
    this.fillColorList(startingColors);
  }
  
  public void fillColorList(ArrayList<int[]> startingColors){
    this.removeAll();
    group = new ButtonGroup();
    this.setPreferredSize(new Dimension(80, rowHeight * (startingColors.size() + 2)));
    for (int i = 0; i < startingColors.size(); i++) {
      int[] startingColor = startingColors.get(i);
      ColorItem ci = new ColorItem(i, new Color(startingColor[0], startingColor[1], startingColor[2]));
      ci.setBounds(10, rowHeight * (i+1), ColorItem.preferredWidth, ColorItem.preferredHeight);
      ci.addColorSelectedListener(this);
      this.add(ci);
      group.add(ci.getRadio());
    }
  }
  
  public void addColorSelectedListener(ColorSelectedListener l){
    listeners.add(l);
  }
  
  public void removeColorSelectedListener(ColorSelectedListener l){
    if(listeners.contains(l)){
      listeners.remove(l);
    }
  }
  
  public void clearSelection(){
    this.group.clearSelection();
  }

  @Override
  public void colorSelected(ColorSelectedEvent e) {
    for (int i = 0; i < listeners.size(); i++) {
       listeners.get(i).colorSelected(e);
    }
  }
}
