import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class Toolbar extends JPanel implements ActionListener, ChangeListener {
    private JButton clearBtn;
    private JButton addAnimalBtn;
    private JButton addFlowerBtn;
    private JButton composeCanvasButton;
    private JButton saveButton;
    private JButton loadButton;
    private JButton saveRightButton;
    private JButton loadRightButton;
    private JButton rotateCanvasButton; // Add this line
    private JSlider penSizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 4);
    private JLabel colorLabel = new JLabel("  ");

    private RightCanvas rightCanvas;
    private LeftCanvas leftCanvas;

    public Toolbar(RightCanvas rightCanvas, LeftCanvas leftCanvas) {
        this.rightCanvas = rightCanvas;
        this.leftCanvas = leftCanvas;

        setLayout(new FlowLayout(FlowLayout.LEFT));

        // Load buttons with updated icon names
        clearBtn = createIconButton("resources/icons/clear.png", "Clear Drawing");
        addAnimalBtn = createIconButton("resources/icons/add animal.png", "Add Animal");
        addFlowerBtn = createIconButton("resources/icons/add flower.png", "Add Flower");
        composeCanvasButton = createIconButton("resources/icons/compose.png", "Compose Left Canvas");
        saveButton = createIconButton("resources/icons/save.png", "Save Left Canvas");
        loadButton = createIconButton("resources/icons/load image.png", "Load Image to Left Canvas");
        saveRightButton = createIconButton("resources/icons/save right.png", "Save Right Canvas");
        loadRightButton = createIconButton("resources/icons/upload right.png", "Load Image to Right Canvas");
        rotateCanvasButton = createIconButton("resources/icons/rotate.png", "Rotate Left Canvas 90°"); 

        // Configure pen size slider
        penSizeSlider.setMinorTickSpacing(1);
        penSizeSlider.setMajorTickSpacing(5);
        penSizeSlider.setPaintTicks(true);
        penSizeSlider.setPaintLabels(true);

        // Pen color preview box
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

        // Add listeners
        clearBtn.addActionListener(this);
        composeCanvasButton.addActionListener(this);
        saveButton.addActionListener(this);
        loadButton.addActionListener(this);
        saveRightButton.addActionListener(this);
        loadRightButton.addActionListener(this);
        penSizeSlider.addChangeListener(this);
        rotateCanvasButton.addActionListener(this); 
    }

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
            leftCanvas.rotateCanvas(Math.PI / 2); 
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == penSizeSlider && !penSizeSlider.getValueIsAdjusting()) {
            rightCanvas.setPenSize(penSizeSlider.getValue());
        }
    }

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