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

public class LeftCanvas extends JPanel {
    private BufferedImage selectedImage;
    private Point imagePosition = new Point(50, 50);
    private boolean draggingImage = false;
    private Point dragStartPoint;

    public LeftCanvas() {
        setBackground(Color.WHITE);
        setupDragAndDrop();
        setupMouseListeners();
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
                    JOptionPane.showMessageDialog(LeftCanvas.this, "Error importing image: " + ex.getMessage());
                }
                return false;
            }
        });
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedImage != null && 
                    e.getX() >= imagePosition.x && 
                    e.getX() <= imagePosition.x + selectedImage.getWidth() && 
                    e.getY() >= imagePosition.y && 
                    e.getY() <= imagePosition.y + selectedImage.getHeight()) {
                    draggingImage = true;
                    dragStartPoint = e.getPoint();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                draggingImage = false;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingImage) {
                    int dx = e.getX() - dragStartPoint.x;
                    int dy = e.getY() - dragStartPoint.y;
                    imagePosition = new Point(
                        imagePosition.x + dx,
                        imagePosition.y + dy
                    );
                    dragStartPoint = e.getPoint();
                    repaint();
                }
            }
        });
    }

    public void loadImageFromFile(File file) throws IOException {
        selectedImage = ImageIO.read(file);
        repaint();
    }

    public void saveCanvasToFile(File file, String format) throws IOException {
        BufferedImage image = captureCanvas();
        ImageIO.write(image, format, file);
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
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        if (selectedImage != null) {
            g.drawImage(selectedImage, imagePosition.x, imagePosition.y, this);
        }
        
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Image Composition Area - Drag images here", 10, 20);
    }
}