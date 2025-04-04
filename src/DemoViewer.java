import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.event.*;
import java.awt.image.BufferedImage;

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

class ModelLoader {
    public Model loadModel(String filename) throws IOException {
        Model model = new Model();
        
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        
        // Calculate model center and size for normalization
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;
        
        ArrayList<Vector3D> tempVertices = new ArrayList<>();
        
        // First pass: read vertices to calculate bounding box
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("v ")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 4) {
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    
                    tempVertices.add(new Vector3D(x, y, z));
                    
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    minZ = Math.min(minZ, z);
                    maxZ = Math.max(maxZ, z);
                }
            }
        }
        
        // Calculate center and scale
        double centerX = (minX + maxX) / 2;
        double centerY = (minY + maxY) / 2;
        double centerZ = (minZ + maxZ) / 2;
        
        double maxDim = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
        double scale = 2.0 / maxDim; // Normalize to fit in a 2x2x2 cube
        
        // Add normalized vertices
        for (Vector3D v : tempVertices) {
            model.vertices.add(new Vector3D(
                (v.x - centerX) * scale,
                (v.y - centerY) * scale,
                (v.z - centerZ) * scale
            ));
        }
        
        // Second pass: read faces
        reader.close();
        reader = new BufferedReader(new FileReader(filename));
        
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("f ")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 4) {
                    // Parse face indices (OBJ format uses 1-based indexing)
                    int v1 = Integer.parseInt(parts[1].split("/")[0]) - 1;
                    int v2 = Integer.parseInt(parts[2].split("/")[0]) - 1;
                    int v3 = Integer.parseInt(parts[3].split("/")[0]) - 1;
                    
                    model.triangles.add(new Triangle(v1, v2, v3));
                    
                    // If the face has more than 3 vertices, triangulate it
                    for (int i = 4; i < parts.length; i++) {
                        int v = Integer.parseInt(parts[i].split("/")[0]) - 1;
                        model.triangles.add(new Triangle(v1, v3, v));
                        v3 = v;
                    }
                }
            }
        }
        
        reader.close();
        return model;
    }
}
