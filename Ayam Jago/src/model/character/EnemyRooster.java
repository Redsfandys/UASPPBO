package model.character;

import model.GameObject;
import model.Tile;
import model.character.PlayerRooster;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.util.Random;

public class EnemyRooster extends GameObject {
    private int health;
    private float visualRow;
    private float visualCol;
    private boolean isMoving;
    private static final float MOVE_SPEED = 0.2f;
    private Random random = new Random();

    public EnemyRooster(int row, int col, int size) {
        super(row, col, size);
        this.health = 2;
        this.visualRow = row;
        this.visualCol = col;
        this.isMoving = false;
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
            moved = tryMove(row + dRow, col, maze);
            
            // If vertical failed, try horizontal
            if (!moved) {
                moved = tryMove(row, col + dCol, maze);
            }
        } else {
            // Try horizontal first
            moved = tryMove(row, col + dCol, maze);
            
            // If horizontal failed, try vertical
            if (!moved) {
                moved = tryMove(row + dRow, col, maze);
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
        
        // Draw enemy as a green square
        g.setColor(Color.GREEN);
        g.fillRect(pixelX, pixelY, size, size);
        
        // Draw health as a number
        g.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.BOLD, 12);
        g.setFont(font);
        g.drawString("♥" + health, pixelX + 5, pixelY - 5);
    }
}
