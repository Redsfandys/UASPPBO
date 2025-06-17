package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import model.character.Character;

public class FemaleRooster extends GameObject {
    private boolean active;
    
    // Animation variables
    private static BufferedImage[] femaleFrames = new BufferedImage[2];
    private static boolean imagesLoaded = false;
    private static boolean loadAttempted = false;
    private int animationCounter = 0;
    private static final int ANIMATION_SPEED = 60; // Slower, gentle animation
    
    public FemaleRooster(int row, int col, int size) {
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
            // Try loading female rooster frames
            File female1File = new File(Character.IMAGE_BASE_PATH + "female1.png");
            File female2File = new File(Character.IMAGE_BASE_PATH + "female2.png");
            
            if (female1File.exists() && female2File.exists()) {
                femaleFrames[0] = ImageIO.read(female1File);
                femaleFrames[1] = ImageIO.read(female2File);
                System.out.println("Successfully loaded female rooster animation frames");
            } else if (female1File.exists()) {
                // If only one frame exists, use it for both
                femaleFrames[0] = ImageIO.read(female1File);
                femaleFrames[1] = ImageIO.read(female1File);
                System.out.println("Using single female rooster image for animation");
            } else {
                // Try alternative naming
                File femaleFile = new File(Character.IMAGE_BASE_PATH + "female.png");
                if (femaleFile.exists()) {
                    femaleFrames[0] = ImageIO.read(femaleFile);
                    femaleFrames[1] = ImageIO.read(femaleFile);
                    System.out.println("Using female.png for animation");
                } else {
                    System.out.println("No female rooster images found at path: " + Character.IMAGE_BASE_PATH);
                }
            }
            
            imagesLoaded = (femaleFrames[0] != null && femaleFrames[1] != null);
            
        } catch (IOException e) {
            System.out.println("Error loading female rooster images: " + e.getMessage());
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
            int frameIndex = (animationCounter / ANIMATION_SPEED) % femaleFrames.length;
            BufferedImage currentFrame = femaleFrames[frameIndex];
            
            if (currentFrame != null) {
                g.drawImage(currentFrame, pixelX, pixelY, size, size, null);
            } else {
                drawFallback(g, pixelX, pixelY);
            }
        } else {
            drawFallback(g, pixelX, pixelY);
        }
        
        // Draw heart symbols to indicate this is the beloved
        g.setColor(Color.PINK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("💗", pixelX + size - 15, pixelY + 15);
    }
    
    private void drawFallback(Graphics g, int pixelX, int pixelY) {
        // Fallback to pink/magenta square if images aren't loaded
        g.setColor(Color.MAGENTA);
        g.fillRect(pixelX, pixelY, size, size);
        
        // Add some animation even for fallback
        int pulseSize = (animationCounter / 20) % 4; // Gentle pulsing effect
        g.setColor(Color.PINK);
        g.fillRect(pixelX + pulseSize, pixelY + pulseSize, 
                  size - (pulseSize * 2), size - (pulseSize * 2));
        
        // Draw "F" for Female in the center
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("F", pixelX + size/2 - 5, pixelY + size/2 + 5);
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
