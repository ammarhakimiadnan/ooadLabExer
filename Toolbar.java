import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;

public class Toolbar extends JPanel implements ActionListener, ChangeListener {
    private JButton clearBtn = new JButton("Clear");
    private JSlider penSizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 4);
    private JLabel colorLabel = new JLabel("  ");
    private JButton composeCanvasButton = new JButton("Compose");
    private JButton saveButton = new JButton("Save Canvas");
    private JButton loadButton = new JButton("Load Image");
    private JButton saveRightButton = new JButton("Save Right Canvas");
    private JButton loadRightButton = new JButton("Load Image to Right");
    
    private RightCanvas rightCanvas;
    private LeftCanvas leftCanvas;

    public Toolbar(RightCanvas rightCanvas, LeftCanvas leftCanvas) {
        this.rightCanvas = rightCanvas;
        this.leftCanvas = leftCanvas;
        
        setLayout(new FlowLayout(FlowLayout.LEFT));
        
        // Configure components
        penSizeSlider.setMinorTickSpacing(1);
        penSizeSlider.setMajorTickSpacing(5);
        penSizeSlider.setPaintTicks(true);
        penSizeSlider.setPaintLabels(true);
        
        colorLabel.setOpaque(true);
        colorLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        colorLabel.setBackground(rightCanvas.getPenColor());
        
        // Add components
        add(clearBtn);
        add(new JButton("Add Animal"));
        add(new JButton("Add Flower"));
        add(penSizeSlider);
        add(colorLabel);
        add(composeCanvasButton);
        add(saveButton);
        add(loadButton);
        add(saveRightButton);
        add(loadRightButton);
        
        // Add listeners
        clearBtn.addActionListener(this);
        penSizeSlider.addChangeListener(this);
        composeCanvasButton.addActionListener(this);
        saveButton.addActionListener(this);
        loadButton.addActionListener(this);
        saveRightButton.addActionListener(this);
        loadRightButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clearBtn) {
            rightCanvas.clearCanvas();
        } 
        else if (e.getSource() == composeCanvasButton) {
            CanvasComposer.composeAndShow(leftCanvas);
        }
        else if (e.getSource() == saveButton) {
            saveCanvas(leftCanvas);
        }
        else if (e.getSource() == loadButton) {
            loadImage(leftCanvas);
        }
        else if (e.getSource() == saveRightButton) {
            saveCanvas(rightCanvas);
        }
        else if (e.getSource() == loadRightButton) {
            loadImage(rightCanvas);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == penSizeSlider) {
            if (!penSizeSlider.getValueIsAdjusting()) {
                rightCanvas.setPenSize(penSizeSlider.getValue());
            }
        }
    }

    private void saveCanvas(JPanel canvas) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Canvas As");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getName().toLowerCase();
            String format = fileName.endsWith(".jpg") ? "jpg" : "png";
            if (!fileName.endsWith("." + format)) {
                file = new File(file.getAbsolutePath() + "." + format);
            }
            try {
                if (canvas instanceof LeftCanvas) {
                    ((LeftCanvas)canvas).saveCanvasToFile(file, format);
                } else if (canvas instanceof RightCanvas) {
                    ((RightCanvas)canvas).saveCanvasToFile(file, format);
                }
                JOptionPane.showMessageDialog(null, "Canvas saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error saving file: " + ex.getMessage());
            }
        }
    }

    private void loadImage(JPanel canvas) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Image");
        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                if (canvas instanceof LeftCanvas) {
                    ((LeftCanvas)canvas).loadImageFromFile(file);
                } else if (canvas instanceof RightCanvas) {
                    ((RightCanvas)canvas).loadImageFromFile(file);
                }
                JOptionPane.showMessageDialog(null, "Image loaded successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error loading image: " + ex.getMessage());
            }
        }
    }
}