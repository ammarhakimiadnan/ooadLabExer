import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

// LeftCanvas handles displaying and interacting with image items on a canvas
public class LeftCanvas extends JPanel {

    // Represents an image on the canvas with transformation properties
    private static class CanvasImage {
        BufferedImage image;
        Point position;
        double rotation;
        double scale;
        boolean flipH, flipV;

        CanvasImage(BufferedImage img, Point pos) {
            image = img;
            position = pos;
            rotation = 0;
            scale = 1.0;
            flipH = false;
            flipV = false;
        }
    }

    // Types of interaction handles available on selected image
    private enum HandleType { NONE, SCALE, FLIP_TOP, FLIP_BOTTOM, FLIP_LEFT, FLIP_RIGHT, ROTATE }

    private List<CanvasImage> images = new ArrayList<>();  
    private CanvasImage selectedImage = null;              
    private Point dragStartPoint;                          
    private double dragStartRotation;                      
    private double dragStartAngle;                         
    private boolean dragging = false;                      
    private boolean rotating = false;                      
    private HandleType activeHandle = HandleType.NONE;     
    private double canvasRotation = 0;                     
    private final int HANDLE_SIZE = 10;                    

    public LeftCanvas() {
        setBackground(Color.WHITE);
        setupDragAndDrop();   
        setupMouseListeners();
    }

    // Adds an image to the canvas at a default position
    public void addImage(BufferedImage img) {
        images.add(new CanvasImage(img, new Point(50, 50)));
        repaint();
    }

    // Rotates the entire canvas
    public void rotateCanvas(double radians) {
        canvasRotation += radians;
        repaint();
    }

    // Loads an image file into the canvas
    public void loadImageFromFile(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        addImage(img);
    }

    // Drag-and-drop setup for importing images
    private void setupDragAndDrop() {
        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    Transferable t = support.getTransferable();
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        BufferedImage img = ImageIO.read(files.get(0));
                        addImage(img);
                        return true;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LeftCanvas.this, "Error importing image: " + ex.getMessage());
                }
                return false;
            }
        });
    }

    // Mouse listeners for interaction: selection, dragging, rotating, flipping, and scaling
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedImage = getImageAt(e.getPoint()); 
                if (selectedImage != null) {
                    dragStartPoint = e.getPoint();
                    dragStartRotation = selectedImage.rotation;

                    HandleType handle = getHandleAt(e.getPoint());
                    activeHandle = handle;

                    if (handle == HandleType.ROTATE) {
                        // Start rotating
                        rotating = true;
                        dragStartAngle = Math.atan2(e.getY() - selectedImage.position.y, e.getX() - selectedImage.position.x);
                    } else {
                        // Start dragging (either image or scale)
                        dragging = true;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Reset drag/rotate flags
                dragging = false;
                rotating = false;
                activeHandle = HandleType.NONE;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Perform flip on click if a flip handle is clicked
                if (selectedImage != null && activeHandle != HandleType.NONE) {
                    if (activeHandle == HandleType.FLIP_LEFT || activeHandle == HandleType.FLIP_RIGHT) {
                        selectedImage.flipH = !selectedImage.flipH;
                    } else if (activeHandle == HandleType.FLIP_TOP || activeHandle == HandleType.FLIP_BOTTOM) {
                        selectedImage.flipV = !selectedImage.flipV;
                    }
                    repaint();
                }
            }
        });

        // Handles dragging for move, rotate, and scale
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedImage == null) return;

                if (rotating) {
                    // Update rotation angle based on drag
                    double angle = Math.atan2(e.getY() - selectedImage.position.y, e.getX() - selectedImage.position.x);
                    selectedImage.rotation = dragStartRotation + (angle - dragStartAngle);
                    repaint();
                } else if (dragging && activeHandle == HandleType.SCALE) {
                    // Proportional scaling from center
                    int dx = e.getX() - selectedImage.position.x;
                    int dy = e.getY() - selectedImage.position.y;
                    double newScale = Math.max(0.1, Math.min(5.0, Math.sqrt(dx * dx + dy * dy) / 100.0));
                    selectedImage.scale = newScale;
                    repaint();
                } else if (dragging && activeHandle == HandleType.NONE) {
                    // Move the image
                    int dx = e.getX() - dragStartPoint.x;
                    int dy = e.getY() - dragStartPoint.y;
                    selectedImage.position.translate(dx, dy);
                    dragStartPoint = e.getPoint();
                    repaint();
                }
            }
        });

        setFocusable(true); // Enables keyboard focus
    }

    // Returns the topmost image under a given point
    private CanvasImage getImageAt(Point p) {
        for (int i = images.size() - 1; i >= 0; i--) {
            CanvasImage img = images.get(i);
            int w = (int) (img.image.getWidth() * img.scale);
            int h = (int) (img.image.getHeight() * img.scale);
            Rectangle bounds = new Rectangle(img.position.x, img.position.y, w, h);
            if (bounds.contains(p)) return img;
        }
        return null;
    }

    // Detects which handle is clicked at a point (corner, edge, or rotation)
    private HandleType getHandleAt(Point p) {
        if (selectedImage == null) return HandleType.NONE;

        Rectangle box = new Rectangle(selectedImage.position.x, selectedImage.position.y,
                (int) (selectedImage.image.getWidth() * selectedImage.scale),
                (int) (selectedImage.image.getHeight() * selectedImage.scale));

        // Corners for scaling
        Point[] corners = {
                new Point(box.x, box.y),
                new Point(box.x + box.width, box.y),
                new Point(box.x, box.y + box.height),
                new Point(box.x + box.width, box.y + box.height)
        };

        // Edges for flipping
        Point[] edges = {
                new Point(box.x + box.width / 2, box.y),             // top
                new Point(box.x + box.width / 2, box.y + box.height),// bottom
                new Point(box.x, box.y + box.height / 2),            // left
                new Point(box.x + box.width, box.y + box.height / 2) // right
        };

        // Rotation handle
        Point rotateHandle = new Point(box.x + box.width / 2, box.y - 20);

        // Detect scaling handles
        for (Point corner : corners) {
            if (new Rectangle(corner.x - HANDLE_SIZE, corner.y - HANDLE_SIZE, HANDLE_SIZE * 2, HANDLE_SIZE * 2).contains(p))
                return HandleType.SCALE;
        }

        // Detect rotation handle
        if (new Rectangle(rotateHandle.x - HANDLE_SIZE, rotateHandle.y - HANDLE_SIZE, HANDLE_SIZE * 2, HANDLE_SIZE * 2).contains(p))
            return HandleType.ROTATE;

        // Detect flip edges
        if (new Rectangle(edges[0].x - HANDLE_SIZE, edges[0].y - HANDLE_SIZE, HANDLE_SIZE * 2, HANDLE_SIZE * 2).contains(p)) return HandleType.FLIP_TOP;
        if (new Rectangle(edges[1].x - HANDLE_SIZE, edges[1].y - HANDLE_SIZE, HANDLE_SIZE * 2, HANDLE_SIZE * 2).contains(p)) return HandleType.FLIP_BOTTOM;
        if (new Rectangle(edges[2].x - HANDLE_SIZE, edges[2].y - HANDLE_SIZE, HANDLE_SIZE * 2, HANDLE_SIZE * 2).contains(p)) return HandleType.FLIP_LEFT;
        if (new Rectangle(edges[3].x - HANDLE_SIZE, edges[3].y - HANDLE_SIZE, HANDLE_SIZE * 2, HANDLE_SIZE * 2).contains(p)) return HandleType.FLIP_RIGHT;

        return HandleType.NONE;
    }

    // Draws the canvas and its content
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(canvasRotation, getWidth() / 2, getHeight() / 2); // Rotate entire canvas

        // Draw all images with transformations
        for (CanvasImage img : images) {
            AffineTransform at = new AffineTransform();
            at.translate(img.position.x, img.position.y);
            at.rotate(img.rotation, img.image.getWidth() * img.scale / 2, img.image.getHeight() * img.scale / 2);
            at.scale(img.flipH ? -img.scale : img.scale, img.flipV ? -img.scale : img.scale);
            g2.drawImage(img.image, at, null);
        }

        // Draw handles and bounding box for selected image
        if (selectedImage != null) {
            int w = (int) (selectedImage.image.getWidth() * selectedImage.scale);
            int h = (int) (selectedImage.image.getHeight() * selectedImage.scale);
            int x = selectedImage.position.x;
            int y = selectedImage.position.y;

            // Draw red selection box
            g2.setColor(Color.RED);
            g2.drawRect(x, y, w, h);

            // Points for all 8 handles and the rotate handle
            Point[] handles = {
                new Point(x, y),                   // top-left
                new Point(x + w, y),               // top-right
                new Point(x, y + h),               // bottom-left
                new Point(x + w, y + h),           // bottom-right
                new Point(x + w / 2, y),           // top (flip)
                new Point(x + w / 2, y + h),       // bottom (flip)
                new Point(x, y + h / 2),           // left (flip)
                new Point(x + w, y + h / 2),       // right (flip)
                new Point(x + w / 2, y - 20)       // rotation handle
            };

            // Draw circular handles
            for (Point pt : handles) {
                Ellipse2D handle = new Ellipse2D.Double(pt.x - HANDLE_SIZE / 2, pt.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
                g2.setColor(Color.WHITE);
                g2.fill(handle);
                g2.setColor(Color.BLACK);
                g2.draw(handle);
            }
        }

        g2.dispose();
    }

    // Save the canvas content to a file
    public void saveCanvasToFile(File file, String format) throws IOException {
        BufferedImage image = captureCanvas();
        ImageIO.write(image, format, file);
    }

    // Captures the canvas as a BufferedImage
    public BufferedImage captureCanvas() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        paint(g2);
        g2.dispose();
        return image;
    }
}
