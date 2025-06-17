package model;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

// A simple class for visual-only objects like grass, rocks, etc.
public class Decoration extends GameObject {

    private BufferedImage image;

    // The constructor takes a position and the specific image to use.
    public Decoration(int row, int col, int size, BufferedImage image) {
        super(row, col, size);
        this.image = image;
    }

    @Override
    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, col * size, row * size, size, size, null);
        }
    }

    @Override
    public void update() {
        // Decorations are static, so the update method is empty.
        // You could add animation logic here in the future!
    }
}