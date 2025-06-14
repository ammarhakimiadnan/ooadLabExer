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

// The Toolbar class provides buttons and tools for interacting with LeftCanvas and RightCanvas
public class Toolbar extends JPanel implements ActionListener, ChangeListener {

    // Button declarations for various toolbar actions
    private JButton clearBtn;
    private JButton addAnimalBtn;
    private JButton addFlowerBtn;
    private JButton composeCanvasButton;
    private JButton saveButton;
    private JButton loadButton;
    private JButton saveRightButton;
    private JButton loadRightButton;
    private JButton rotateCanvasButton;
    private JButton deleteBtn;  // Delete selected image on LeftCanvas

    // Pen stroke slider and color label
    private JSlider penSizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 4);
    private JLabel colorLabel = new JLabel("  ");

    // Canvas references
    private RightCanvas rightCanvas;
    private LeftCanvas leftCanvas;

    // Constructor to initialize the toolbar and all its components
    public Toolbar(RightCanvas rightCanvas, LeftCanvas leftCanvas) {
        this.rightCanvas = rightCanvas;
        this.leftCanvas = leftCanvas;

        setLayout(new FlowLayout(FlowLayout.LEFT)); // Horizontal layout

        // Initialize icon-based buttons with tooltip text
        clearBtn = createIconButton("resources/icons/clear.png", "Clear Drawing");
        addAnimalBtn = createIconButton("resources/icons/add animal.png", "Insert Animal Image");
        addFlowerBtn = createIconButton("resources/icons/add flower.png", "Insert Flower Image");
        composeCanvasButton = createIconButton("resources/icons/compose.png", "Compose Left Canvas");
        saveButton = createIconButton("resources/icons/save.png", "Save Left Canvas");
        loadButton = createIconButton("resources/icons/load image.png", "Load Image to Left Canvas");
        saveRightButton = createIconButton("resources/icons/save right.png", "Save Right Canvas");
        loadRightButton = createIconButton("resources/icons/upload right.png", "Load Image to Right Canvas");
        rotateCanvasButton = createIconButton("resources/icons/rotate.png", "Rotate Left Canvas 90°");
        deleteBtn = createIconButton("resources/icons/delete.png", "Delete Selected Image");

        // Pen size slider configuration
        penSizeSlider.setMinorTickSpacing(1);
        penSizeSlider.setMajorTickSpacing(5);
        penSizeSlider.setPaintTicks(true);
        penSizeSlider.setPaintLabels(true);

        // Display the current pen color from RightCanvas
        colorLabel.setOpaque(true);
        colorLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        colorLabel.setBackground(rightCanvas.getPenColor());

        // Add components to toolbar
        add(clearBtn);
        add(addAnimalBtn);
        add(addFlowerBtn);
        add(penSizeSlider);
        add(colorLabel);
        add(composeCanvasButton);
        add(saveButton);
        add(loadButton);
        add(saveRightButton);
        add(loadRightButton);
        add(rotateCanvasButton);
        add(deleteBtn); // Add delete button

        // Register event listeners
        clearBtn.addActionListener(this);
        addAnimalBtn.addActionListener(this);
        addFlowerBtn.addActionListener(this);
        composeCanvasButton.addActionListener(this);
        saveButton.addActionListener(this);
        loadButton.addActionListener(this);
        saveRightButton.addActionListener(this);
        loadRightButton.addActionListener(this);
        rotateCanvasButton.addActionListener(this);
        deleteBtn.addActionListener(this);
        penSizeSlider.addChangeListener(this);
    }

    // Helper method to create a JButton with a scaled icon and tooltip
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

    // Respond to button actions
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clearBtn) {
            rightCanvas.clearCanvas();

        } else if (e.getSource() == composeCanvasButton) {
            CanvasComposer.composeAndShow(leftCanvas);

        } else if (e.getSource() == saveButton) {
            saveCanvas(leftCanvas);

        } else if (e.getSource() == loadButton) {
            loadImage(leftCanvas);

        } else if (e.getSource() == saveRightButton) {
            saveCanvas(rightCanvas);

        } else if (e.getSource() == loadRightButton) {
            loadImage(rightCanvas);

        } else if (e.getSource() == rotateCanvasButton) {
            leftCanvas.rotateCanvas(Math.PI / 2); // Rotate canvas by 90 degrees

        } else if (e.getSource() == addAnimalBtn || e.getSource() == addFlowerBtn) {
            insertImageFromDevice(); // Both buttons use same insertion logic

        } else if (e.getSource() == deleteBtn) {
            leftCanvas.deleteSelectedImage(); // Deletes currently selected image
        }
    }

    // Let the user choose an image from their device and insert into LeftCanvas
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

    // Respond to changes in pen size slider
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == penSizeSlider && !penSizeSlider.getValueIsAdjusting()) {
            rightCanvas.setPenSize(penSizeSlider.getValue());
        }
    }

    // Save canvas (left or right) to file
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

    // Load image file into canvas
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

