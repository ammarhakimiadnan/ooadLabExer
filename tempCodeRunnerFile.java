import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("PixelCraft Studio");
        
        LeftCanvas leftCanvas = new LeftCanvas();
        RightCanvas rightCanvas = new RightCanvas();
        Toolbar toolbar = new Toolbar(rightCanvas, leftCanvas);
        
        // Create scroll panes with proper viewport settings
        JScrollPane leftScroll = new JScrollPane(leftCanvas);
        leftScroll.setBorder(BorderFactory.createEmptyBorder());
        leftScroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
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
        
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(toolbar, BorderLayout.SOUTH);
        
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}