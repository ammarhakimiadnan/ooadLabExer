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

    public RightCanvas() {
        setBackground(Color.WHITE);
        setToolTipText("Right-click to change pen color! Double-click image to move it.");
        initializeDrawingBuffer();
        setupMouseListeners();
        setupDragAndDrop();
    }

    private void initializeDrawingBuffer() {
        drawingBuffer = new BufferedImage(
            drawingSize.width,
            drawingSize.height,
            BufferedImage.TYPE_INT_ARGB
        );
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
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
            
            public void mouseReleased(MouseEvent e) {
                previousPoint = null;
                dragStartPoint = null;
            }
            
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

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (imageSelected && dragStartPoint != null && uploadedImage != null) {
                    // Move the image (existing code)
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
                        // Erase by drawing with background color (only affects drawing buffer)
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

    public void setEraserMode(boolean enabled) {
        this.eraserMode = enabled;
    }

    public boolean isEraserMode() {
        return eraserMode;
    }

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw white background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw the uploaded image (on its own layer)
        if (uploadedImage != null && imagePosition != null) {
            g.drawImage(uploadedImage, imagePosition.x, imagePosition.y, this);
            
            // Draw selection border if image is selected
            if (imageSelected) {
                Graphics2D g2d = (Graphics2D)g.create();
                g2d.setColor(Color.BLUE);
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, 
                                BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                g2d.drawRect(imagePosition.x, imagePosition.y, 
                            uploadedImage.getWidth(), uploadedImage.getHeight());
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

    @Override
    public Dimension getPreferredSize() {
        return drawingSize;
    }

    public void setPenSize(int size) {
        penSize = size;
    }

    public void setPenColor(Color color) {
        this.penColor = color;
    }

    public Color getPenColor() {
        return penColor;
    }

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