package mandeljava;

import java.awt.Color;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ColorItem extends Panel implements ItemListener {
  private final JRadioButton radio;
  private final JPanel colorWindow;
  private final int index;
  private final ArrayList<ColorSelectedListener>listeners;
  public static int preferredWidth = 40;
  public static int preferredHeight = 20;
  
  public ColorItem(int index, Color color){
    this.index = index;
    this.listeners = new ArrayList<ColorSelectedListener>();
    this.setLayout(null);
    this.radio = new JRadioButton();
    this.radio.setBackground(Color.WHITE);
    this.colorWindow = new JPanel();
    this.colorWindow.setLayout(null);
    this.colorWindow.setBackground(color);
    this.colorWindow.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    this.radio.setBounds(0, 0, 20, 20);
    this.colorWindow.setBounds(20, 0, 20, 20);
    this.add(this.radio);
    this.add(this.colorWindow);
    this.radio.addItemListener(this);
  }
  
  public JRadioButton getRadio(){
    return this.radio;
  }
  
  public void addColorSelectedListener(ColorSelectedListener l){
    listeners.add(l);
  }
  
  public void removeColorSelectedListener(ColorSelectedListener l){
    if(listeners.contains(l)){
      listeners.remove(l);
    }
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    for (int i = 0; i < listeners.size(); i++) {
      if(this.radio.isSelected()){
        listeners.get(i).colorSelected(new ColorSelectedEvent(this.index));
      }
    }
  }
}
