package main;

import model.Tile;
import model.PlayerRooster;
import model.EnemyRooster;
import model.CornItem;
import model.Portal;
import model.FemaleRooster;
import model.KeyItem;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    private static final int TILE_SIZE = 34; // 34px sprites
    private static final int MAZE_WIDTH = 25; // Increased from 15 to 25
    private static final int MAZE_HEIGHT = 20; // Increased from 15 to 20
    private static final int FPS = 60;
    
    private Tile[][] maze;
    private PlayerRooster player;
    private EnemyRooster enemy;
    private CornItem corn;
    private Portal portal;
    private FemaleRooster femaleRooster; // The captured female to rescue
    private KeyItem key; // Key needed to rescue the female
    private Timer gameTimer;
    private boolean playerTurn;
    private Random random = new Random();
    private int turnCounter = 0;
    
    // Level system variables
    private int playerLevel = 1;
    private int playerKillCount = 0;
    private int enemyDeathCount = 0;
    private int enemyLevel = 1;
    private static final int KILLS_PER_PLAYER_LEVEL = 5;
    private static final int DEATHS_PER_ENEMY_LEVEL = 5;
    private static final int KILLS_FOR_PORTAL = 5; // Portal appears after 5 kills
    private static final int BASE_ENEMY_HEALTH = 2;
    private int survivalTime = 0; // Track how long player survived
    private boolean gameOver = false;
    
    // Story and stage system
    private int currentStage = 1; // Stage 1: Kill enemies, Stage 2: Rescue female
    private boolean portalUsed = false;
    private boolean femaleRescued = false;
    private boolean keyCollected = false; // Track if player has the key
    private String storyMessage = "";
    private int storyDisplayTime = 0;
    
    public GamePanel() {
        // Increase panel size to accommodate larger maze and UI area
        setPreferredSize(new Dimension(MAZE_WIDTH * TILE_SIZE, MAZE_HEIGHT * TILE_SIZE + 200));
        setBackground(Color.BLACK);
        setFocusable(true);
        
        initializeGame();
        
        // Set up game timer for smooth updates
        gameTimer = new Timer(1000 / FPS, this);
        gameTimer.start();
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (playerTurn && !player.isMoving()) {
                    handleKeyPress(e.getKeyCode());
                }
            }
        });
    }
    
    /**
     * Set the assets path for all game resources (tiles and characters)
     * @param path The directory path where assets are stored (should end with /)
     */
    public static void setAssetsPath(String path) {
        Tile.ASSETS_PATH = path;
    }
    
    private void initializeGame() {
        // Ensure assets path is set correctly (adjust the path as needed)
        setAssetsPath("c:/Users/ASV/Downloads/Ayam Jago/assets/");
        
        // Initialize maze
        maze = new Tile[MAZE_HEIGHT][MAZE_WIDTH];
        createMaze();
        
        // Create player at position (1,1) - use the correct PlayerRooster from model package
        player = new PlayerRooster(1, 1, TILE_SIZE);
        
        // Add just one enemy at the opposite corner (only in stage 1)
        enemy = new EnemyRooster(MAZE_HEIGHT - 2, MAZE_WIDTH - 2, TILE_SIZE);
        
        // No corn, portal, female, or key at the beginning
        corn = null;
        portal = null;
        femaleRooster = null;
        key = null;
        
        playerTurn = true;
        turnCounter = 0;
        
        playerLevel = 1;
        playerKillCount = 0;
        enemyDeathCount = 0;
        enemyLevel = 1;
        survivalTime = 0;
        gameOver = false;
        
        // Story system initialization
        currentStage = 1;
        portalUsed = false;
        femaleRescued = false;
        keyCollected = false;
        showStoryMessage("Your beloved female has been captured by the evil farmer!\nDefeat his guard roosters to find the portal to her rescue!", 300);
        
        // Set initial enemy health based on enemy level
        enemy.setHealth(BASE_ENEMY_HEALTH + (enemyLevel - 1));
    }
    
    private void createMaze() {
        // Create a simple maze with walls at the borders and some random walls inside
        for (int row = 0; row < MAZE_HEIGHT; row++) {
            for (int col = 0; col < MAZE_WIDTH; col++) {
                boolean isWall = (col == 0 || row == 0 || col == MAZE_WIDTH - 1 || row == MAZE_HEIGHT - 1);
                
                // Add some internal walls
                if (!isWall && (col % 3 == 0 && row % 2 == 0)) {
                    isWall = true;
                }
                
                maze[row][col] = new Tile(isWall);
            }
        }
        
        // Ensure starting position and some paths are clear
        maze[1][1].setWall(false);
        maze[1][2].setWall(false);
        maze[2][1].setWall(false);
        
        // Ensure enemy positions are clear
        maze[MAZE_HEIGHT - 2][MAZE_WIDTH - 2].setWall(false);
    }
    
    private void handleKeyPress(int keyCode) {
        // Don't process if player is moving or it's not the player's turn
        if (player.isMoving() || !playerTurn) {
            return;
        }
        
        int dRow = 0, dCol = 0;
        
        switch (keyCode) {
            case KeyEvent.VK_UP:    dRow = -1; break;
            case KeyEvent.VK_DOWN:  dRow = 1;  break;
            case KeyEvent.VK_LEFT:  dCol = -1; break;
            case KeyEvent.VK_RIGHT: dCol = 1;  break;
            default: return;  // Ignore other keys
        }
        
        // Calculate target position
        int targetRow = player.getRow() + dRow;
        int targetCol = player.getCol() + dCol;
        
        // Check if target position has portal (stage 1)
        if (currentStage == 1 && portal != null && portal.isActive() && 
            targetRow == portal.getRow() && targetCol == portal.getCol()) {
            // Player enters portal - transition to stage 2
            enterPortal();
            return;
        }
        
        // Check if target position has key (stage 2)
        if (currentStage == 2 && key != null && key.isActive() &&
            targetRow == key.getRow() && targetCol == key.getCol()) {
            // Player collects the key
            collectKey();
            // Don't return - allow the move to continue
        }
        
        // Check if target position has female rooster (stage 2 and player has key)
        if (currentStage == 2 && femaleRooster != null && femaleRooster.isActive() &&
            targetRow == femaleRooster.getRow() && targetCol == femaleRooster.getCol()) {
            if (keyCollected) {
                // Player rescues the female rooster
                rescueFemale();
                return;
            } else {
                // Player doesn't have the key yet
                showStoryMessage("You need the key to unlock her cage!\nFind the key first!", 150);
                return;
            }
        }
        
        // Check if target position has enemy (collision detection) - only in stage 1
        if (currentStage == 1 && enemy != null && 
            targetRow == enemy.getRow() && targetCol == enemy.getCol()) {
            // Collision with enemy - attack without moving
            handlePlayerAttackCollision();
            
            // Count this as a move but don't actually move the player
            player.incrementMoveCount();
            
            // Check if player has used both moves after attack
            if (player.getMoveCount() >= 2) {
                playerTurn = false; // Enemy turn next
                
                // Increase turn counter
                turnCounter++;
                
                // 25% chance to spawn corn every 3 turns if no corn exists
                if (corn == null && turnCounter % 3 == 0 && random.nextInt(100) < 25) {
                    corn = CornItem.createAtRandomPosition(maze, player, enemy, TILE_SIZE);
                }
            }
        } else {
            // No collision - try to move player normally
            if (player.move(dRow, dCol, maze)) {
                // Check if player collected corn after successful move
                checkCornCollection();
                
                // In stage 2, player can move freely without turn limits
                if (currentStage == 1) {
                    // If player has used both moves, end their turn
                    if (player.getMoveCount() >= 2) {
                        playerTurn = false; // Enemy turn next
                        
                        // Increase turn counter
                        turnCounter++;
                        
                        // 25% chance to spawn corn every 3 turns if no corn exists
                        if (corn == null && turnCounter % 3 == 0 && random.nextInt(100) < 25) {
                            corn = CornItem.createAtRandomPosition(maze, player, enemy, TILE_SIZE);
                        }
                    }
                } else {
                    // Stage 2: reset move count immediately for free movement
                    player.resetMoveCount();
                    playerTurn = true;
                }
            }
        }
    }
    
    private void enterPortal() {
        currentStage = 2;
        portalUsed = true;
        
        // Clear the maze and create a new one for stage 2
        createStage2Maze();
        
        // Respawn player at new starting position (top)
        player.setRow(1);
        player.setCol(MAZE_WIDTH / 2);
        
        // Remove enemy in stage 2 - no more combat
        enemy = null;
        
        // Spawn the key somewhere in the middle area
        spawnKey();
        
        // Spawn the captured female rooster at the bottom
        femaleRooster = new FemaleRooster(MAZE_HEIGHT - 2, MAZE_WIDTH / 2, TILE_SIZE);
        
        // Remove portal and corn
        portal = null;
        corn = null;
        
        // Reset player movement - free movement in stage 2
        player.resetMoveCount();
        playerTurn = true;
        
        showStoryMessage("You've entered the farmer's lair!\nFind the key to unlock your beloved's cage!", 200);
    }
    
    private void createStage2Maze() {
        // Create a simpler maze layout for stage 2 (farmer's lair)
        for (int row = 0; row < MAZE_HEIGHT; row++) {
            for (int col = 0; col < MAZE_WIDTH; col++) {
                boolean isWall = (col == 0 || row == 0 || col == MAZE_WIDTH - 1 || row == MAZE_HEIGHT - 1);
                
                // Add fewer internal walls for easier navigation
                if (!isWall && (row % 4 == 0 && col % 3 == 0)) {
                    isWall = true;
                }
                
                maze[row][col] = new Tile(isWall);
            }
        }
        
        // Ensure key positions are clear
        maze[1][MAZE_WIDTH / 2].setWall(false); // Player start position (top center)
        maze[2][MAZE_WIDTH / 2].setWall(false); // Path from player start
        
        // Clear path to female position (bottom center)
        maze[MAZE_HEIGHT - 2][MAZE_WIDTH / 2].setWall(false); // Female position
        maze[MAZE_HEIGHT - 3][MAZE_WIDTH / 2].setWall(false); // Path to female
        
        // Clear some areas around the center for key placement
        for (int row = MAZE_HEIGHT / 2 - 1; row <= MAZE_HEIGHT / 2 + 1; row++) {
            for (int col = MAZE_WIDTH / 2 - 2; col <= MAZE_WIDTH / 2 + 2; col++) {
                if (row >= 0 && row < MAZE_HEIGHT && col >= 0 && col < MAZE_WIDTH) {
                    maze[row][col].setWall(false);
                }
            }
        }
    }
    
    private void spawnKey() {
        // Spawn key in the middle area of the maze
        int attempts = 0;
        do {
            int row = MAZE_HEIGHT / 2 + random.nextInt(3) - 1; // Around middle height
            int col = MAZE_WIDTH / 2 + random.nextInt(5) - 2; // Around middle width
            
            if (row > 0 && row < MAZE_HEIGHT - 1 && col > 0 && col < MAZE_WIDTH - 1 &&
                !maze[row][col].isWall() && 
                !(row == player.getRow() && col == player.getCol()) &&
                !(row == MAZE_HEIGHT - 2 && col == MAZE_WIDTH / 2)) { // Not at female position
                key = new KeyItem(row, col, TILE_SIZE);
                break;
            }
            attempts++;
        } while (attempts < 50);
        
        // Fallback if no valid position found
        if (key == null) {
            key = new KeyItem(MAZE_HEIGHT / 2, MAZE_WIDTH / 2 - 3, TILE_SIZE);
        }
    }
    
    private void collectKey() {
        keyCollected = true;
        key.setActive(false);
        key = null;
        showStoryMessage("You found the key!\nNow go rescue your beloved at the bottom of the lair!", 150);
    }
    
    private void rescueFemale() {
        femaleRescued = true;
        femaleRooster.setActive(false);
        showStoryMessage("Congratulations! You've rescued your beloved!\nYou both escape to safety and live happily ever after!", 400);
        
        // End the game successfully after a delay
        Timer endGameTimer = new Timer(5000, e -> {
            gameOver = true;
            AyamJagoLabirinGame.showVictory(playerLevel, playerKillCount, survivalTime);
        });
        endGameTimer.setRepeats(false);
        endGameTimer.start();
    }
    
    private void showStoryMessage(String message, int duration) {
        storyMessage = message;
        storyDisplayTime = duration;
    }
    
    private void handlePlayerAttackCollision() {
        // Player attacks enemy without moving - both take damage
        player.setHealth(player.getHealth() - 1);
        enemy.setHealth(enemy.getHealth() - 1);
        System.out.println("Player attacks (collision)! Player stays in place. Player health: " + player.getHealth() + ", Enemy health: " + enemy.getHealth());
        
        // Check if enemy is defeated
        if (enemy.getHealth() <= 0) {
            System.out.println("Enemy defeated!");
            
            // Increase kill and death counters
            playerKillCount++;
            enemyDeathCount++;
            
            // Check for portal spawning in stage 1
            if (currentStage == 1 && playerKillCount >= KILLS_FOR_PORTAL && portal == null) {
                spawnPortal();
                showStoryMessage("A magical portal has appeared!\nEnter it to reach the farmer's lair and rescue your beloved!", 200);
            }
            
            // Check for player level up
            if (playerKillCount >= KILLS_PER_PLAYER_LEVEL) {
                levelUpPlayer();
            }
            
            // Check for enemy level up (every 5 deaths)
            if (enemyDeathCount % DEATHS_PER_ENEMY_LEVEL == 0) {
                levelUpEnemy();
            }
            
            // Respawn enemy with current enemy level health
            respawnEnemy();
        }
        
        // Check if player is defeated - GAME OVER (no respawn)
        if (player.getHealth() <= 0) {
            System.out.println("Game Over! Player defeated.");
            gameOver = true;
            AyamJagoLabirinGame.showGameOver(playerLevel, playerKillCount, survivalTime);
        }
    }
    
    private void spawnPortal() {
        // Spawn portal at a random valid location
        do {
            int row = random.nextInt(MAZE_HEIGHT - 2) + 1;
            int col = random.nextInt(MAZE_WIDTH - 2) + 1;
            
            if (!maze[row][col].isWall() && 
                !(row == player.getRow() && col == player.getCol()) &&
                !(row == enemy.getRow() && col == enemy.getCol()) &&
                (corn == null || !(row == corn.getRow() && col == corn.getCol()))) {
                portal = new Portal(row, col, TILE_SIZE);
                break;
            }
        } while (true);
    }
    
    private void respawnEnemy() {
        // Calculate enemy health based on how many times it has died
        int enemyHealthBonus = (enemyDeathCount / DEATHS_PER_ENEMY_LEVEL);
        int newEnemyHealth = BASE_ENEMY_HEALTH + enemyHealthBonus;
        
        // Create a new enemy at corner
        enemy = new EnemyRooster(MAZE_HEIGHT - 2, MAZE_WIDTH - 2, TILE_SIZE);
        enemy.setHealth(newEnemyHealth);
        
        System.out.println("New enemy spawned with " + newEnemyHealth + " health");
        System.out.println("Enemy respawn count: " + enemyDeathCount + " (Level bonus: +" + enemyHealthBonus + ")");
    }
    
    private void processEnemyTurn() {
        // Only process enemy turn in stage 1
        if (currentStage != 1 || enemy == null || enemy.isMoving()) {
            // In stage 2, immediately return to player turn
            if (currentStage == 2) {
                player.resetMoveCount();
                playerTurn = true;
            }
            return;
        }
        
        // Debug output
        System.out.println("Processing enemy turn...");
        
        // Calculate where enemy wants to move
        int playerRow = player.getRow();
        int playerCol = player.getCol();
        int enemyRow = enemy.getRow();
        int enemyCol = enemy.getCol();
        
        // Calculate movement direction - only one direction at a time (no diagonal)
        int dRow = 0, dCol = 0;
        
        // Prioritize the direction with greater distance
        int rowDiff = Math.abs(playerRow - enemyRow);
        int colDiff = Math.abs(playerCol - enemyCol);
        
        if (rowDiff > colDiff) {
            // Move vertically first
            if (playerRow < enemyRow) dRow = -1;
            else if (playerRow > enemyRow) dRow = 1;
        } else if (colDiff > rowDiff) {
            // Move horizontally first
            if (playerCol < enemyCol) dCol = -1;
            else if (playerCol > enemyCol) dCol = 1;
        } else if (rowDiff > 0 || colDiff > 0) {
            // Equal distance, choose randomly between horizontal and vertical
            if (random.nextBoolean()) {
                // Move vertically
                if (playerRow < enemyRow) dRow = -1;
                else if (playerRow > enemyRow) dRow = 1;
            } else {
                // Move horizontally
                if (playerCol < enemyCol) dCol = -1;
                else if (playerCol > enemyCol) dCol = 1;
            }
        }
        
        // Check if enemy would move into player's position (collision)
        int targetRow = enemyRow + dRow;
        int targetCol = enemyCol + dCol;
        
        // Only allow attack if enemy is adjacent (not diagonal) to player
        boolean isAdjacentHorizontally = (enemyRow == playerRow) && (Math.abs(enemyCol - playerCol) == 1);
        boolean isAdjacentVertically = (enemyCol == playerCol) && (Math.abs(enemyRow - playerRow) == 1);
        
        if ((targetRow == playerRow && targetCol == playerCol) && 
            (isAdjacentHorizontally || isAdjacentVertically)) {
            // Enemy would collide with player and is adjacent (not diagonal) - attack without moving
            handleEnemyAttackCollision();
            System.out.println("Enemy attacks without moving! Enemy stays at: " + enemyRow + "," + enemyCol);
        } else {
            // No collision or not adjacent - enemy tries to move towards player
            enemy.moveTowardsPlayer(player, maze);
        }
        
        // Return to player's turn
        player.resetMoveCount();
        playerTurn = true;
    }
    
    private void handleEnemyAttackCollision() {
        // Enemy attacks player without moving - only player takes damage
        player.setHealth(player.getHealth() - 1);
        System.out.println("Enemy attacks (collision)! Enemy stays in place. Player health: " + player.getHealth());
        
        // Check if player is defeated - GAME OVER (no respawn)
        if (player.getHealth() <= 0) {
            System.out.println("Game Over! Player defeated.");
            gameOver = true;
            AyamJagoLabirinGame.showGameOver(playerLevel, playerKillCount, survivalTime);
        }
    }
    
    private void levelUpPlayer() {
        playerLevel++;
        playerKillCount = 0; // Reset kill count
        
        // Give player health bonus
        int healthBonus = 3;
        player.setHealth(player.getHealth() + healthBonus);
        
        System.out.println("PLAYER LEVEL UP! Level: " + playerLevel);
        System.out.println("Health bonus: +" + healthBonus + ". Current health: " + player.getHealth());
    }
    
    private void levelUpEnemy() {
        enemyLevel++;
        
        System.out.println("ENEMY LEVEL UP! Enemy Level: " + enemyLevel);
        System.out.println("Next enemies will be stronger!");
    }
    
    private void checkCornCollection() {
        if (corn != null && corn.isActive() && 
            player.getRow() == corn.getRow() && player.getCol() == corn.getCol()) {
            
            // Player collects corn - heal 3 health
            player.setHealth(player.getHealth() + 3);
            System.out.println("Player collected corn! Health +3. Current health: " + player.getHealth());
            
            // Deactivate the corn
            corn.setActive(false);
            corn = null;
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            updateGame();
            repaint();
        }
    }
    
    private void updateGame() {
        if (gameOver) return;
        
        player.update();
        
        // Only update enemy in stage 1
        if (currentStage == 1 && enemy != null) {
            enemy.update();
        }
        
        // Update corn animation if it exists
        if (corn != null && corn.isActive()) {
            corn.update();
        }
        
        // Update portal animation if it exists
        if (portal != null && portal.isActive()) {
            portal.update();
        }
        
        // Update key animation if it exists
        if (key != null && key.isActive()) {
            key.update();
        }
        
        // Update female rooster animation if it exists
        if (femaleRooster != null && femaleRooster.isActive()) {
            femaleRooster.update();
        }
        
        // Decrement story display time
        if (storyDisplayTime > 0) {
            storyDisplayTime--;
        }
        
        // Increment survival time
        survivalTime++;
        
        // If no animations are in progress and it's enemy's turn, process enemy turn
        if (!player.isMoving() && 
            (currentStage == 1 ? (enemy == null || !enemy.isMoving()) : true) && 
            !playerTurn) {
            processEnemyTurn();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (gameOver) {
            // Draw game over screen
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", getWidth()/2 - 150, getHeight()/2);
            return;
        }
        
        // Draw maze
        for (int row = 0; row < MAZE_HEIGHT; row++) {
            for (int col = 0; col < MAZE_WIDTH; col++) {
                // Gambar tile menggunakan method draw milik Tile
                maze[row][col].draw(g, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE);
            }
        }
        
        // Draw corn if it exists and is active
        if (corn != null && corn.isActive()) {
            corn.draw(g);
        }
        
        // Draw portal if it exists and is active (stage 1 only)
        if (currentStage == 1 && portal != null && portal.isActive()) {
            portal.draw(g);
        }
        
        // Draw key if it exists and is active (stage 2 only)
        if (currentStage == 2 && key != null && key.isActive()) {
            key.draw(g);
        }
        
        // Draw female rooster if it exists and is active (stage 2 only)
        if (currentStage == 2 && femaleRooster != null && femaleRooster.isActive()) {
            femaleRooster.draw(g);
        }
        
        // Draw enemy only in stage 1
        if (currentStage == 1 && enemy != null) {
            enemy.draw(g);
        }
        
        // Draw player
        player.draw(g);
        
        // Draw story message if active
        if (storyDisplayTime > 0 && !storyMessage.isEmpty()) {
            drawStoryMessage(g);
        }
        
        // Move UI to bottom of the game area to not obstruct gameplay
        int uiStartY = MAZE_HEIGHT * TILE_SIZE + 10;
        
        // Draw stage indicator
        g.setColor(Color.BLACK);
        g.fillRect(5, uiStartY, 500, 25);
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        String stageText;
        if (currentStage == 1) {
            stageText = "Stage 1: Defeat Guards (" + playerKillCount + "/" + KILLS_FOR_PORTAL + ")";
        } else {
            String keyStatus = keyCollected ? "✓ Key Collected" : "Find the Key";
            stageText = "Stage 2: " + keyStatus + " - Rescue Your Beloved!";
        }
        g.drawString(stageText, 10, uiStartY + 15);
        
        // Draw turn indicator (only relevant in stage 1)
        if (currentStage == 1) {
            g.setColor(Color.BLACK);
            g.fillRect(5, uiStartY + 30, 200, 25);
            g.setColor(playerTurn ? Color.GREEN : Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            if (playerTurn) {
                g.drawString("Player Turn: " + (2 - player.getMoveCount()) + " moves left", 10, uiStartY + 45);
            } else {
                g.drawString("Enemy Turn", 10, uiStartY + 45);
            }
        } else {
            // Stage 2: Free movement
            g.setColor(Color.BLACK);
            g.fillRect(5, uiStartY + 30, 200, 25);
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Free Movement - Find the Key!", 10, uiStartY + 45);
        }
        
        // Draw health and level information
        g.setColor(Color.BLACK);
        g.fillRect(5, uiStartY + 60, 500, 100);
        g.setColor(Color.WHITE);
        g.drawString("Player Health: " + player.getHealth(), 10, uiStartY + 75);
        
        if (currentStage == 1 && enemy != null) {
            g.drawString("Enemy Health: " + enemy.getHealth(), 200, uiStartY + 75);
        } else if (currentStage == 2) {
            g.setColor(Color.YELLOW);
            g.drawString("Key Status: " + (keyCollected ? "Collected ✓" : "Not Found"), 200, uiStartY + 75);
        }
        
        // Draw level information
        g.setColor(Color.YELLOW);
        g.drawString("Player Level: " + playerLevel, 10, uiStartY + 95);
        g.drawString("Kills: " + playerKillCount + "/" + KILLS_PER_PLAYER_LEVEL, 10, uiStartY + 115);
        
        g.setColor(Color.RED);
        g.drawString("Enemy Deaths: " + enemyDeathCount, 200, uiStartY + 95);
        
        g.setColor(Color.CYAN);
        g.drawString("Survival Time: " + survivalTime + " turns", 10, uiStartY + 135);
    }
    
    private void drawStoryMessage(Graphics g) {
        // Draw semi-transparent background
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(50, 100, MAZE_WIDTH * TILE_SIZE - 100, 150);
        
        // Draw border
        g.setColor(Color.YELLOW);
        g.drawRect(50, 100, MAZE_WIDTH * TILE_SIZE - 100, 150);
        
        // Draw story text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Split message into lines
        String[] lines = storyMessage.split("\n");
        int startY = 130;
        for (int i = 0; i < lines.length; i++) {
            g.drawString(lines[i], 60, startY + (i * 25));
        }
    }
}
