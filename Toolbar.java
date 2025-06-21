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
    private JButton addAnimalBtn, addFlowerBtn, loadButton, saveButton, composeCanvasButton, rotateCanvasButton, deleteBtn, newCanvasButton;

    // ===== RightCanvas Buttons =====
    private JSlider penSizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 4);
    private JButton clearBtn, loadRightButton, saveRightButton, colorButton, eraserButton;

    private RightCanvas rightCanvas;
    private LeftCanvas leftCanvas;

    // Define paths to the animal and flower folders
    private static final String ANIMAL_FOLDER_PATH = "animal";
    private static final String FLOWER_FOLDER_PATH = "flower";
    private static final String PICTURES_FOLDER_PATH = System.getProperty("user.home") + File.separator + "Pictures";

    public Toolbar(RightCanvas rightCanvas, LeftCanvas leftCanvas) {
        this.rightCanvas = rightCanvas;
        this.leftCanvas = leftCanvas;

        // Use BorderLayout to split left/right control groups
        setLayout(new BorderLayout());

        // ==== LEFT Panel ====
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addAnimalBtn = createIconButton("resources/icons/add_animal.png", "Insert Animal Image");
        addFlowerBtn = createIconButton("resources/icons/add_flower.png", "Insert Flower Image");
        loadButton = createIconButton("resources/icons/load_image.png", "Load Image to Left Canvas");
        saveButton = createIconButton("resources/icons/save.png", "Save Left Canvas");
        composeCanvasButton = createIconButton("resources/icons/compose.png", "Compose Left Canvas");
        rotateCanvasButton = createIconButton("resources/icons/rotate.png", "Rotate Left Canvas 90°");
        deleteBtn = createIconButton("resources/icons/delete.png", "Delete Selected Image");
        newCanvasButton = createIconButton("resources/icons/new.png", "Create New Canvas");

        leftPanel.add(addAnimalBtn);
        leftPanel.add(addFlowerBtn);
        leftPanel.add(loadButton);
        leftPanel.add(saveButton);
        leftPanel.add(composeCanvasButton);
        leftPanel.add(rotateCanvasButton);
        leftPanel.add(deleteBtn);
        leftPanel.add(newCanvasButton);

        // ==== RIGHT Panel ====
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clearBtn = createIconButton("resources/icons/clear.png", "Clear Right Canvas");
        loadRightButton = createIconButton("resources/icons/upload_right.png", "Load Image to Right Canvas");
        saveRightButton = createIconButton("resources/icons/save_right.png", "Save Right Canvas");

        penSizeSlider.setMinorTickSpacing(1);
        penSizeSlider.setMajorTickSpacing(5);
        penSizeSlider.setPaintTicks(true);
        penSizeSlider.setPaintLabels(true);

        // Create color button instead of label
        colorButton = new JButton() {
            @Override
            public void paintComponent(Graphics g) {
                // Only paint the icon, no button decoration
                if (getIcon() != null) {
                    getIcon().paintIcon(this, g, 0, 0);
                }
            }
        };
        colorButton.setPreferredSize(new Dimension(32, 32));
        colorButton.setContentAreaFilled(false);
        colorButton.setBorderPainted(false);
        colorButton.setFocusPainted(false);
        colorButton.setBorder(BorderFactory.createEmptyBorder());

        updateColorButtonIcon(rightCanvas.getPenColor());
        colorButton.addActionListener(_ -> {
            JColorChooser chooser = new JColorChooser(rightCanvas.getPenColor());
            JDialog dialog = JColorChooser.createDialog(
                null,  // No parent - will center on screen
                "Choose Pen Color",
                true,
                chooser,
                _ -> {
                    rightCanvas.setPenColor(chooser.getColor());
                    updateColorButtonIcon(chooser.getColor());
                },
                null
            );
            dialog.setLocationRelativeTo(null);  // Center on screen
            dialog.setVisible(true);
        });

        eraserButton = createIconButton("resources/icons/eraser.png", "Toggle Eraser");
        eraserButton.addActionListener(this);

        rightPanel.add(penSizeSlider);
        rightPanel.add(colorButton);
        rightPanel.add(eraserButton);
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
        newCanvasButton.addActionListener(this);

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

    private void updateColorButtonIcon(Color color) {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw color circle
        g2d.setColor(color);
        g2d.fillOval(4, 4, 24, 24);  // Slightly smaller than button size
        
        // Optional: Add border
        g2d.setColor(color.darker());
        g2d.drawOval(4, 4, 24, 24);
        
        g2d.dispose();
        colorButton.setIcon(new ImageIcon(image));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        // ==== LeftCanvas actions ====
        if (src == addAnimalBtn) {
            insertImageFromDevice(ANIMAL_FOLDER_PATH, "animal");
        } else if (src == addFlowerBtn) {
            insertImageFromDevice(FLOWER_FOLDER_PATH, "flower");
        } else if (src == loadButton) {
            insertImageFromDevice(PICTURES_FOLDER_PATH, "custom");
        } else if (src == saveButton) {
            saveCanvas(leftCanvas);
        } else if (src == composeCanvasButton) {
            CanvasComposer.composeAndShow(leftCanvas);
        } else if (src == rotateCanvasButton) {
            leftCanvas.rotateCanvas(Math.PI / 2);
        } else if (src == deleteBtn) {
            leftCanvas.deleteSelectedImage();
        } else if (src == newCanvasButton) {
            createNewCanvas();
        }
        // ==== RightCanvas actions ====
        else if (src == clearBtn) {
            rightCanvas.clearCanvas();
        } else if (src == loadRightButton) {
            // Changed this to use the right canvas loading method
            loadImage(rightCanvas);
        } else if (src == saveRightButton) {
            saveCanvas(rightCanvas);
        } else if (src == eraserButton) {
            rightCanvas.setEraserMode(!rightCanvas.isEraserMode());
            eraserButton.setSelected(rightCanvas.isEraserMode());
            
            if (rightCanvas.isEraserMode()) {
                eraserButton.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            } else {
                eraserButton.setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }

    /**
     * Opens file chooser to insert an image into LeftCanvas from a specified folder.
     * @param folderPath The path to the folder containing the images (e.g., animal or flower folder).
     */
    private void insertImageFromDevice(String folderPath, String type) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Image to Insert");
        
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            fileChooser.setCurrentDirectory(folder);
        } else {
            JOptionPane.showMessageDialog(this, "Folder not found: " + folderPath);
            return;
        }

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    leftCanvas.insertImage(image, type);
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
        fileChooser.setCurrentDirectory(new File(PICTURES_FOLDER_PATH)); // Set default to Pictures folder

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                if (canvas instanceof LeftCanvas) {
                    ((LeftCanvas) canvas).loadImageFromFile(file);
                } else if (canvas instanceof RightCanvas) {
                    ((RightCanvas) canvas).loadImageFromFile(file);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error loading: " + ex.getMessage());
            }
        }
    }

    private void createNewCanvas() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField widthField = new JTextField("400");
        JTextField heightField = new JTextField("400");
        
        panel.add(new JLabel("Width:"));
        panel.add(widthField);
        panel.add(new JLabel("Height:"));
        panel.add(heightField);
        
        // Get the main frame to use as parent
        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(this);
        
        int result = JOptionPane.showConfirmDialog(
            frame,  // Changed from 'this' to 'frame'
            panel,
            "Create New Canvas",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                int width = Integer.parseInt(widthField.getText());
                int height = Integer.parseInt(heightField.getText());
                if (width > 0 && height > 0) {
                    leftCanvas.setCanvasSize(width, height);
                    leftCanvas.clearCanvas();
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter positive values");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid numbers");
            }
        }
    }
}