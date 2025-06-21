// CreationItem.java (updated)
import java.awt.image.BufferedImage;

public interface CreationItem {
    BufferedImage getImage();
    String getType();
    void rotate(double radians);
    void scale(double scaleFactor);
    void flipHorizontal();
    void flipVertical();
    double getRotation();
    double getScale();
    boolean isFlippedH();
    boolean isFlippedV();
}