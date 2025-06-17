package model.character;

import model.GameObject;
import model.Tile;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;

public class PlayerRooster extends GameObject {
    private int health;
    private float visualRow;
    private float visualCol;
    private boolean isMoving;
    private int moveCount; // Track how many moves have been made in this turn
    private static final float MOVE_SPEED = 0.2f;

    public PlayerRooster(int row, int col, int size) {
        super(row, col, size);
        this.health = 5;
        this.visualRow = row;
        this.visualCol = col;
        this.isMoving = false;
        this.moveCount = 0;
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
        
        // Draw player as a red square
        g.setColor(Color.RED);
        g.fillRect(pixelX, pixelY, size, size);
        
        // Draw health as a number
        g.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.BOLD, 12);
        g.setFont(font);
        g.drawString("♥" + health, pixelX + 5, pixelY - 5);
    }
}
