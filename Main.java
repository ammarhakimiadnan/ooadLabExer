// This is a simple Java Swing application that creates a drawing studio interface allowing 
// users to draw on a left canvas and manipulate images on a right canvas.
import javax.swing.*;
import java.awt.*;

public class Main {
    // Main method to set up the JFrame and add components
    public static void main(String[] args) {
        JFrame frame = new JFrame("Drawing Studio Pro");
        
        LeftCanvas leftCanvas = new LeftCanvas();
        RightCanvas rightCanvas = new RightCanvas();
        Toolbar toolbar = new Toolbar(rightCanvas, leftCanvas);
        
        // Create scroll panes with proper viewport settings
        JScrollPane leftScroll = new JScrollPane(leftCanvas);
        leftScroll.setBorder(BorderFactory.createEmptyBorder());
        leftScroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        // Set preferred size for left canvas to ensure it displays correctly
        JScrollPane rightScroll = new JScrollPane(rightCanvas);
        rightScroll.setBorder(BorderFactory.createEmptyBorder());
        rightScroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        // Setup main window layout
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftScroll,
            rightScroll
        );
        splitPane.setDividerLocation(400);
        
        // Add components to the frame
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(toolbar, BorderLayout.SOUTH);
        
        // Set frame properties
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}