package model;

import interfaces.Drawable;
import interfaces.Updateable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AttackEffect implements Drawable, Updateable {

    // --- Image Loading (Static: Loaded only ONCE for all effects) ---
    private static final int NUM_FRAMES = 8; // The number of frames in your animation
    private static BufferedImage[] frames = new BufferedImage[NUM_FRAMES];
    private static boolean imagesLoaded = false;

    // --- Instance Variables (Unique for each slash on screen) ---
    private int x, y; // Position in pixels
    private int lifeTimer;
    private static final int TOTAL_DURATION = 16; // Effect lasts for 16 frames (about 1/4 second)
    private int direction;


    public AttackEffect(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.lifeTimer = TOTAL_DURATION;
        this.direction = direction; // Store the attack direction
        if (!imagesLoaded) {
            loadImages();
        }
    }


    // This static method loads the 8 frames into memory once.
    private static void loadImages() {
        try {
            String basePath = model.character.Character.IMAGE_BASE_PATH;
            for (int i = 0; i < NUM_FRAMES; i++) {
                // Assumes files are named "attack_1.png", "attack_2.png", etc.
                String filePath = basePath + "attack_" + (i + 1) + ".png";
                File imageFile = new File(filePath);
                if (imageFile.exists()) {
                    frames[i] = ImageIO.read(imageFile);
                } else {
                    System.out.println("Attack sprite not found: " + filePath);
                }
            }
            imagesLoaded = true;
            System.out.println("Attack animation frames loaded successfully.");
        } catch (IOException e) {
            System.out.println("Error loading attack animation frames: " + e.getMessage());
        }
    }

    // A method to check if the effect should be removed
    public boolean isActive() {
        return lifeTimer > 0;
    }

    // The update method simply counts down the life of the effect
    public void update() {
        if (lifeTimer > 0) {
            lifeTimer--;
        }
    }

    // The new draw method, which draws the correct animation frame
    public void draw(Graphics g) {
        if (!isActive() || !imagesLoaded) {
            return;
        }

        // --- Calculate which frame to show ---
        // As lifeTimer goes from 16 down to 1, this calculation will map
        // the lifetime of the effect to the 8 animation frames.
        int elapsedFrames = TOTAL_DURATION - lifeTimer;
        int frameIndex = (int) (((float) elapsedFrames / TOTAL_DURATION) * NUM_FRAMES);

        // Ensure frameIndex is always within bounds
        if (frameIndex < 0) frameIndex = 0;
        if (frameIndex >= NUM_FRAMES) frameIndex = NUM_FRAMES - 1;

        BufferedImage currentFrame = frames[frameIndex];
        if (currentFrame != null) {
            int desiredSize = 48; // Let's try 48x48 pixels. Adjust this to what looks best!

            Graphics2D g2d = (Graphics2D) g.create();
            AffineTransform tx = new AffineTransform();
            BufferedImage transformedFrame = currentFrame; // Start with the original frame

            switch (direction) {
                case model.PlayerRooster.DIRECTION_LEFT:
                    tx.scale(-1, 1);
                    tx.translate(-currentFrame.getWidth(), 0);
                    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                    transformedFrame = op.filter(currentFrame, null);
                    break;
                case model.PlayerRooster.DIRECTION_UP:
                    tx.rotate(Math.toRadians(-90), currentFrame.getWidth() / 2.0, currentFrame.getHeight() / 2.0);
                    op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
                    transformedFrame = op.filter(currentFrame, null);
                    break;
                case model.PlayerRooster.DIRECTION_DOWN:
                    tx.rotate(Math.toRadians(90), currentFrame.getWidth() / 2.0, currentFrame.getHeight() / 2.0);
                    op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
                    transformedFrame = op.filter(currentFrame, null);
                    break;
                // Case for DIRECTION_RIGHT is default, no transformation needed.
            }

            // 3. Calculate the draw position to center the NEW, SMALLER size.
            int drawX = x - desiredSize / 2;
            int drawY = y - desiredSize / 2;

            // 4. Draw the transformed image at the desired final size.
            g2d.drawImage(transformedFrame, drawX, drawY, desiredSize, desiredSize, null);
            g2d.dispose();

        }
    }
}