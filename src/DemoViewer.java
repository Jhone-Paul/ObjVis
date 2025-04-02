import javax.swing.*;
import java.awt.*;
import java.util.*;

public class DemoViewer {
  public static void main(String[] args) {
    String FilePath = "";
    try {
      FilePath = args[0];
      System.out.println("Showing files at: "+ FilePath);
    } catch (Exception e) {
      System.err.println("Error, please ensure the right amount of arguments \n" + e);
      System.exit(0);
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

// Vector3D class for 3D vectors
class Vector3D {
    public double x, y, z;
    
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vector3D subtract(Vector3D other) {
        return new Vector3D(x - other.x, y - other.y, z - other.z);
    }
    
    public Vector3D cross(Vector3D other) {
        return new Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }
    
    public double dot(Vector3D other) {
        return x * other.x + y * other.y + z * other.z;
    }
    
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }
    
    public Vector3D normalize() {
        double len = length();
        if (len > 0) {
            return new Vector3D(x / len, y / len, z / len);
        }
        return new Vector3D(0, 0, 0);
    }
}

// Triangle class for faces
class Triangle {
    public int v1, v2, v3; // Indices into the vertex list
    
    public Triangle(int v1, int v2, int v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }
}

// Model class to hold the OBJ data
class Model {
    public ArrayList<Vector3D> vertices = new ArrayList<>();
    public ArrayList<Triangle> triangles = new ArrayList<>();
}



