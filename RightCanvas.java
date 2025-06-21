// Canvas for freehand drawing, image upload, and editing with pen or eraser functionality.
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class RightCanvas extends JPanel {
    private BufferedImage drawingBuffer;
    private BufferedImage uploadedImage;
    private Point imagePosition = null;
    private Point dragStartPoint;
    private Color penColor = Color.BLACK;
    private int penSize = 4;
    private Point previousPoint;
    private Dimension drawingSize = new Dimension(800, 600);
    private boolean imageSelected = false;
    private boolean eraserMode = false;

    // Constructor initializes the canvas with a white background and sets up mouse listeners
    public RightCanvas() {
        setBackground(Color.WHITE);
        setToolTipText("Right-click to change pen color! Double-click image to move it.");
        initializeDrawingBuffer();
        setupMouseListeners();
        setupDragAndDrop();
    }

    // Initialize the drawing buffer with the specified size and type
    private void initializeDrawingBuffer() {
        drawingBuffer = new BufferedImage(
            drawingSize.width,
            drawingSize.height,
            BufferedImage.TYPE_INT_ARGB
        );
    }

    // Set up mouse listeners for drawing, image selection, and drag-and-drop functionality
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            // Handle mouse press events for drawing or selecting images
            public void mousePressed(MouseEvent e) {
                if (imageSelected && uploadedImage != null && imagePosition != null) {
                    // Check if click is on the selected image
                    Rectangle imageRect = new Rectangle(
                        imagePosition.x, 
                        imagePosition.y, 
                        uploadedImage.getWidth(), 
                        uploadedImage.getHeight()
                    );
                    if (imageRect.contains(e.getPoint())) {
                        dragStartPoint = e.getPoint();
                        return;
                    }
                }
                // If not on selected image, start drawing
                previousPoint = e.getPoint();
            }
            
            // Handle mouse release events to stop drawing or moving images
            public void mouseReleased(MouseEvent e) {
                previousPoint = null;
                dragStartPoint = null;
            }
            
            // Handle mouse click events for changing pen color or selecting images
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Color newColor = JColorChooser.showDialog(
                        e.getComponent(), 
                        "Choose Pen Color!", 
                        penColor
                    );
                    if (newColor != null) {
                        penColor = newColor;
                    }
                }
                else if (e.getClickCount() == 2 && !e.isConsumed()) {
                    // Double-click to select/deselect image
                    if (uploadedImage != null && imagePosition != null) {
                        Rectangle imageRect = new Rectangle(
                            imagePosition.x, 
                            imagePosition.y, 
                            uploadedImage.getWidth(), 
                            uploadedImage.getHeight()
                        );
                        if (imageRect.contains(e.getPoint())) {
                            imageSelected = !imageSelected;
                            repaint();
                        } else {
                            imageSelected = false;
                        }
                    } else {
                        imageSelected = false;
                    }
                }
            }
        });

        // Mouse motion listener for dragging images or drawing with pen/eraser
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (imageSelected && dragStartPoint != null && uploadedImage != null) {
                    // Move the image
                    int dx = e.getX() - dragStartPoint.x;
                    int dy = e.getY() - dragStartPoint.y;
                    imagePosition.x += dx;
                    imagePosition.y += dy;
                    dragStartPoint = e.getPoint();
                    repaint();
                } else if (previousPoint != null) {
                    // Draw with pen or eraser
                    Point currentPoint = e.getPoint();
                    
                    Graphics2D g2d = drawingBuffer.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    if (eraserMode) {
                        // Erase by drawing with background color
                        g2d.setComposite(AlphaComposite.Clear);
                    } else {
                        // Normal drawing with pen color
                        g2d.setColor(penColor);
                        g2d.setComposite(AlphaComposite.SrcOver);
                    }
                    
                    g2d.setStroke(new BasicStroke(penSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    
                    double xScale = (double)drawingBuffer.getWidth() / getWidth();
                    double yScale = (double)drawingBuffer.getHeight() / getHeight();
                    
                    int x1 = (int)(previousPoint.x * xScale);
                    int y1 = (int)(previousPoint.y * yScale);
                    int x2 = (int)(currentPoint.x * xScale);
                    int y2 = (int)(currentPoint.y * yScale);
                    
                    g2d.drawLine(x1, y1, x2, y2);
                    g2d.dispose();
                    
                    previousPoint = currentPoint;
                    repaint();
                }
            }
        });
    }

    // Set up drag-and-drop functionality for image uploads
    private void setupDragAndDrop() {
        setTransferHandler(new TransferHandler() {
            // Allow files to be dropped onto the canvas
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            // Handle the import of dropped files
            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    Transferable t = support.getTransferable();
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File file = files.get(0);
                        loadImageFromFile(file);
                        return true;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RightCanvas.this, 
                        "Error importing image: " + ex.getMessage());
                }
                return false;
            }
        });
    }

    // Enable or disable eraser mode
    public void setEraserMode(boolean enabled) {
        this.eraserMode = enabled;
    }

    // Check if eraser mode is enabled
    public boolean isEraserMode() {
        return eraserMode;
    }

    // Load an image from a file and center it on the canvas
    public void loadImageFromFile(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        if (img != null) {
            uploadedImage = img;
            // Center the image initially
            imagePosition = new Point(
                (getWidth() - img.getWidth()) / 2,
                (getHeight() - img.getHeight()) / 2
            );
            repaint();
        }
    }

    // Load an image from a BufferedImage object and center it on the canvas
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw white background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw the uploaded image
        if (uploadedImage != null && imagePosition != null) {
            g.drawImage(uploadedImage, imagePosition.x, imagePosition.y, this);
            
            // Draw selection border if image is selected
            if (imageSelected) {
                Graphics2D g2d = (Graphics2D)g.create();
                g2d.setColor(Color.BLUE);
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                g2d.drawRect(imagePosition.x, imagePosition.y, uploadedImage.getWidth(), uploadedImage.getHeight());
                g2d.dispose();
            }
        }
        
        // Draw the drawing buffer (pen drawings) on top
        if (drawingBuffer != null) {
            g.drawImage(drawingBuffer, 
                0, 0, getWidth(), getHeight(),
                0, 0, drawingBuffer.getWidth(), drawingBuffer.getHeight(),
                this);
        }
    }

    // Get the preferred size of the canvas for layout purposes
    @Override
    public Dimension getPreferredSize() {
        return drawingSize;
    }

    // Set the size of the pen for drawing
    public void setPenSize(int size) {
        penSize = size;
    }

    // Get the current size of the pen
    public void setPenColor(Color color) {
        this.penColor = color;
    }

    // Get the current color of the pen
    public Color getPenColor() {
        return penColor;
    }

    // Clear the canvas, removing all drawings and uploaded images
    public void clearCanvas() {
        // Clear the drawing buffer
        Graphics2D g2d = drawingBuffer.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, drawingBuffer.getWidth(), drawingBuffer.getHeight());
        g2d.dispose();
        
        // Clear the uploaded image
        uploadedImage = null;
        imagePosition = null;
        imageSelected = false;
        
        repaint();
    }

    // Save the current canvas to a file in the specified format (e.g., PNG, JPEG)
    public void saveCanvasToFile(File file, String format) throws IOException {
        // Create a new image combining both layers
        BufferedImage combined = new BufferedImage(
            drawingBuffer.getWidth(),
            drawingBuffer.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = combined.createGraphics();
        
        // Draw white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, combined.getWidth(), combined.getHeight());
        
        // Draw the uploaded image if it exists
        if (uploadedImage != null && imagePosition != null) {
            g2d.drawImage(uploadedImage, imagePosition.x, imagePosition.y, null);
        }
        
        // Draw the pen drawings
        g2d.drawImage(drawingBuffer, 0, 0, null);
        
        g2d.dispose();
        ImageIO.write(combined, format, file);
    }
}