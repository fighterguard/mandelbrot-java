package mandeljava;

import java.awt.Color;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

public class ColorSlider extends Panel{
  private JSlider slider;
  private JLabel label;
  
  public ColorSlider(String labelText, int min, int max, int value){
    this.slider = new JSlider(min, max, value);
    this.slider.setBackground(Color.WHITE);
    this.label = new JLabel(labelText);
    slider.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
         JSlider sourceSlider=(JSlider)e.getSource();
         BasicSliderUI ui = (BasicSliderUI)sourceSlider.getUI();
         int value = ui.valueForXPosition( e.getX() );
         slider.setValue(value);
      }
    });
    
    this.label.setHorizontalAlignment(SwingConstants.RIGHT);
    this.setLayout(null);
    this.slider.setBounds(60, 0, 128, 30);
    this.label.setBounds(0, 0, 50, 30);
    this.add(this.slider);
    this.add(this.label);
  }
  
  public void setValue(int value){
    this.slider.setValue(value);
  }
  
  public int getValue(){
    return slider.getValue();
  }
  
  public void addChangeListener(ChangeListener l){
    this.slider.addChangeListener(l);
  }
}
