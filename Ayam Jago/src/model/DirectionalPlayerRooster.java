package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import model.character.Character;

public class DirectionalPlayerRooster extends GameObject {
    // Konstanta untuk arah
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;
    
    private int health;
    private float visualRow;
    private float visualCol;
    private boolean isMoving;
    private int moveCount;
    private static final float MOVE_SPEED = 0.2f;
    
    // Arah saat ini
    private int currentDirection = DIRECTION_RIGHT;
    
    // Image untuk berbagai arah
    private static BufferedImage[] playerImages = new BufferedImage[4]; // Atas, Kanan, Bawah, Kiri
    private static boolean[] imageLoaded = new boolean[4];
    private static boolean loadAttempted = false;
    
    public DirectionalPlayerRooster(int row, int col, int size) {
        super(row, col, size);
        this.health = 5;
        this.visualRow = row;
        this.visualCol = col;
        this.isMoving = false;
        this.moveCount = 0;
        
        // Load image if not loaded already
        if (!loadAttempted) {
            loadImages();
        }
    }
    
    private static void loadImages() {
        loadAttempted = true;
        String[] imageNames = {
            "player_up.png",
            "player_right.png",
            "player_down.png", 
            "player_left.png"
        };
        
        // Coba muat semua gambar
        for (int i = 0; i < 4; i++) {
            try {
                File imageFile = new File(Character.IMAGE_BASE_PATH + imageNames[i]);
                if (imageFile.exists()) {
                    playerImages[i] = ImageIO.read(imageFile);
                    imageLoaded[i] = true;
                    System.out.println("Successfully loaded player image: " + imageNames[i]);
                } else {
                    // Jika file tidak ada, lihat apakah ada fallback image
                    File fallbackFile = new File(Character.IMAGE_BASE_PATH + "player.png");
                    if (fallbackFile.exists()) {
                        playerImages[i] = ImageIO.read(fallbackFile);
                        imageLoaded[i] = true;
                        System.out.println("Using fallback image for: " + imageNames[i]);
                    } else {
                        System.out.println("Player image not found: " + imageNames[i]);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading player image " + imageNames[i] + ": " + e.getMessage());
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
    
    public int getMoveCount() {
        return moveCount;
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
    }
    
    @Override
    public void draw(Graphics g) {
        // Calculate pixel position for smooth movement
        int pixelX = Math.round(visualCol * size);
        int pixelY = Math.round(visualRow * size);
        
        // Gambar player dengan image sesuai arah
        if (imageLoaded[currentDirection] && playerImages[currentDirection] != null) {
            g.drawImage(playerImages[currentDirection], pixelX, pixelY, size, size, null);
        } else {
            // Fallback jika tidak ada gambar
            g.setColor(Color.RED);
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
