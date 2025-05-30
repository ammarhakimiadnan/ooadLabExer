import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class CanvasComposer {

    public static void composeAndShow(LeftCanvas canvas) {
        BufferedImage[] imageHolder = new BufferedImage[1];
        imageHolder[0] = canvas.captureCanvas();

        JFrame frame = new JFrame("Composed Canvas");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(imageHolder[0], 0, 0, null);
            }
        };
        imagePanel.setPreferredSize(new Dimension(imageHolder[0].getWidth(), imageHolder[0].getHeight()));

        // Save button
        JButton saveButton = new JButton("Save Composed Canvas");
        saveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    ImageIO.write(imageHolder[0], "PNG", file);
                    JOptionPane.showMessageDialog(frame, "Canvas saved successfully!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to save canvas: " + ex.getMessage());
                }
            }
        });

        // Rotate button
        JButton rotateButton = new JButton("Rotate 90°");
        rotateButton.addActionListener(e -> {
            imageHolder[0] = rotateImage90(imageHolder[0]);
            imagePanel.setPreferredSize(new Dimension(imageHolder[0].getWidth(), imageHolder[0].getHeight()));
            frame.pack();
            imagePanel.repaint();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(rotateButton);

        frame.setLayout(new BorderLayout());
        frame.add(imagePanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }

    // Utility method to rotate a BufferedImage by 90 degrees clockwise
    private static BufferedImage rotateImage90(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(h, w, src.getType());
        Graphics2D g2 = dest.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate(h / 2.0, w / 2.0);
        at.rotate(Math.toRadians(90));
        at.translate(-w / 2.0, -h / 2.0);
        g2.drawRenderedImage(src, at);
        g2.dispose();
        return dest;
    }
}