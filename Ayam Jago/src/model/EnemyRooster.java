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
import java.util.Random;
import model.character.Character;

public class EnemyRooster extends GameObject {
    // Direction constants
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;
    
    private int health;
    private float visualRow;
    private float visualCol;
    private boolean isMoving;
    private static final float MOVE_SPEED = 0.1f;
    private Random random = new Random();
    
    // Current direction
    private int currentDirection = DIRECTION_LEFT;
    
    // Animated sprites
    private static BufferedImage[] enemyIdleLeft = new BufferedImage[2];
    private static BufferedImage[] enemyMoveLeft = new BufferedImage[2];
    private static BufferedImage[] enemyIdleRight = new BufferedImage[2];
    private static BufferedImage[] enemyMoveRight = new BufferedImage[2];
    private static boolean imagesLoaded = false;
    private static boolean loadAttempted = false;
    
    // Animation variables
    private int animationCounter = 0;
    private static final int ANIMATION_SPEED = 30; // Change frame every 30 updates

    public EnemyRooster(int row, int col, int size) {
        super(row, col, size);
        this.health = 2;
        this.visualRow = row;
        this.visualCol = col;
        this.isMoving = false;
        
        // Load images if not already loaded
        if (!loadAttempted) {
            loadImages();
        }
    }
    
    private static void loadImages() {
        loadAttempted = true;
        
        try {
            // Load idle frames (facing left)
            File idle1File = new File(Character.IMAGE_BASE_PATH + "enemy-idle1.png");
            File idle2File = new File(Character.IMAGE_BASE_PATH + "enemy-idle2.png");
            
            if (idle1File.exists() && idle2File.exists()) {
                enemyIdleLeft[0] = ImageIO.read(idle1File);
                enemyIdleLeft[1] = ImageIO.read(idle2File);
                
                // Create flipped versions (facing right)
                enemyIdleRight[0] = flipImageHorizontally(enemyIdleLeft[0]);
                enemyIdleRight[1] = flipImageHorizontally(enemyIdleLeft[1]);
                
                System.out.println("Successfully loaded enemy idle frames");
            }
            
            // Load move frames (facing left)
            File move1File = new File(Character.IMAGE_BASE_PATH + "enemy-move1.png");
            File move2File = new File(Character.IMAGE_BASE_PATH + "enemy-move2.png");
            
            if (move1File.exists() && move2File.exists()) {
                enemyMoveLeft[0] = ImageIO.read(move1File);
                enemyMoveLeft[1] = ImageIO.read(move2File);
                
                // Create flipped versions (facing right)
                enemyMoveRight[0] = flipImageHorizontally(enemyMoveLeft[0]);
                enemyMoveRight[1] = flipImageHorizontally(enemyMoveLeft[1]);
                
                System.out.println("Successfully loaded enemy move frames");
            }
            
            imagesLoaded = (enemyIdleLeft[0] != null && enemyIdleLeft[1] != null && 
                           enemyMoveLeft[0] != null && enemyMoveLeft[1] != null);
            
        } catch (IOException e) {
            System.out.println("Error loading enemy animated sprites: " + e.getMessage());
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

    public void moveTowardsPlayer(PlayerRooster player, Tile[][] maze) {
        if (isMoving) return;
        
        // Calculate direction to player
        int playerRow = player.getRow();
        int playerCol = player.getCol();
        
        // Debug info
        System.out.println("Enemy at: " + row + "," + col + " Player at: " + playerRow + "," + playerCol);
        
        // Calculate direction
        int dRow = 0;
        int dCol = 0;
        
        if (playerRow > row) dRow = 1;
        else if (playerRow < row) dRow = -1;
        
        if (playerCol > col) dCol = 1;
        else if (playerCol < col) dCol = -1;
        
        // Try to move horizontally or vertically (preferring the direction that gets closer)
        boolean moved = false;
        
        // Try primary direction first (the one with greater distance)
        if (Math.abs(playerRow - row) > Math.abs(playerCol - col)) {
            // Try vertical first
            moved = tryMove(row + dRow, col, maze, dRow, 0);
            
            // If vertical failed, try horizontal
            if (!moved && dCol != 0) {
                moved = tryMove(row, col + dCol, maze, 0, dCol);
            }
        } else {
            // Try horizontal first
            moved = tryMove(row, col + dCol, maze, 0, dCol);
            
            // If horizontal failed, try vertical
            if (!moved && dRow != 0) {
                moved = tryMove(row + dRow, col, maze, dRow, 0);
            }
        }
        
        // If nothing worked, try any valid move
        if (!moved) {
            int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}}; // Right, Down, Left, Up
            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];
                if (isValidMove(newRow, newCol, maze)) {
                    row = newRow;
                    col = newCol;
                    isMoving = true;
                    
                    // Update direction based on actual movement
                    if (dir[0] == -1) currentDirection = DIRECTION_UP;
                    else if (dir[0] == 1) currentDirection = DIRECTION_DOWN;
                    else if (dir[1] == -1) currentDirection = DIRECTION_LEFT;
                    else if (dir[1] == 1) currentDirection = DIRECTION_RIGHT;
                    
                    System.out.println("Enemy moved to: " + row + "," + col + " facing: " + getDirectionName());
                    return;
                }
            }
        }
    }
    
    private boolean tryMove(int newRow, int newCol, Tile[][] maze, int dRow, int dCol) {
        if (isValidMove(newRow, newCol, maze)) {
            row = newRow;
            col = newCol;
            isMoving = true;
            
            // Update direction based on movement direction
            if (dRow < 0) currentDirection = DIRECTION_UP;
            else if (dRow > 0) currentDirection = DIRECTION_DOWN;
            else if (dCol < 0) currentDirection = DIRECTION_LEFT;
            else if (dCol > 0) currentDirection = DIRECTION_RIGHT;
            
            System.out.println("Enemy moved to: " + row + "," + col + " facing: " + getDirectionName());
            return true;
        }
        return false;
    }
    
    // Helper method for debugging
    private String getDirectionName() {
        switch (currentDirection) {
            case DIRECTION_UP: return "UP";
            case DIRECTION_RIGHT: return "RIGHT";
            case DIRECTION_DOWN: return "DOWN";
            case DIRECTION_LEFT: return "LEFT";
            default: return "UNKNOWN";
        }
    }
    
    private boolean isValidMove(int newRow, int newCol, Tile[][] maze) {
        // Check maze bounds
        if (newRow < 0 || newRow >= maze.length || newCol < 0 || newCol >= maze[0].length) {
            return false;
        }
        
        // Check if target is a wall
        return !maze[newRow][newCol].isWall();
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
                currentFrames = (currentDirection == DIRECTION_RIGHT) ? enemyMoveRight : enemyMoveLeft;
            } else {
                // Use idle sprites when idle
                currentFrames = (currentDirection == DIRECTION_RIGHT) ? enemyIdleRight : enemyIdleLeft;
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
        // Fallback to green square if images aren't loaded
        g.setColor(Color.GREEN);
        g.fillRect(pixelX, pixelY, size, size);
    }
}
