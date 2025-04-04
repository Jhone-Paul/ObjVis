import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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

class RenderPanel extends JPanel {
  private Model model;
  private int heading =180;
  private int pitch =0;
  private double scale = 1.0;

  public RenderPanel(Model model) {
    this.model = model;

    addMouseWheelListener(new MouseWheelListener(){
      public void mouseWheelMoved(MouseWheelEvent e) {
        scale =e.getPreciseWheelRotation()*0.1;
        if (scale < 0.1) scale = 0.1;
        if (scale > 10) scale = 10;
        repaint();
      }
    });
  }

  public void setModel(Model model){
    this.model = model;
    repaint();
  }
  public void setHeading(int heading) {
        this.heading = heading;
        repaint();
  }
    
  public void setPitch(int pitch) {
      this.pitch = pitch;
    repaint();
  }
    
  @Override
  protected void paintComponent(Graphics g) {
      super.paintComponent(g);
        
      if (model == null) return;
      
      Graphics2D g2 = (Graphics2D) g;
      g2.setColor(Color.BLACK);
      g2.fillRect(0, 0, getWidth(), getHeight());
        
      // Enable anti-aliasing
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Center of the panel
      int centerX = getWidth() / 2;
      int centerY = getHeight() / 2;
        
      // Calculate sine and cosine values for the rotations
      double headingRad = Math.toRadians(heading);
      double pitchRad = Math.toRadians(pitch);
      double cosHeading = Math.cos(headingRad);
      double sinHeading = Math.sin(headingRad);
      double cosPitch = Math.cos(pitchRad);
      double sinPitch = Math.sin(pitchRad);
        
        // Create z-buffer (for depth sorting)
      double[][] zBuffer = new double[getWidth()][getHeight()];
      for (int x = 0; x < getWidth(); x++) {
          for (int y = 0; y < getHeight(); y++) {
              zBuffer[x][y] = Double.NEGATIVE_INFINITY;
            }
        }
        
      BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2img = img.createGraphics();
        g2img.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Project all triangles
        for (Triangle triangle : model.triangles) {
            Vector3D v1 = model.vertices.get(triangle.v1);
            Vector3D v2 = model.vertices.get(triangle.v2);
            Vector3D v3 = model.vertices.get(triangle.v3);
            
            // Apply rotation and projection to each vertex
            Point p1 = projectVertex(v1, centerX, centerY, cosHeading, sinHeading, cosPitch, sinPitch);
            Point p2 = projectVertex(v2, centerX, centerY, cosHeading, sinHeading, cosPitch, sinPitch);
            Point p3 = projectVertex(v3, centerX, centerY, cosHeading, sinHeading, cosPitch, sinPitch);
            
            // Calculate the normal vector of the triangle
            Vector3D edge1 = v2.subtract(v1);
            Vector3D edge2 = v3.subtract(v1);
            Vector3D normal = edge1.cross(edge2).normalize();
            
            // Simple lighting calculation
            Vector3D lightDir = new Vector3D(0, 0, -1); // Light direction
            double lightIntensity = Math.max(0.1, normal.dot(lightDir));
            int shade = (int)(lightIntensity * 200);
            
            // Create polygon for the triangle
            Polygon poly = new Polygon();
            poly.addPoint(p1.x, p1.y);
            poly.addPoint(p2.x, p2.y);
            poly.addPoint(p3.x, p3.y);
            
            // Compute average Z for depth sorting
            double avgZ = (v1.z + v2.z + v3.z) / 3.0;
            
            // Draw the triangle with basic lighting
            g2img.setColor(new Color(shade, shade, shade));
            g2img.fillPolygon(poly);
            g2img.setColor(Color.DARK_GRAY);
            g2img.drawPolygon(poly);
        }
        
        g2.drawImage(img, 0, 0, null);
        
        // Display controls info
        g2.setColor(Color.WHITE);
        g2.drawString("Use sliders to rotate, mouse wheel to zoom", 10, 20);
        g2.drawString("Heading: " + heading + "°, Pitch: " + pitch + "°, Zoom: " + String.format("%.1f", scale) + "x", 10, 40); 
    }
    
    private Point projectVertex(Vector3D v, int centerX, int centerY, 
                               double cosHeading, double sinHeading,
                               double cosPitch, double sinPitch) {
        // Apply rotation around Y-axis (heading)
        double x = v.x * cosHeading - v.z * sinHeading;
        double y = v.y;
        double z = v.x * sinHeading + v.z * cosHeading;
        
        // Apply rotation around X-axis (pitch)
        double y2 = y * cosPitch - z * sinPitch;
        double z2 = y * sinPitch + z * cosPitch;
        
        // Apply scale
        x *= scale * 100;
        y2 *= scale * 100;
        z2 *= scale * 100;
        
        // Apply perspective projection
        double distance = 5;
        double perspective = distance / (distance + z2 + 10);
        
        int screenX = centerX + (int)(x * perspective);
        int screenY = centerY - (int)(y2 * perspective); // Y is flipped in screen coordinates
        
        return new Point(screenX, screenY);
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
