package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import interfaces.Movable;
import main.SoundManager;
import model.character.Character;

public class PlayerRooster extends GameObject implements Movable {
    // Direction constants
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;
    
    private int health;
    private float visualRow;
    private float visualCol;
    private boolean isMoving;
    private int moveCount;
    private static final float MOVE_SPEED = 0.1f;
    
    // Current direction
    private int currentDirection = DIRECTION_RIGHT;
    
    // Animated sprites
    private static BufferedImage[] playerIdleLeft = new BufferedImage[2];
    private static BufferedImage[] playerMoveLeft = new BufferedImage[2];
    private static BufferedImage[] playerIdleRight = new BufferedImage[2];
    private static BufferedImage[] playerMoveRight = new BufferedImage[2];
    private static boolean imagesLoaded = false;
    private static boolean loadAttempted = false;
    
    // Animation variables
    private int animationCounter = 0;
    private static final int ANIMATION_SPEED = 10; // Change frame every 30 updates

    private boolean flashing = false;
    private int flashDuration = 0;
    private Color flashColor = Color.BLACK;

    public void startFlash(Color color, int durationInFrames) {
        this.flashing = true;
        this.flashColor = color;
        this.flashDuration = durationInFrames;
    }



    public PlayerRooster(int row, int col, int size) {
        super(row, col, size);
        this.health = 5;
        this.visualRow = row;
        this.visualCol = col;
        this.isMoving = false;
        this.moveCount = 0;
        
        // Load images if not already loaded
        if (!loadAttempted) {
            loadImages();
        }
    }
    
    private static void loadImages() {
        loadAttempted = true;
        
        try {
            // Load idle frames (facing left)
            File idle1File = new File(Character.IMAGE_BASE_PATH + "player-idle1.png");
            File idle2File = new File(Character.IMAGE_BASE_PATH + "player-idle2.png");

            if (idle1File.exists() && idle2File.exists()) {
                playerIdleLeft[0] = ImageIO.read(idle1File);
                playerIdleLeft[1] = ImageIO.read(idle2File);

                // Create flipped versions (facing right)
                playerIdleRight[0] = flipImageHorizontally(playerIdleLeft[0]);
                playerIdleRight[1] = flipImageHorizontally(playerIdleLeft[1]);

                System.out.println("Successfully loaded player idle frames");
            }

            // Load move frames (facing left)
            File move1File = new File(Character.IMAGE_BASE_PATH + "player-move1.png");
            File move2File = new File(Character.IMAGE_BASE_PATH + "player-move2.png");

            if (move1File.exists() && move2File.exists()) {
                playerMoveLeft[0] = ImageIO.read(move1File);
                playerMoveLeft[1] = ImageIO.read(move2File);

                // Create flipped versions (facing right)
                playerMoveRight[0] = flipImageHorizontally(playerMoveLeft[0]);
                playerMoveRight[1] = flipImageHorizontally(playerMoveLeft[1]);

                System.out.println("Successfully loaded player move frames");
            }

            imagesLoaded = (playerIdleLeft[0] != null && playerIdleLeft[1] != null &&
                           playerMoveLeft[0] != null && playerMoveLeft[1] != null);

        } catch (IOException e) {
            System.out.println("Error loading player animated sprites: " + e.getMessage());
            imagesLoaded = false;
        }
    }
    
    private static BufferedImage flipImageHorizontally(BufferedImage image) {
        if (image == null) return null;
        
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage flipped = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = flipped.createGraphics();
        
        // Apply horizontal flip transformation
        AffineTransform transform = new AffineTransform();
        transform.scale(-1, 1); // Flip horizontally
        transform.translate(-width, 0);
        
        g2d.setTransform(transform);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        
        return flipped;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isMoving() {
        return isMoving;
    }
    
    public int getMoveCount() {
        return moveCount;
    }
    public int getCurrentDirection() {
        return this.currentDirection;
    }

    public void incrementMoveCount() {
        moveCount++;
    }
    
    public void resetMoveCount() {
        moveCount = 0;
    }
    
    public boolean move(int dRow, int dCol, Tile[][] maze) {
        // Calculate next position (one tile)
        int newRow = row + dRow;
        int newCol = col + dCol;
        
        // Update direction based on movement
        if (dRow < 0) currentDirection = DIRECTION_UP;
        else if (dRow > 0) currentDirection = DIRECTION_DOWN;
        else if (dCol < 0) currentDirection = DIRECTION_LEFT;
        else if (dCol > 0) currentDirection = DIRECTION_RIGHT;
        
        // Check if new position is valid
        if (newRow < 0 || newRow >= maze.length || 
            newCol < 0 || newCol >= maze[0].length) {
            return false;
        }
        
        // Check if target position is a wall
        if (maze[newRow][newCol].isWall()) {
            return false;
        }
        
        // Move player by one tile
        row = newRow;
        col = newCol;
        isMoving = true;
        
        // Increment move counter
        incrementMoveCount();
        SoundManager.playSound("assets/step.wav"); // Play step sound on successful move
        return true;
    }

    @Override
    public void update() {
        // Update visual position toward actual position
        float dx = col - visualCol;
        float dy = row - visualRow;
        
        if (Math.abs(dx) < MOVE_SPEED && Math.abs(dy) < MOVE_SPEED) {
            // Close enough to snap to target position
            visualRow = row;
            visualCol = col;
            isMoving = false;
        } else {
            // Move toward target position
            visualRow += Math.signum(dy) * MOVE_SPEED;
            visualCol += Math.signum(dx) * MOVE_SPEED;
            isMoving = true;
        }
        if (flashing) {
            flashDuration--;
            if (flashDuration <= 0) {
                flashing = false;
            }
        }
        // Update animation counter
        animationCounter++;
    }

    @Override
    public void draw(Graphics g) {
        // Calculate pixel position for smooth movement
        int pixelX = Math.round(visualCol * size);
        int pixelY = Math.round(visualRow * size);
        
        if (imagesLoaded) {
            BufferedImage[] currentFrames;
            
            // Choose sprite set based on movement state and direction
            if (isMoving) {
                // Use move sprites when moving
                currentFrames = (currentDirection == DIRECTION_RIGHT) ? playerMoveRight : playerMoveLeft;
            } else {
                // Use idle sprites when idle
                currentFrames = (currentDirection == DIRECTION_RIGHT) ? playerIdleRight : playerIdleLeft;
            }

            if (flashing) {
                g.setColor(flashColor);
                g.fillRect(getCol()* size , getRow() * size, 16, 16);
            }
            
            // Calculate current frame
            int frameIndex = (animationCounter / ANIMATION_SPEED) % currentFrames.length;
            BufferedImage currentImage = currentFrames[frameIndex];
            
            if (currentImage != null) {
                g.drawImage(currentImage, pixelX, pixelY, size, size, null);
            } else {
                drawFallback(g, pixelX, pixelY);
            }

        } else {
            drawFallback(g, pixelX, pixelY);
        }

        // Draw health as a number
        g.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.BOLD, 12);
        g.setFont(font);
        g.drawString("♥" + health, pixelX + 5, pixelY - 5);
    }
    
    private void drawFallback(Graphics g, int pixelX, int pixelY) {
        // Fallback to red square if images aren't loaded
        g.setColor(Color.RED);
        g.fillRect(pixelX, pixelY, size, size);
    }
}
