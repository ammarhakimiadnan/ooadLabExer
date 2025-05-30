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
import javax.swing.JColorChooser;

public class RightCanvas extends JPanel {
    private BufferedImage drawingBuffer;
    private Color penColor = Color.BLACK;
    private int penSize = 4;
    private Point previousPoint;
    private final Dimension drawingSize = new Dimension(800, 600);

    public RightCanvas() {
        setBackground(Color.WHITE);
        setToolTipText("Right-click to change pen color!");
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
        Graphics2D g2d = drawingBuffer.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, drawingBuffer.getWidth(), drawingBuffer.getHeight());
        g2d.dispose();
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                double xScale = (double)drawingBuffer.getWidth() / getWidth();
                double yScale = (double)drawingBuffer.getHeight() / getHeight();
                previousPoint = new Point(
                    (int)(e.getX() * xScale),
                    (int)(e.getY() * yScale)
                );
            }
            
            public void mouseReleased(MouseEvent e) {
                previousPoint = null;
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
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (previousPoint != null) {
                    double xScale = (double)drawingBuffer.getWidth() / getWidth();
                    double yScale = (double)drawingBuffer.getHeight() / getHeight();
                    
                    int x = (int)(e.getX() * xScale);
                    int y = (int)(e.getY() * yScale);

                    Graphics2D g2d = drawingBuffer.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(penColor);
                    g2d.setStroke(new BasicStroke(penSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawLine(previousPoint.x, previousPoint.y, x, y);
                    g2d.dispose();
                    
                    previousPoint = new Point(x, y);
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
                    JOptionPane.showMessageDialog(RightCanvas.this, "Error importing image: " + ex.getMessage());
                }
                return false;
            }
        });
    }

    public void saveCanvasToFile(File file, String format) throws IOException {
        BufferedImage image = captureCanvas();
        ImageIO.write(image, format, file);
    }

    public void loadImageFromFile(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        drawingBuffer = img;
        repaint();
    }

    public BufferedImage captureCanvas() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        paint(g2);
        g2.dispose();
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (drawingBuffer != null) {
            g.drawImage(drawingBuffer, 0, 0, getWidth(), getHeight(), 
                        0, 0, drawingSize.width, drawingSize.height, this);
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
        Graphics2D g2d = drawingBuffer.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, drawingBuffer.getWidth(), drawingBuffer.getHeight());
        g2d.dispose();
        repaint();
    }
}