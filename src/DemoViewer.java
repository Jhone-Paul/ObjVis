import javax.swing.*;
import java.awt.*;

public class DemoViewer {
  public static void main(String[] args) {
    String FilePath = "";
    try {
      FilePath = args[0];
      System.out.println("Showing files at: "+ FilePath);
    } catch (Exception e) {
      System.out.println("Error, please ensure the right amount of arguments \n" + e);
    }

    JFrame frame = new JFrame("OBJ Viewer");
    //frame.setDefualtCloseOperation(JFrame.EXIT_ON_CLOSE);
    Container pane = frame.getContentPane();
    pane.setLayout(new BorderLayout());
  
    JSlider headingSlider = new JSlider(0,360,180);
    pane.add(headingSlider, BorderLayout.SOUTH);

    JSlider pitchSlider = new JSlider(0,360,180);
    pane.add(pitchSlider, BorderLayout.EAST);
    JPanel renderPanel = new JPanel() {
      public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

                    // rendering magic will happen here
        }
      };
      pane.add(renderPanel, BorderLayout.CENTER);

      frame.setSize(400, 400);
      frame.setVisible(true);
  }
}

