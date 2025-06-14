import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
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
        Point2D.Double position; 
        double rotation;
        double scale;
        boolean flipH, flipV;

        CanvasImage(BufferedImage img, Point pos) {
            image = img;
            position = new Point2D.Double(pos.x, pos.y);
            rotation = 0;
            scale = 1.0;
            flipH = false;
            flipV = false;
        }

        Point2D.Double getCenter() {
            double w = image.getWidth() * scale;
            double h = image.getHeight() * scale;
            return new Point2D.Double(position.x + w / 2, position.y + h / 2);
        }
    }

    private enum HandleType { NONE, MOVE, SCALE, FLIP_TOP, FLIP_BOTTOM, FLIP_LEFT, FLIP_RIGHT, ROTATE }

    private List<CanvasImage> images = new ArrayList<>();
    private CanvasImage selectedImage = null;
    private HandleType activeHandle = HandleType.NONE;
    
    private Point dragStartPoint;
    private double dragStartRotation;
    private double dragStartAngle;
    private double dragStartScaleDist;

    private double canvasRotation = 0;
    private final int HANDLE_SIZE = 10;
    private final int ROTATE_HANDLE_OFFSET = 30; 

    public LeftCanvas() {
        setBackground(Color.WHITE);
        setupDragAndDrop();
        setupMouseListeners();
    }

    // Adds an image to the canvas at default position (used internally)
    public void addImage(BufferedImage img) {
        images.add(new CanvasImage(img, new Point(50, 50)));
        repaint();
    }

    // ✅ Public method for Toolbar to insert user-selected images
    public void insertImage(BufferedImage image) {
        addImage(image); // Calls the same logic as file drag/drop or load
    }

    // Rotates the entire canvas
    public void rotateCanvas(double radians) {
        canvasRotation += radians;
        repaint();
    }

    // Loads an image from file (e.g., via load button or drag-and-drop)
    public void loadImageFromFile(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        addImage(img);
    }

    private void setupDragAndDrop() {
        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    Transferable t = support.getTransferable();
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

    private void setupMouseListeners() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedImage = null;
                for (int i = images.size() - 1; i >= 0; i--) {
                    CanvasImage img = images.get(i);
                    HandleType handle = getHandleAt(e.getPoint(), img);

                    if (handle != HandleType.NONE) {
                        selectedImage = img;
                        activeHandle = handle;
                        dragStartPoint = e.getPoint();

                        Point2D.Double center = selectedImage.getCenter();
                        double dx = e.getX() - center.x;
                        double dy = e.getY() - center.y;

                        switch (handle) {
                            case FLIP_LEFT:
                            case FLIP_RIGHT:
                                selectedImage.flipH = !selectedImage.flipH;
                                activeHandle = HandleType.NONE;
                                break;
                            case FLIP_TOP:
                            case FLIP_BOTTOM:
                                selectedImage.flipV = !selectedImage.flipV;
                                activeHandle = HandleType.NONE;
                                break;
                            case ROTATE:
                                dragStartRotation = selectedImage.rotation;
                                dragStartAngle = Math.atan2(dy, dx);
                                break;
                            case SCALE:
                                dragStartScaleDist = Math.sqrt(dx * dx + dy * dy);
                                break;
                            default:
                                break;
                        }
                        repaint();
                        return;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                activeHandle = HandleType.NONE;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedImage == null || activeHandle == HandleType.NONE) return;

                Point2D.Double center = selectedImage.getCenter();
                double dx = e.getX() - center.x;
                double dy = e.getY() - center.y;

                switch (activeHandle) {
                    case MOVE:
                        double moveDx = e.getX() - dragStartPoint.x;
                        double moveDy = e.getY() - dragStartPoint.y;
                        selectedImage.position.x += moveDx;
                        selectedImage.position.y += moveDy;
                        dragStartPoint = e.getPoint();
                        break;
                    case ROTATE:
                        double currentAngle = Math.atan2(dy, dx);
                        selectedImage.rotation = dragStartRotation + (currentAngle - dragStartAngle);
                        break;
                    case SCALE:
                        double currentDist = Math.sqrt(dx * dx + dy * dy);
                        double scaleFactor = currentDist / dragStartScaleDist;
                        selectedImage.scale *= scaleFactor;
                        selectedImage.scale = Math.max(0.1, Math.min(10.0, selectedImage.scale));
                        dragStartScaleDist = currentDist;
                        break;
                    default:
                        break;
                }
                repaint();
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        setFocusable(true);
    }

    private AffineTransform getTransformForImage(CanvasImage img) {
        AffineTransform at = new AffineTransform();
        Point2D.Double center = img.getCenter();

        at.translate(img.position.x, img.position.y);
        at.rotate(img.rotation, center.x - img.position.x, center.y - img.position.y);
        double scaleX = img.flipH ? -img.scale : img.scale;
        double scaleY = img.flipV ? -img.scale : img.scale;
        at.scale(scaleX, scaleY);

        return at;
    }

    private HandleType getHandleAt(Point p, CanvasImage img) {
        Shape transformedBounds = getTransformForImage(img).createTransformedShape(
                new Rectangle(0, 0, img.image.getWidth(), img.image.getHeight()));

        if (!transformedBounds.contains(p)) {
            Point2D transformedRotHandle = getTransformedHandle(img, HandleType.ROTATE);
            if (transformedRotHandle.distance(p) <= HANDLE_SIZE) {
                return HandleType.ROTATE;
            }
            return HandleType.NONE;
        }

        HandleType[] checkOrder = {
            HandleType.SCALE, HandleType.FLIP_TOP, HandleType.FLIP_BOTTOM,
            HandleType.FLIP_LEFT, HandleType.FLIP_RIGHT
        };

        for (HandleType type : checkOrder) {
            Point2D handlePoint = getTransformedHandle(img, type);
            if (type == HandleType.SCALE) {
                Point2D[] corners = getTransformedCorners(img);
                for (Point2D corner : corners) {
                    if (corner.distance(p) <= HANDLE_SIZE) return HandleType.SCALE;
                }
            } else {
                if (handlePoint.distance(p) <= HANDLE_SIZE) return type;
            }
        }

        return HandleType.MOVE;
    }

    private Point2D[] getTransformedCorners(CanvasImage img) {
        double w = img.image.getWidth();
        double h = img.image.getHeight();
        Point2D[] corners = {
            new Point2D.Double(0, 0),
            new Point2D.Double(w, 0),
            new Point2D.Double(w, h),
            new Point2D.Double(0, h)
        };
        getTransformForImage(img).transform(corners, 0, corners, 0, 4);
        return corners;
    }

    private Point2D getTransformedHandle(CanvasImage img, HandleType type) {
        double w = img.image.getWidth();
        double h = img.image.getHeight();
        Point2D.Double handlePos = new Point2D.Double();

        switch (type) {
            case FLIP_TOP:    handlePos.setLocation(w / 2, 0); break;
            case FLIP_BOTTOM: handlePos.setLocation(w / 2, h); break;
            case FLIP_LEFT:   handlePos.setLocation(0, h / 2); break;
            case FLIP_RIGHT:  handlePos.setLocation(w, h / 2); break;
            case ROTATE:      handlePos.setLocation(w / 2, -ROTATE_HANDLE_OFFSET / img.scale); break;
            default: return null;
        }

        getTransformForImage(img).transform(handlePos, handlePos);
        return handlePos;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.rotate(canvasRotation, getWidth() / 2.0, getHeight() / 2.0);

        for (CanvasImage img : images) {
            g2.drawImage(img.image, getTransformForImage(img), null);
        }

        if (selectedImage != null) {
            AffineTransform originalTransform = g2.getTransform();
            Point2D[] corners = getTransformedCorners(selectedImage);

            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(1));
            for (int i = 0; i < 4; i++) {
                g2.drawLine((int) corners[i].getX(), (int) corners[i].getY(),
                            (int) corners[(i + 1) % 4].getX(), (int) corners[(i + 1) % 4].getY());
            }

            List<Point2D> handlePoints = new ArrayList<>(List.of(corners));
            handlePoints.add(getTransformedHandle(selectedImage, HandleType.FLIP_TOP));
            handlePoints.add(getTransformedHandle(selectedImage, HandleType.FLIP_BOTTOM));
            handlePoints.add(getTransformedHandle(selectedImage, HandleType.FLIP_LEFT));
            handlePoints.add(getTransformedHandle(selectedImage, HandleType.FLIP_RIGHT));
            handlePoints.add(getTransformedHandle(selectedImage, HandleType.ROTATE));

            Point2D topMid = getTransformedHandle(selectedImage, HandleType.FLIP_TOP);
            Point2D rotHandle = getTransformedHandle(selectedImage, HandleType.ROTATE);
            if (topMid != null && rotHandle != null) {
                g2.setColor(Color.BLACK);
                g2.drawLine((int) topMid.getX(), (int) topMid.getY(), (int) rotHandle.getX(), (int) rotHandle.getY());
            }

            for (Point2D pt : handlePoints) {
                if (pt == null) continue;
                Ellipse2D handle = new Ellipse2D.Double(pt.getX() - HANDLE_SIZE / 2.0,
                                                        pt.getY() - HANDLE_SIZE / 2.0,
                                                        HANDLE_SIZE, HANDLE_SIZE);
                g2.setColor(Color.WHITE);
                g2.fill(handle);
                g2.setColor(Color.BLACK);
                g2.draw(handle);
            }
            g2.setTransform(originalTransform);
        }
        g2.dispose();
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
}
