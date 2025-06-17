package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import model.character.Character;

public class KeyItem extends GameObject {
    private boolean active;
    
    // Animation variables
    private static BufferedImage keyImage;
    private static boolean imageLoaded = false;
    private static boolean loadAttempted = false;
    private int animationCounter = 0;
    private static final int GLOW_SPEED = 20; // Speed of glow animation
    
    public KeyItem(int row, int col, int size) {
        super(row, col, size);
        this.active = true;
        
        // Load image if not already loaded
        if (!loadAttempted) {
            loadImage();
        }
    }
    
    private static void loadImage() {
        loadAttempted = true;
        
        try {
            // Try loading key image
            File keyFile = new File(Character.IMAGE_BASE_PATH + "key.png");
            
            if (keyFile.exists()) {
                keyImage = ImageIO.read(keyFile);
                imageLoaded = true;
                System.out.println("Successfully loaded key image");
            } else {
                System.out.println("Key image not found at path: " + Character.IMAGE_BASE_PATH + "key.png");
                imageLoaded = false;
            }
            
        } catch (IOException e) {
            System.out.println("Error loading key image: " + e.getMessage());
            imageLoaded = false;
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
        
        if (imageLoaded && keyImage != null) {
            // Draw the key image
            g.drawImage(keyImage, pixelX, pixelY, size, size, null);
            
            // Add glowing effect
            int glowIntensity = (int)(Math.sin(animationCounter / (double)GLOW_SPEED) * 50 + 100);
            g.setColor(new Color(255, 255, 0, Math.max(0, Math.min(255, glowIntensity))));
            g.drawRect(pixelX - 2, pixelY - 2, size + 4, size + 4);
            g.drawRect(pixelX - 1, pixelY - 1, size + 2, size + 2);
        } else {
            drawFallback(g, pixelX, pixelY);
        }
    }
    
    private void drawFallback(Graphics g, int pixelX, int pixelY) {
        // Fallback to golden key shape if image isn't loaded
        g.setColor(Color.YELLOW);
        
        // Draw key shape
        int keyWidth = size - 8;
        int keyHeight = size - 8;
        int startX = pixelX + 4;
        int startY = pixelY + 4;
        
        // Key head (circle)
        g.fillOval(startX, startY, keyWidth / 2, keyWidth / 2);
        g.setColor(Color.BLACK);
        g.fillOval(startX + 3, startY + 3, keyWidth / 2 - 6, keyWidth / 2 - 6);
        
        // Key shaft
        g.setColor(Color.YELLOW);
        g.fillRect(startX + keyWidth / 4, startY + keyWidth / 2, keyWidth / 4, keyHeight / 2);
        
        // Key teeth
        g.fillRect(startX + keyWidth / 4 + keyWidth / 8, startY + keyWidth / 2 + keyHeight / 4, keyWidth / 8, keyHeight / 8);
        g.fillRect(startX + keyWidth / 4 + keyWidth / 8, startY + keyWidth / 2 + keyHeight / 3, keyWidth / 16, keyHeight / 12);
        
        // Glowing border
        int glowIntensity = (int)(Math.sin(animationCounter / (double)GLOW_SPEED) * 50 + 100);
        g.setColor(new Color(255, 255, 0, Math.max(0, Math.min(255, glowIntensity))));
        g.drawRect(pixelX - 1, pixelY - 1, size + 2, size + 2);
        
        // "KEY" text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 8));
        g.drawString("KEY", pixelX + 2, pixelY + size - 2);
    }

    @Override
    public void update() {
        // Update animation counter for glow effect
        animationCounter++;
        
        // Reset counter to prevent overflow
        if (animationCounter > 10000) {
            animationCounter = 0;
        }
    }
}
