// Canvas for displaying, manipulating, and saving images with drag-and-drop support.
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.geom.Ellipse2D;

public class LeftCanvas extends JPanel {
    private static class CanvasImage {
        CreationItem creationItem;
        Point2D.Double position;
        
        // Constructor to initialize a CanvasImage with a CreationItem and its position
        CanvasImage(CreationItem item, int x, int y) {
            this.creationItem = item;
            this.position = new Point2D.Double(x, y);
        }
        
        // Method to get the center point of the image based on its position and scale
        Point2D.Double getCenter() {
            double w = creationItem.getImage().getWidth() * creationItem.getScale();
            double h = creationItem.getImage().getHeight() * creationItem.getScale();
            return new Point2D.Double(position.x + w / 2, position.y + h / 2);
        }
    }

    // Enum to define the types of handles for image manipulation
    private enum HandleType {
        NONE, MOVE, SCALE, FLIP_TOP, FLIP_BOTTOM, FLIP_LEFT, FLIP_RIGHT, ROTATE
    }

    private List<CanvasImage> images = new ArrayList<>(); // List to hold all images on the canvas
    private CanvasImage selectedImage = null;
    private HandleType activeHandle = HandleType.NONE;

    private Point dragStartPoint;                       // Point where the drag started
    private double dragStartRotation;                   // Rotation of the image at the start of the drag
    private double dragStartAngle;                      // Angle of the mouse at the start of the drag
    private double dragStartScaleDist;                  // Distance from the center of the image to the mouse at the start of the drag

    private double canvasRotation = 0;                  // Current rotation of the canvas in radians
    private final int HANDLE_SIZE = 10;                 // Size of the handles for image manipulation
    private final int ROTATE_HANDLE_OFFSET = 30;        // Offset for the rotate handle from the center of the image

    private Dimension canvasSize = new Dimension(400, 400); // Default size of the canvas
    private BufferedImage canvasBackground;                              // Background image for the canvas
    private Color outOfBoundsColor = new Color(240, 240, 240);     // Color for the area outside the canvas

    // Constructor to initialize the LeftCanvas with default settings
    public LeftCanvas() {
        setBackground(Color.WHITE);
        setupDragAndDrop();
        setupMouseListeners();
        updateCanvasSize();
    }

    // Method to insert an image into the canvas based on its type
    public void insertImage(BufferedImage image, String type) {
        CreationItem item;
        // Determine the type of CreationItem to create based on the provided type string
        switch (type.toLowerCase()) {
            case "animal":
                item = new AnimalItem(image);
                break;
            case "flower":
                item = new FlowerItem(image);
                break;
            default:
                item = new CustomImageItem(image);
        }
        addCreationItem(item);  // Add the CreationItem to the canvas
    }

    // Method to add a CreationItem to the canvas at the center
    private void addCreationItem(CreationItem item) {
        int x = (canvasSize.width - item.getImage().getWidth()) / 2;    // Calculate the x position to center the image
        int y = (canvasSize.height - item.getImage().getHeight()) / 2;  // Calculate the y position to center the image
        
        CanvasImage canvasImg = new CanvasImage(item, Math.max(0, x), Math.max(0, y)); // Create a new CanvasImage with the item and its position
        
        // Scale the image if it exceeds the canvas size
        if (item.getImage().getWidth() > canvasSize.width || item.getImage().getHeight() > canvasSize.height) {
            double scale = Math.min(
                (double)canvasSize.width / item.getImage().getWidth(),
                (double)canvasSize.height / item.getImage().getHeight()
            );
            item.scale(scale * 0.95);
        }
        
        images.add(canvasImg);  // Add the CanvasImage to the list of images
        repaint();              // Repaint the canvas to reflect the changes
    }

    // Method to rotate the entire canvas by a specified angle in radians
    public void rotateCanvas(double radians) {
        canvasRotation += radians;
        repaint();
    }

    // Method to load an image from a file and add it to the canvas
    public void loadImageFromFile(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        addImage(img);
    }

    // Method to delete the currently selected image from the canvas
    public void deleteSelectedImage() {
        if (selectedImage != null) {
            images.remove(selectedImage);
            selectedImage = null;
            repaint();
        }
    }

    // Method to save the current canvas as an image file in a specified format
    public void saveCanvasToFile(File file, String format) throws IOException {
        BufferedImage image = captureCanvas();
        ImageIO.write(image, format, file);
    }

    // Method to capture the current state of the canvas as a BufferedImage
    public BufferedImage captureCanvas() {
        BufferedImage image = new BufferedImage(canvasSize.width, canvasSize.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, canvasSize.width, canvasSize.height);
        
        for (CanvasImage img : images) {
            g2.drawImage(img.creationItem.getImage(), getTransformForImage(img), null);
        }
        
        g2.dispose();
        return image;
    }

    // Method to set the color for the area outside the canvas
    public void setOutOfBoundsColor(Color color) {
        this.outOfBoundsColor = color;
        repaint();
    }

    // Method to set the size of the canvas
    public void setCanvasSize(int width, int height) {
        canvasSize = new Dimension(width, height);
        updateCanvasSize();
    }

    // Method to clear all images from the canvas
    public void clearCanvas() {
        images.clear();
        selectedImage = null;
        repaint();
    }

    // Method to add an image to the canvas at the center
    private void addImage(BufferedImage img) {
        int x = (canvasSize.width - img.getWidth()) / 2;
        int y = (canvasSize.height - img.getHeight()) / 2;
        
        CreationItem item = new CustomImageItem(img);
        CanvasImage canvasImg = new CanvasImage(item, Math.max(0, x), Math.max(0, y));
        
        if (img.getWidth() > canvasSize.width || img.getHeight() > canvasSize.height) {
            double scale = Math.min(
                (double)canvasSize.width / img.getWidth(),
                (double)canvasSize.height / img.getHeight()
            );
            item.scale(scale * 0.95);
        }
        
        images.add(canvasImg);
        repaint();
    }

    // Method to set up drag-and-drop functionality for the canvas
    private void setupDragAndDrop() {
        setTransferHandler(new TransferHandler() {
            // Method to check if the dragged data can be imported
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            // Method to handle the import of files dropped onto the canvas
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
                    JOptionPane.showMessageDialog(LeftCanvas.this, "Error importing image: " + ex.getMessage());    // Show error message if import fails
                }
                return false;
            }
        });
    }

    // Method to set up mouse listeners for image manipulation
    private void setupMouseListeners() {
        // Mouse adapter to handle mouse events for image manipulation
        MouseAdapter adapter = new MouseAdapter() {
            private Point2D.Double toCanvasCoordinates(Point p) {
                int canvasX = (getWidth() - canvasSize.width) / 2;
                int canvasY = (getHeight() - canvasSize.height) / 2;
                return new Point2D.Double(p.x - canvasX, p.y - canvasY);
            }

            // Method to handle mouse pressed events for selecting and manipulating images
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                selectedImage = null;

                Point2D.Double canvasPoint = toCanvasCoordinates(e.getPoint());

                for (int i = images.size() - 1; i >= 0; i--) {
                    CanvasImage img = images.get(i);
                    HandleType handle = getHandleAt(canvasPoint, img);

                    if (handle != HandleType.NONE) {
                        selectedImage = img;
                        activeHandle = handle;
                        dragStartPoint = e.getPoint();

                        Point2D.Double center = selectedImage.getCenter();
                        double dx = e.getX() - center.x;
                        double dy = e.getY() - center.y;

                        // Handle the specific action based on the selected handle
                        switch (handle) {
                            case FLIP_LEFT:
                            case FLIP_RIGHT:
                                selectedImage.creationItem.flipHorizontal();
                                activeHandle = HandleType.NONE;
                                break;
                            case FLIP_TOP:
                            case FLIP_BOTTOM:
                                selectedImage.creationItem.flipVertical();
                                activeHandle = HandleType.NONE;
                                break;
                            case ROTATE:
                                dragStartRotation = selectedImage.creationItem.getRotation();
                                dragStartAngle = Math.atan2(dy, dx);
                                break;
                            case SCALE:
                                dragStartScaleDist = Math.sqrt(dx * dx + dy * dy);
                                break;
                            case MOVE:
                                break;
                            case NONE:
                                break;
                        }
                        repaint();
                        return;
                    }
                }
            }

            // Method to handle mouse released events to stop manipulation
            @Override
            public void mouseReleased(MouseEvent e) {
                activeHandle = HandleType.NONE;
            }

            // Method to handle mouse dragged events for manipulating the selected image
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedImage == null || activeHandle == HandleType.NONE) return;

                Point2D.Double canvasPoint = toCanvasCoordinates(e.getPoint());
                Point2D.Double center = selectedImage.getCenter();
                double dx = canvasPoint.x - center.x;
                double dy = canvasPoint.y - center.y;

                // Calculate the new position based on the active handle
                switch (activeHandle) {
                    case MOVE:
                        double moveDx = e.getX() - dragStartPoint.x;
                        double moveDy = e.getY() - dragStartPoint.y;
                        
                        double newX = selectedImage.position.x + moveDx;
                        double newY = selectedImage.position.y + moveDy;
                        
                        double scaledWidth = selectedImage.creationItem.getImage().getWidth() * selectedImage.creationItem.getScale();
                        double scaledHeight = selectedImage.creationItem.getImage().getHeight() * selectedImage.creationItem.getScale();
                        
                        newX = Math.max(0, Math.min(newX, canvasSize.width - scaledWidth));
                        newY = Math.max(0, Math.min(newY, canvasSize.height - scaledHeight));
                        
                        selectedImage.position.x = newX;
                        selectedImage.position.y = newY;
                        dragStartPoint = e.getPoint();
                        break;
                        
                    case ROTATE:
                        double currentAngle = Math.atan2(dy, dx);
                        selectedImage.creationItem.rotate(dragStartRotation + (currentAngle - dragStartAngle));
                        break;
                        
                    case SCALE:
                        double currentDist = Math.sqrt(dx * dx + dy * dy);
                        double scaleFactor = currentDist / dragStartScaleDist;
                        selectedImage.creationItem.scale(scaleFactor);
                        dragStartScaleDist = currentDist;
                        
                        double scaledWidth2 = selectedImage.creationItem.getImage().getWidth() * selectedImage.creationItem.getScale();
                        double scaledHeight2 = selectedImage.creationItem.getImage().getHeight() * selectedImage.creationItem.getScale();
                        
                        selectedImage.position.x = Math.max(0, Math.min(
                            selectedImage.position.x,
                            canvasSize.width - scaledWidth2
                        ));
                        selectedImage.position.y = Math.max(0, Math.min(
                            selectedImage.position.y,
                            canvasSize.height - scaledHeight2
                        ));
                        break;
                    default:
                        break;
                }
                repaint();
            }

            // Method to handle mouse moved events to change the cursor based on the active handle
            @Override
            public void mouseMoved(MouseEvent e) {
                Point2D.Double canvasPoint = toCanvasCoordinates(e.getPoint());

                if (selectedImage != null) {
                    HandleType handle = getHandleAt(canvasPoint, selectedImage);
                    switch (handle) {
                        case SCALE:
                            setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                            break;
                        case ROTATE:
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            break;
                        case MOVE:
                            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            break;
                        case FLIP_TOP:
                        case FLIP_BOTTOM:
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            break;
                        case FLIP_LEFT:
                        case FLIP_RIGHT:
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            break;
                        default:
                            setCursor(Cursor.getDefaultCursor());
                    }
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        setFocusable(true);
    }

    // Method to get the transformation for an image based on its position, rotation, and scale
    private AffineTransform getTransformForImage(CanvasImage img) {
        AffineTransform at = new AffineTransform();
        Point2D.Double center = img.getCenter();

        at.translate(img.position.x, img.position.y);
        at.rotate(img.creationItem.getRotation(), center.x - img.position.x, center.y - img.position.y);
        double scaleX = img.creationItem.isFlippedH() ? -img.creationItem.getScale() : img.creationItem.getScale();
        double scaleY = img.creationItem.isFlippedV() ? -img.creationItem.getScale() : img.creationItem.getScale();
        at.scale(scaleX, scaleY);

        return at;
    }

    // Method to get the handle type at a specific point in the canvas
    private HandleType getHandleAt(Point2D.Double p, CanvasImage img) {
        Shape bounds = getTransformForImage(img).createTransformedShape(new Rectangle(0, 0, img.creationItem.getImage().getWidth(), img.creationItem.getImage().getHeight()));

        // Check if the point is within the bounds of the image
        if (!bounds.contains(p)) {
            Point2D rot = getTransformedHandle(img, HandleType.ROTATE);
            if (rot != null && rot.distance(p) <= HANDLE_SIZE) {
                return HandleType.ROTATE;
            }
            return HandleType.NONE;
        }

        // Check for each handle type if the point is close enough to be considered active
        for (HandleType type : new HandleType[]{
                HandleType.SCALE, HandleType.FLIP_TOP, HandleType.FLIP_BOTTOM,
                HandleType.FLIP_LEFT, HandleType.FLIP_RIGHT}) {
            Point2D handle = getTransformedHandle(img, type);
            if (type == HandleType.SCALE) {
                for (Point2D corner : getTransformedCorners(img)) {
                    if (corner.distance(p) <= HANDLE_SIZE) return HandleType.SCALE;
                }
            } else if (handle != null && handle.distance(p) <= HANDLE_SIZE) {
                return type;
            }
        }

        return HandleType.MOVE;
    }

    // Method to get the transformed corners of an image based on its position, rotation, and scale
    private Point2D[] getTransformedCorners(CanvasImage img) {
        double w = img.creationItem.getImage().getWidth();
        double h = img.creationItem.getImage().getHeight();
        Point2D[] corners = {
                new Point2D.Double(0, 0), new Point2D.Double(w, 0),
                new Point2D.Double(w, h), new Point2D.Double(0, h)
        };
        getTransformForImage(img).transform(corners, 0, corners, 0, 4);
        return corners;
    }

    // Method to get the transformed position of a handle based on its type
    private Point2D getTransformedHandle(CanvasImage img, HandleType type) {
        double w = img.creationItem.getImage().getWidth();
        double h = img.creationItem.getImage().getHeight();
        Point2D.Double point = new Point2D.Double();

        // Set the point based on the handle type
        switch (type) {
            case FLIP_TOP: point.setLocation(w / 2, 0); break;
            case FLIP_BOTTOM: point.setLocation(w / 2, h); break;
            case FLIP_LEFT: point.setLocation(0, h / 2); break;
            case FLIP_RIGHT: point.setLocation(w, h / 2); break;
            case ROTATE: point.setLocation(w / 2, -ROTATE_HANDLE_OFFSET / img.creationItem.getScale()); break;
            default: return null;
        }

        getTransformForImage(img).transform(point, point);
        return point;
    }

    // Method to update the canvas size and create a new background image
    private void updateCanvasSize() {
        setPreferredSize(canvasSize);
        canvasBackground = new BufferedImage(canvasSize.width, canvasSize.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvasBackground.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvasSize.width, canvasSize.height);
        g2d.dispose();
        revalidate();
        repaint();
    }

    // Method to get the current canvas size
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        int canvasX = (getWidth() - canvasSize.width) / 2;
        int canvasY = (getHeight() - canvasSize.height) / 2;
        
        g2.setColor(outOfBoundsColor);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.setColor(Color.WHITE);
        g2.fillRect(canvasX, canvasY, canvasSize.width, canvasSize.height);
        
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(canvasX, canvasY, canvasSize.width, canvasSize.height);
        
        g2.translate(canvasX + canvasSize.width / 2.0, canvasY + canvasSize.height / 2.0);
        g2.rotate(canvasRotation);
        g2.translate(-canvasSize.width / 2.0, -canvasSize.height / 2.0);

        for (CanvasImage img : images) {
            g2.drawImage(img.creationItem.getImage(), getTransformForImage(img), null);
        }

        if (selectedImage != null) {
            AffineTransform original = g2.getTransform();
            Point2D[] corners = getTransformedCorners(selectedImage);

            g2.setColor(Color.RED);
            for (int i = 0; i < 4; i++) {
                g2.drawLine((int) corners[i].getX(), (int) corners[i].getY(),
                            (int) corners[(i + 1) % 4].getX(), (int) corners[(i + 1) % 4].getY());
            }

            Point2D topMid = getTransformedHandle(selectedImage, HandleType.FLIP_TOP);
            Point2D rot = getTransformedHandle(selectedImage, HandleType.ROTATE);
            if (topMid != null && rot != null) {
                g2.setColor(Color.BLACK);
                g2.drawLine((int) topMid.getX(), (int) topMid.getY(), (int) rot.getX(), (int) rot.getY());
            }

            List<Point2D> handlePoints = new ArrayList<>(List.of(corners));
            handlePoints.add(getTransformedHandle(selectedImage, HandleType.FLIP_TOP));
            handlePoints.add(getTransformedHandle(selectedImage, HandleType.FLIP_BOTTOM));
            handlePoints.add(getTransformedHandle(selectedImage, HandleType.FLIP_LEFT));
            handlePoints.add(getTransformedHandle(selectedImage, HandleType.FLIP_RIGHT));
            handlePoints.add(getTransformedHandle(selectedImage, HandleType.ROTATE));

            for (Point2D pt : handlePoints) {
                if (pt == null) continue;
                Ellipse2D circle = new Ellipse2D.Double(pt.getX() - HANDLE_SIZE / 2.0, pt.getY() - HANDLE_SIZE / 2.0, HANDLE_SIZE, HANDLE_SIZE);
                g2.setColor(Color.WHITE);
                g2.fill(circle);
                g2.setColor(Color.BLACK);
                g2.draw(circle);
            }
            g2.setTransform(original);
        }
        g2.dispose();
    }

    // Method to check if a point is within the bounds of the canvas
    private boolean isWithinCanvas(Point2D.Double point, CanvasImage img) {
        double scaledWidth = img.creationItem.getImage().getWidth() * img.creationItem.getScale();
        double scaledHeight = img.creationItem.getImage().getHeight() * img.creationItem.getScale();
        
        return point.x >= 0 && 
            point.y >= 0 && 
            (point.x + scaledWidth) <= canvasSize.width && 
            (point.y + scaledHeight) <= canvasSize.height;
    }
}