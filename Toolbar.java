import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Toolbar with LeftCanvas controls aligned to the far left
 * and RightCanvas controls pushed to the far right.
 */
public class Toolbar extends JPanel implements ActionListener, ChangeListener {
    // ===== LeftCanvas Buttons =====
    private JButton addAnimalBtn, addFlowerBtn, loadButton, saveButton, composeCanvasButton, rotateCanvasButton, deleteBtn, resizeButton;
    private JTextField widthField, heightField;

    // ===== RightCanvas Buttons =====
    private JSlider penSizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 4);
    private JLabel colorLabel = new JLabel("  ");
    private JButton clearBtn, loadRightButton, saveRightButton;

    private RightCanvas rightCanvas;
    private LeftCanvas leftCanvas;

    public Toolbar(RightCanvas rightCanvas, LeftCanvas leftCanvas) {
        this.rightCanvas = rightCanvas;
        this.leftCanvas = leftCanvas;

        // Use BorderLayout to split left/right control groups
        setLayout(new BorderLayout());

        // ==== LEFT Panel ====
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addAnimalBtn = createIconButton("resources/icons/add animal.png", "Insert Animal Image");
        addFlowerBtn = createIconButton("resources/icons/add flower.png", "Insert Flower Image");
        loadButton = createIconButton("resources/icons/load image.png", "Load Image to Left Canvas");
        saveButton = createIconButton("resources/icons/save.png", "Save Left Canvas");
        composeCanvasButton = createIconButton("resources/icons/compose.png", "Compose Left Canvas");
        rotateCanvasButton = createIconButton("resources/icons/rotate.png", "Rotate Left Canvas 90°");
        deleteBtn = createIconButton("resources/icons/delete.png", "Delete Selected Image");

        leftPanel.add(addAnimalBtn);
        leftPanel.add(addFlowerBtn);
        leftPanel.add(loadButton);
        leftPanel.add(saveButton);
        leftPanel.add(composeCanvasButton);
        leftPanel.add(rotateCanvasButton);
        leftPanel.add(deleteBtn);

        leftPanel.add(new JLabel("Width:"));
        widthField = new JTextField("400", 5);
        leftPanel.add(widthField);
        
        leftPanel.add(new JLabel("Height:"));
        heightField = new JTextField("400", 5);
        leftPanel.add(heightField);
        
        resizeButton = new JButton("Resize");
        resizeButton.addActionListener(this);
        leftPanel.add(resizeButton);

        // ==== RIGHT Panel ====
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clearBtn = createIconButton("resources/icons/clear.png", "Clear Right Canvas");
        loadRightButton = createIconButton("resources/icons/upload right.png", "Load Image to Right Canvas");
        saveRightButton = createIconButton("resources/icons/save right.png", "Save Right Canvas");

        penSizeSlider.setMinorTickSpacing(1);
        penSizeSlider.setMajorTickSpacing(5);
        penSizeSlider.setPaintTicks(true);
        penSizeSlider.setPaintLabels(true);

        Image img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(rightCanvas.getPenColor());
        g.fillRect(0, 0, 32, 32);
        colorLabel.setIcon(new ImageIcon(img));

        rightPanel.add(penSizeSlider);
        rightPanel.add(colorLabel);
        rightPanel.add(clearBtn);
        rightPanel.add(loadRightButton);
        rightPanel.add(saveRightButton);

        // Add both sections to the toolbar
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        // ===== Register Listeners =====
        addAnimalBtn.addActionListener(this);
        addFlowerBtn.addActionListener(this);
        loadButton.addActionListener(this);
        saveButton.addActionListener(this);
        composeCanvasButton.addActionListener(this);
        rotateCanvasButton.addActionListener(this);
        deleteBtn.addActionListener(this);

        clearBtn.addActionListener(this);
        loadRightButton.addActionListener(this);
        saveRightButton.addActionListener(this);

        penSizeSlider.addChangeListener(this);
    }

    /**
     * Creates an icon button with tooltip and styling.
     */
    private JButton createIconButton(String path, String tooltip) {
        ImageIcon icon = new ImageIcon(path);
        Image scaled = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        JButton button = new JButton(new ImageIcon(scaled));
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(40, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        // ==== LeftCanvas actions ====
        if (src == addAnimalBtn || src == addFlowerBtn) {
            insertImageFromDevice();
        } else if (src == loadButton) {
            loadImage(leftCanvas);
        } else if (src == saveButton) {
            saveCanvas(leftCanvas);
        } else if (src == composeCanvasButton) {
            CanvasComposer.composeAndShow(leftCanvas);
        } else if (src == rotateCanvasButton) {
            leftCanvas.rotateCanvas(Math.PI / 2);
        } else if (src == deleteBtn) {
            leftCanvas.deleteSelectedImage();
        } else if (src == resizeButton) {
            try {
                int width = Integer.parseInt(widthField.getText());
                int height = Integer.parseInt(heightField.getText());
                if (width > 0 && height > 0) {
                    leftCanvas.setCanvasSize(width, height);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter positive values");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers");
            }
        }

        // ==== RightCanvas actions ====
        else if (src == clearBtn) {
            rightCanvas.clearCanvas();
        } else if (src == loadRightButton) {
            loadImage(rightCanvas);
        } else if (src == saveRightButton) {
            saveCanvas(rightCanvas);
        }
    }

    public void setOutOfBoundsColor(Color color) {
        leftCanvas.setOutOfBoundsColor(color);
    }

    /**
     * Opens file chooser to insert an image into LeftCanvas.
     */
    private void insertImageFromDevice() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Image to Insert");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    leftCanvas.insertImage(image);
                } else {
                    JOptionPane.showMessageDialog(this, "Unsupported image format.");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage());
            }
        }
    }

    /**
     * Adjusts pen size for RightCanvas drawing.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == penSizeSlider && !penSizeSlider.getValueIsAdjusting()) {
            rightCanvas.setPenSize(penSizeSlider.getValue());
        }
    }

    /**
     * Shared save logic for both canvases.
     */
    private void saveCanvas(JPanel canvas) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Canvas As");

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String name = file.getName().toLowerCase();
            String format = name.endsWith(".jpg") ? "jpg" : "png";
            if (!name.endsWith("." + format)) {
                file = new File(file.getAbsolutePath() + "." + format);
            }

            try {
                if (canvas instanceof LeftCanvas) {
                    ((LeftCanvas) canvas).saveCanvasToFile(file, format);
                } else if (canvas instanceof RightCanvas) {
                    ((RightCanvas) canvas).saveCanvasToFile(file, format);
                }
                JOptionPane.showMessageDialog(null, "Canvas saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error saving: " + ex.getMessage());
            }
        }
    }

    /**
     * Shared image loader for both canvases.
     */
    private void loadImage(JPanel canvas) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Image");

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                if (canvas instanceof LeftCanvas) {
                    ((LeftCanvas) canvas).loadImageFromFile(file);
                } else if (canvas instanceof RightCanvas) {
                    ((RightCanvas) canvas).loadImageFromFile(file);
                }
                JOptionPane.showMessageDialog(null, "Image loaded successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error loading: " + ex.getMessage());
            }
        }
    }
}



