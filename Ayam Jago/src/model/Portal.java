package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import model.character.Character;

public class Portal extends GameObject {
    private boolean active;
    
    // Animation variables
    private static BufferedImage[] portalFrames = new BufferedImage[2];
    private static boolean imagesLoaded = false;
    private static boolean loadAttempted = false;
    private int animationCounter = 0;
    private static final int ANIMATION_SPEED = 30; // Portal animation speed
    
    public Portal(int row, int col, int size) {
        super(row, col, size);
        this.active = true;
        
        // Load images if not already loaded
        if (!loadAttempted) {
            loadImages();
        }
    }
    
    private static void loadImages() {
        loadAttempted = true;
        
        try {
            // Try loading portal frames
            File portal1File = new File(Character.IMAGE_BASE_PATH + "portal1.png");
            File portal2File = new File(Character.IMAGE_BASE_PATH + "portal2.png");
            
            if (portal1File.exists() && portal2File.exists()) {
                portalFrames[0] = ImageIO.read(portal1File);
                portalFrames[1] = ImageIO.read(portal2File);
                System.out.println("Successfully loaded portal animation frames");
            } else if (portal1File.exists()) {
                // If only one frame exists, use it for both
                portalFrames[0] = ImageIO.read(portal1File);
                portalFrames[1] = ImageIO.read(portal1File);
                System.out.println("Using single portal image for animation");
            } else {
                System.out.println("No portal images found at path: " + Character.IMAGE_BASE_PATH);
            }
            
            imagesLoaded = (portalFrames[0] != null && portalFrames[1] != null);
            
        } catch (IOException e) {
            System.out.println("Error loading portal images: " + e.getMessage());
            imagesLoaded = false;
        }
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void draw(Graphics g) {
        if (!active) return;
        
        // Calculate pixel position
        int pixelX = col * size;
        int pixelY = row * size;
        
        if (imagesLoaded) {
            // Calculate current frame for animation
            int frameIndex = (animationCounter / ANIMATION_SPEED) % portalFrames.length;
            BufferedImage currentFrame = portalFrames[frameIndex];
            
            if (currentFrame != null) {
                g.drawImage(currentFrame, pixelX, pixelY, size, size, null);
            } else {
                drawFallback(g, pixelX, pixelY);
            }
        } else {
            drawFallback(g, pixelX, pixelY);
        }
    }
    
    private void drawFallback(Graphics g, int pixelX, int pixelY) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Create swirling portal effect
        int centerX = pixelX + size / 2;
        int centerY = pixelY + size / 2;
        int radius = size / 3;
        
        // Outer ring
        g.setColor(new Color(128, 0, 255, 150)); // Purple with transparency
        g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Inner ring with animation
        int innerRadius = radius / 2 + (animationCounter / 10) % 5;
        g.setColor(new Color(255, 255, 255, 200)); // White center
        g.fillOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);
        
        // Sparkle effect
        g.setColor(Color.YELLOW);
        for (int i = 0; i < 8; i++) {
            double angle = (animationCounter / 5.0 + i * 45) * Math.PI / 180;
            int sparkleX = centerX + (int)(Math.cos(angle) * radius);
            int sparkleY = centerY + (int)(Math.sin(angle) * radius);
            g.fillOval(sparkleX - 2, sparkleY - 2, 4, 4);
        }
    }

    @Override
    public void update() {
        // Update animation counter
        animationCounter++;
        
        // Reset counter to prevent overflow
        if (animationCounter > 10000) {
            animationCounter = 0;
        }
    }
}
