package model;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class KeyItem extends GameObject {
    private boolean active;

    // --- Animation Variables ---
    private static BufferedImage[] keyFrames = new BufferedImage[2];
    private static boolean imagesLoaded = false;
    private int animationCounter = 0;
    private static final int ANIMATION_SPEED = 40; // How fast the key animates

    public KeyItem(int row, int col, int size) {
        super(row, col, size);
        this.active = true;
        if (!imagesLoaded) {
            loadImages();
        }
    }

    private static void loadImages() {
        try {
            String basePath = model.character.Character.IMAGE_BASE_PATH;
            keyFrames[0] = ImageIO.read(new File(basePath + "key_1.png"));
            keyFrames[1] = ImageIO.read(new File(basePath + "key_2.png"));
            imagesLoaded = true;
            System.out.println("Successfully loaded key animation frames.");
        } catch (IOException e) {
            System.out.println("Error loading key frames: " + e.getMessage());
        }
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public void update() {
        animationCounter++;
    }

    @Override
    public void draw(Graphics g) {
        // This 'if' statement is crucial. If the key is not active, it stops right here.
        if (!active || !imagesLoaded) {
            return;
        }

        // Calculate current frame
        int frameIndex = (animationCounter / ANIMATION_SPEED) % keyFrames.length;
        BufferedImage currentFrame = keyFrames[frameIndex];

        if (currentFrame != null) {
            g.drawImage(currentFrame, col * size, row * size, size, size, null);
        }
    }
}