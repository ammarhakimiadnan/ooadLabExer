// Implements CreationItem for flower images with rotation, scaling, and flipping capabilities.
import java.awt.image.BufferedImage;

public class FlowerItem implements CreationItem {
    private BufferedImage image;
    private double rotation;
    private double scale;
    private boolean flipH;
    private boolean flipV;

    // Constructor to initialize the FlowerItem with an image
    public FlowerItem(BufferedImage image) {
        this.image = image;
        this.rotation = 0;
        this.scale = 1.0;
        this.flipH = false;
        this.flipV = false;
    }

    // Method to get the image associated with this FlowerItem
    @Override
    public BufferedImage getImage() {
        return image;
    }

    // Method to set a new image for this FlowerItem
    @Override
    public String getType() {
        return "Flower";
    }

    // Method to rotate the image by a given angle in radians
    @Override
    public void rotate(double radians) {
        this.rotation += radians;
    }

    // Method to scale the image by a given factor
    @Override
    public void scale(double scaleFactor) {
        this.scale *= scaleFactor;
    }

    // Method to flip the image horizontally
    @Override
    public void flipHorizontal() {
        this.flipH = !this.flipH;
    }

    // Method to flip the image vertically
    @Override
    public void flipVertical() {
        this.flipV = !this.flipV;
    }

    // Getters for the properties of the FlowerItem
    @Override
    public double getRotation() {
        return rotation;
    }

    // Method to get the scale of the image
    @Override
    public double getScale() {
        return scale;
    }

    // Check if the image is flipped horizontally
    @Override
    public boolean isFlippedH() {
        return flipH;
    }

    // Check if the image is flipped vertically
    @Override
    public boolean isFlippedV() {
        return flipV;
    }
}