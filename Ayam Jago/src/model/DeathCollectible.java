package model;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

// Represents the item dropped by a dead enemy.
public class DeathCollectible extends GameObject {

    private static BufferedImage image;
    private boolean collected = false;

    public DeathCollectible(int row, int col, int size) {
        super(row, col, size);
    }

    // We'll set the image from GamePanel, so we only load it once.
    public static void setImage(BufferedImage img) {
        image = img;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public boolean isCollected() {
        return collected;
    }

    @Override
    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, col * size, row * size, size, size, null);
        }
    }

    @Override
    public void update() {
        // This object is static, so nothing to update.
    }
}