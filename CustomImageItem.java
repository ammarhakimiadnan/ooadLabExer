// CustomImageItem.java
import java.awt.image.BufferedImage;

public class CustomImageItem implements CreationItem {
    private BufferedImage image;
    private double rotation;
    private double scale;
    private boolean flipH;
    private boolean flipV;

    public CustomImageItem(BufferedImage image) {
        this.image = image;
        this.rotation = 0;
        this.scale = 1.0;
        this.flipH = false;
        this.flipV = false;
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public String getType() {
        return "Custom";
    }

    @Override
    public void rotate(double radians) {
        this.rotation += radians;
    }

    @Override
    public void scale(double scaleFactor) {
        this.scale *= scaleFactor;
    }

    @Override
    public void flipHorizontal() {
        this.flipH = !this.flipH;
    }

    @Override
    public void flipVertical() {
        this.flipV = !this.flipV;
    }

    @Override
    public double getRotation() {
        return rotation;
    }

    @Override
    public double getScale() {
        return scale;
    }

    @Override
    public boolean isFlippedH() {
        return flipH;
    }

    @Override
    public boolean isFlippedV() {
        return flipV;
    }
}