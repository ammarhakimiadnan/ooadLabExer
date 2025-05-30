import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("PixelCraft Studio");
        
        LeftCanvas leftCanvas = new LeftCanvas();
        RightCanvas rightCanvas = new RightCanvas();
        Toolbar toolbar = new Toolbar(rightCanvas, leftCanvas);
        
        // Setup main window layout
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftCanvas,
            rightCanvas
        );
        splitPane.setDividerLocation(400);
        
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(toolbar, BorderLayout.SOUTH);
        
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}