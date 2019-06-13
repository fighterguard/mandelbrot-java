package mandeljava;

import java.awt.Dimension;
import java.awt.Panel;
import java.awt.TextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class LabeledInput extends Panel {

  private final JLabel label;
  private final TextField input;

  public LabeledInput(String labelText) {
    this.label = new JLabel(labelText);
    this.input = new TextField();
    super.setLayout(null);
    this.label.setHorizontalAlignment(SwingConstants.RIGHT);
    this.label.setBounds(0, 0, 95, 20);
    this.input.setBounds(100, 0, 150, 20);
    super.setPreferredSize(new Dimension(250, 20));
    super.add(label);
    super.add(input);
  }

  public void setText(String text) {
    this.input.setText(text);
  }

  public String getText() {
    return this.input.getText();
  }
}
