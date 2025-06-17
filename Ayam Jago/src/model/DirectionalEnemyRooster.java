package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import model.character.Character;

public class DirectionalEnemyRooster extends GameObject {
    // Konstanta untuk arah
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;
    
    private int health;
    private float visualRow;
    private float visualCol;
    private boolean isMoving;
    private static final float MOVE_SPEED = 0.2f;
    private Random random = new Random();
    
    // Arah saat ini
    private int currentDirection = DIRECTION_LEFT;
    
    // Image untuk berbagai arah
    private static BufferedImage[] enemyImages = new BufferedImage[4]; // Atas, Kanan, Bawah, Kiri
    private static boolean[] imageLoaded = new boolean[4];
    private static boolean loadAttempted = false;
    
    public DirectionalEnemyRooster(int row, int col, int size) {
        super(row, col, size);
        this.health = 2;
        this.visualRow = row;
        this.visualCol = col;
        this.isMoving = false;
        
        // Load image if not loaded already
        if (!loadAttempted) {
            loadImages();
        }
    }
    
    private static void loadImages() {
        loadAttempted = true;
        String[] imageNames = {
            "enemy_up.png",
            "enemy_right.png",
            "enemy_down.png", 
            "enemy_left.png"
        };
        
        // Coba muat semua gambar
        for (int i = 0; i < 4; i++) {
            try {
                File imageFile = new File(Character.IMAGE_BASE_PATH + imageNames[i]);
                if (imageFile.exists()) {
                    enemyImages[i] = ImageIO.read(imageFile);
                    imageLoaded[i] = true;
                    System.out.println("Successfully loaded enemy image: " + imageNames[i]);
                } else {
                    // Jika file tidak ada, lihat apakah ada fallback image
                    File fallbackFile = new File(Character.IMAGE_BASE_PATH + "enemy.png");
                    if (fallbackFile.exists()) {
                        enemyImages[i] = ImageIO.read(fallbackFile);
                        imageLoaded[i] = true;
                        System.out.println("Using fallback image for: " + imageNames[i]);
                    } else {
                        System.out.println("Enemy image not found: " + imageNames[i]);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading enemy image " + imageNames[i] + ": " + e.getMessage());
            }
        }
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
        
        // Update direction based on prioritized movement
        if (Math.abs(playerRow - row) > Math.abs(playerCol - col)) {
            // Vertical direction is primary
            if (dRow < 0) currentDirection = DIRECTION_UP;
            else if (dRow > 0) currentDirection = DIRECTION_DOWN;
        } else {
            // Horizontal direction is primary
            if (dCol < 0) currentDirection = DIRECTION_LEFT;
            else if (dCol > 0) currentDirection = DIRECTION_RIGHT;
        }
        
        // Try to move horizontally or vertically (preferring the direction that gets closer)
        boolean moved = false;
        
        // Try primary direction first (the one with greater distance)
        if (Math.abs(playerRow - row) > Math.abs(playerCol - col)) {
            // Try vertical first
            moved = tryMove(row + dRow, col, maze);
            
            // If vertical failed, try horizontal
            if (!moved) {
                moved = tryMove(row, col + dCol, maze);
                
                // Update direction for horizontal move
                if (moved) {
                    if (dCol < 0) currentDirection = DIRECTION_LEFT;
                    else if (dCol > 0) currentDirection = DIRECTION_RIGHT;
                }
            }
        } else {
            // Try horizontal first
            moved = tryMove(row, col + dCol, maze);
            
            // If horizontal failed, try vertical
            if (!moved) {
                moved = tryMove(row + dRow, col, maze);
                
                // Update direction for vertical move
                if (moved) {
                    if (dRow < 0) currentDirection = DIRECTION_UP;
                    else if (dRow > 0) currentDirection = DIRECTION_DOWN;
                }
            }
        }
        
        // If nothing worked, try any valid move
        if (!moved) {
            int[][] directions = {{-1,0}, {0,1}, {1,0}, {0,-1}}; // Up, Right, Down, Left
            for (int i = 0; i < directions.length; i++) {
                int newRow = row + directions[i][0];
                int newCol = col + directions[i][1];
                if (isValidMove(newRow, newCol, maze)) {
                    row = newRow;
                    col = newCol;
                    currentDirection = i; // Set direction based on movement
                    isMoving = true;
                    System.out.println("Enemy moved to: " + row + "," + col);
                    return;
                }
            }
        }
    }
    
    private boolean tryMove(int newRow, int newCol, Tile[][] maze) {
        if (isValidMove(newRow, newCol, maze)) {
            row = newRow;
            col = newCol;
            isMoving = true;
            System.out.println("Enemy moved to: " + row + "," + col);
            return true;
        }
        return false;
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
    }
    
    @Override
    public void draw(Graphics g) {
        // Calculate pixel position for smooth movement
        int pixelX = Math.round(visualCol * size);
        int pixelY = Math.round(visualRow * size);
        
        // Gambar enemy dengan image sesuai arah
        if (imageLoaded[currentDirection] && enemyImages[currentDirection] != null) {
            g.drawImage(enemyImages[currentDirection], pixelX, pixelY, size, size, null);
        } else {
            // Fallback jika tidak ada gambar
            g.setColor(Color.GREEN);
            g.fillRect(pixelX, pixelY, size, size);
            
            // Tambahkan tanda arah
            g.setColor(Color.WHITE);
            int centerX = pixelX + size/2;
            int centerY = pixelY + size/2;
            int arrowSize = size/3;
            
            // Gambar tanda arah sesuai currentDirection
            switch (currentDirection) {
                case DIRECTION_UP:
                    g.drawLine(centerX, centerY - arrowSize, centerX, centerY + arrowSize);
                    g.drawLine(centerX, centerY - arrowSize, centerX - arrowSize/2, centerY - arrowSize/2);
                    g.drawLine(centerX, centerY - arrowSize, centerX + arrowSize/2, centerY - arrowSize/2);
                    break;
                case DIRECTION_RIGHT:
                    g.drawLine(centerX - arrowSize, centerY, centerX + arrowSize, centerY);
                    g.drawLine(centerX + arrowSize, centerY, centerX + arrowSize/2, centerY - arrowSize/2);
                    g.drawLine(centerX + arrowSize, centerY, centerX + arrowSize/2, centerY + arrowSize/2);
                    break;
                case DIRECTION_DOWN:
                    g.drawLine(centerX, centerY - arrowSize, centerX, centerY + arrowSize);
                    g.drawLine(centerX, centerY + arrowSize, centerX - arrowSize/2, centerY + arrowSize/2);
                    g.drawLine(centerX, centerY + arrowSize, centerX + arrowSize/2, centerY + arrowSize/2);
                    break;
                case DIRECTION_LEFT:
                    g.drawLine(centerX - arrowSize, centerY, centerX + arrowSize, centerY);
                    g.drawLine(centerX - arrowSize, centerY, centerX - arrowSize/2, centerY - arrowSize/2);
                    g.drawLine(centerX - arrowSize, centerY, centerX - arrowSize/2, centerY + arrowSize/2);
                    break;
            }
        }
        
        // Draw health as a number
        g.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.BOLD, 12);
        g.setFont(font);
        g.drawString("♥" + health, pixelX + 5, pixelY - 5);
    }
}
