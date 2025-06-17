package model;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class AnimatedSprite {
    private BufferedImage[] idleFrames;
    private BufferedImage[] moveFrames;
    private BufferedImage[] currentFrames;
    
    private int currentFrame = 0;
    private int frameCounter = 0;
    private int frameDelay = 10; // Change frame every 10 updates
    
    private boolean isMoving = false;
    private boolean imagesLoaded = false;
    
    public AnimatedSprite(String baseName, String assetsPath) {
        loadImages(baseName, assetsPath);
    }
    
    private void loadImages(String baseName, String assetsPath) {
        try {
            // Load idle frames
            idleFrames = new BufferedImage[2];
            idleFrames[0] = loadImage(assetsPath + baseName + "-idle1.png");
            idleFrames[1] = loadImage(assetsPath + baseName + "-idle2.png");
            
            // Load move frames
            moveFrames = new BufferedImage[2];
            moveFrames[0] = loadImage(assetsPath + baseName + "-move1.png");
            moveFrames[1] = loadImage(assetsPath + baseName + "-move2.png");
            
            // Set default to idle frames
            currentFrames = idleFrames;
            imagesLoaded = true;
            
            System.out.println("Successfully loaded animated sprites for: " + baseName);
        } catch (Exception e) {
            System.out.println("Error loading animated sprites for " + baseName + ": " + e.getMessage());
            imagesLoaded = false;
        }
    }
    
    private BufferedImage loadImage(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            return ImageIO.read(file);
        } else {
            System.out.println("Image not found: " + path);
            return null;
        }
    }
    
    public void setMoving(boolean moving) {
        if (this.isMoving != moving) {
            this.isMoving = moving;
            currentFrames = moving ? moveFrames : idleFrames;
            currentFrame = 0; // Reset animation when state changes
        }
    }
    
    public void update() {
        if (!imagesLoaded || currentFrames == null) return;
        
        frameCounter++;
        if (frameCounter >= frameDelay) {
            frameCounter = 0;
            currentFrame = (currentFrame + 1) % currentFrames.length;
        }
    }
    
    public void draw(Graphics g, int x, int y, int width, int height) {
        if (imagesLoaded && currentFrames != null && currentFrames[currentFrame] != null) {
            g.drawImage(currentFrames[currentFrame], x, y, width, height, null);
        }
    }
    
    public boolean isImagesLoaded() {
        return imagesLoaded;
    }
}
