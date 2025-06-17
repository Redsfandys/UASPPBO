package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import model.character.Character;

public class CornItem extends GameObject {
    private static final Random random = new Random();
    private boolean active;
    
    // Animation variables
    private static BufferedImage[] cornFrames = new BufferedImage[2];
    private static boolean imagesLoaded = false;
    private static boolean loadAttempted = false;
    private int animationCounter = 0;
    private static final int ANIMATION_SPEED = 45; // Slower animation for corn
    
    public CornItem(int row, int col, int size) {
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
            // Try loading corn frames - use corn.png and corn2.png (your actual filenames)
            File corn1File = new File(Character.IMAGE_BASE_PATH + "corn.png");
            File corn2File = new File(Character.IMAGE_BASE_PATH + "corn2.png");
            
            // If both corn.png and corn2.png exist, use them
            if (corn1File.exists() && corn2File.exists()) {
                cornFrames[0] = ImageIO.read(corn1File);
                cornFrames[1] = ImageIO.read(corn2File);
                System.out.println("Successfully loaded corn frames: corn.png, corn2.png");
            } else if (corn1File.exists()) {
                // If only corn.png exists, use it for both frames
                cornFrames[0] = ImageIO.read(corn1File);
                cornFrames[1] = ImageIO.read(corn1File);
                System.out.println("Using corn.png for both frames");
            } else {
                System.out.println("No corn images found at path: " + Character.IMAGE_BASE_PATH);
                System.out.println("Looking for: corn.png and corn2.png");
            }
            
            imagesLoaded = (cornFrames[0] != null && cornFrames[1] != null);
            
            if (imagesLoaded) {
                System.out.println("Corn animation ready with " + cornFrames.length + " frames");
            }
            
        } catch (IOException e) {
            System.out.println("Error loading corn images: " + e.getMessage());
            imagesLoaded = false;
        }
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public static CornItem createAtRandomPosition(Tile[][] maze, PlayerRooster player, EnemyRooster enemy, int size) {
        int row, col;
        do {
            row = random.nextInt(maze.length);
            col = random.nextInt(maze[0].length);
        } while (maze[row][col].isWall() || 
                 (row == player.getRow() && col == player.getCol()) || 
                 (row == enemy.getRow() && col == enemy.getCol()));
        
        return new CornItem(row, col, size);
    }

    @Override
    public void draw(Graphics g) {
        if (!active) return;
        
        // Calculate pixel position
        int pixelX = col * size;
        int pixelY = row * size;
        
        if (imagesLoaded) {
            // Calculate current frame for animation
            int frameIndex = (animationCounter / ANIMATION_SPEED) % cornFrames.length;
            BufferedImage currentFrame = cornFrames[frameIndex];
            
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
        // Fallback to yellow circle if images aren't loaded
        g.setColor(Color.YELLOW);
        g.fillOval(pixelX + 5, pixelY + 5, size - 10, size - 10);
        
        // Add some animation even for fallback
        int pulseSize = (animationCounter / 15) % 6; // Simple pulsing effect
        g.setColor(Color.ORANGE);
        g.fillOval(pixelX + 5 + pulseSize, pixelY + 5 + pulseSize, 
                  size - 10 - (pulseSize * 2), size - 10 - (pulseSize * 2));
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
