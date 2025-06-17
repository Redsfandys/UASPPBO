package main;

import model.*;

import javax.imageio.ImageIO;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static model.Tile.setAssetsPath;

public class GamePanel extends JPanel implements ActionListener {
    private static final int TILE_SIZE = 34;
    private static final int MAZE_WIDTH = 25;
    private static final int MAZE_HEIGHT = 20;
    private static final int FPS = 60;
    
    private Tile[][] maze;
    private PlayerRooster player;
    private EnemyRooster enemy;
    private CornItem corn;
    private Portal portal;
    private FemaleRooster femaleRooster; // The captured female to rescue
    private Farmer farmer; //
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
    private static final int KILLS_FOR_PORTAL = 10; // Portal appears after 5 kills
    private static final int BASE_ENEMY_HEALTH = 2;
    private int survivalTime = 0; // Track how long player survived
    private boolean gameOver = false;

    private int totalEnemiesKilled = 0;


    private int doorRow;
    private int doorCol;

    private BufferedImage houseImage;
    private int houseRow = -1; // Use -1 to indicate we haven't found it yet
    private int houseCol = -1;


    // Story and stage system
    private int currentStage = 1; // Stage 1: Kill enemies, Stage 2: Rescue female
    private boolean portalUsed = false;
    private boolean femaleRescued = false;
    private boolean keyCollected = false; // Track if player has the key
    private String storyMessage = "";
    private int storyDisplayTime = 0;

//    decoration
    private java.util.List<Decoration> decorations;
    private static BufferedImage grassImage;
    private static BufferedImage rockImage;
    private static BufferedImage flowersImage;

//    attack
    private java.util.List<AttackEffect> attackEffects;
    private java.util.List<DeathCollectible> deathCollectibles;
    private static BufferedImage deathSprite;
    private boolean playerIsDead = false;
    private boolean keyHasBeenDropped = false;

    private float currentVolume = 0.7f; // Start at 70% volume
    private BufferedImage currentPortrait;



    //    stages
private String[] stage1_layout = {
        "WWWWWWWWWWWWWWWWWWWWWWWWW",
        "WP............W.........W",
        "W..WWW.......W.W......W.W",
        "W...W....W.....W....W...W",
        "W......W...W......W.....W",
        "WW....W..........W......W",
        "W....W....W...W....WWW..W",
        "W.W......W....W..W......W",
        "W.W..W.......W......WW..W",
        "W........W..............W",
        "W..W........W......W....W",
        "W...W....W..............W",
        "WW........WWW......W....W",
        "W........W....W.........W",
        "W..WWW........WWWWW.....W",
        "W........W..............W",
        "W..W....W....W....WW....W",
        "W......W........W.......W",
        "W.WW....W....W.......W.EW",
        "WWWWWWWWWWWWWWWWWWWWWWWWW",
    };// Note: 'P' can be a floor tile where the player starts. We'll ignore it during loading.

    private String[] stage2_layout = {
            "WWWWWWWWWWWWWWWWWWWWWWWWW",
            "W.....W.....S.......W...W",
            "W.W...............W...W.W",
            "W.W....WW....W.........WW",
            "W........W..W.WW...W....W",
            "WW..W......W........W...W",
            "W...W.WW..........W.....W",
            "W.......W..WWW........W.W",
            "W.WW.W............WW....W",
            "W....W..W....W........W.W",
            "W.W........W....WW......W",
            "W....WW..M...W......W...W",
            "WWWWWWW....W............W",
            "W....W..................W",
            "W..W....WW...W..HHH.....W",
            "W........W......HHH.....W",
            "W.W..W..WWWWWWWDWWW.....W",
            "W..W....W.........WWW...W",
            "W....W..W....W.F..W.....W",
            "WWWWWWWWWWWWWWWWWWWWWWWWW",
    };
// Map Key: S = Player Start, K = Key Location, D = Door, F = Female Rooster

    public GamePanel() {
        // Increase panel size to accommodate larger maze and UI area

        setPreferredSize(new Dimension(MAZE_WIDTH * TILE_SIZE, MAZE_HEIGHT * TILE_SIZE + 200));
        setBackground(Color.BLACK);
        setFocusable(true);
        this.attackEffects = new java.util.ArrayList<>(); // Add this line
        this.deathCollectibles = new java.util.ArrayList<>(); // Add this line
        SoundManager.getInstance().setMusicVolume(currentVolume);

        initializeGame();

        // Set up game timer for smooth updates
        gameTimer = new Timer(1000 / FPS, this);
        gameTimer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // If a story message is currently showing, this key press will dismiss it.
                if (storyDisplayTime > 0) {
                    storyDisplayTime = 0; // Set the timer to 0 to hide the message
                    return; //
                }

                if (playerIsDead){
                    return;
                }
                if (playerTurn && !player.isMoving()) {
                    // Increment survival time
                    survivalTime++;
                    handleKeyPress(e.getKeyCode());
                }
            }
        });

    }
    
    /**
     * Set the assets path for all game resources (tiles and characters)
     */
    private static void setupAssetsPaths() {
        // Get the current working directory (your project's root folder)
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentDir);

        // Define the assets path relative to the project folder
        String assetsPath = currentDir + File.separator + "assets" + File.separator;

        // Create the assets directory if it doesn't exist (this is good practice)
        File assetsDir = new File(assetsPath);
        if (!assetsDir.exists()) {
            System.out.println("CRITICAL ERROR: 'assets' directory not found at: " + assetsPath);
            return;
        }

        // Set the assets path for tiles and characters
        String javaCompatiblePath = assetsPath.replace('\\', '/');
        System.out.println("Setting assets path to: " + javaCompatiblePath);

        try {
            Tile.ASSETS_PATH = javaCompatiblePath;
            model.character.Character.IMAGE_BASE_PATH = javaCompatiblePath; // Use fully qualified name if needed
        } catch (Exception e) {
            System.out.println("Error setting asset paths: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void initializeGame() {
        // Ensure assets path is set correctly (adjust the path as needed)
        setAssetsPath("assets/");
        
        // Initialize maze
        maze = new Tile[MAZE_HEIGHT][MAZE_WIDTH];
        createMaze();

        loadDecorationImages(); // Load the art
        generateDecorations(40); // Generate 40 random decorations

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
        totalEnemiesKilled = 0; // Add this line

        // Story system initialization
        currentStage = 1;
        portalUsed = false;
        femaleRescued = false;
        keyCollected = false;
        keyHasBeenDropped = false;
        showStoryMessage("Your beloved female has been captured by the evil farmer!\nDefeat his guard roosters to find the portal to her rescue!", 300, "assets/betina-idle1.png");
        
        // Set initial enemy health based on enemy level
        enemy.setHealth(BASE_ENEMY_HEALTH + (enemyLevel - 1));
    }

    private void createMaze() {
        // Loop through our text-based map layout
        for (int row = 0; row < MAZE_HEIGHT; row++) {
            for (int col = 0; col < MAZE_WIDTH; col++) {
                // Make sure we don't go out of bounds of our layout array
                if (row >= stage1_layout.length || col >= stage1_layout[row].length()) {
                    maze[row][col] = new Tile(true); // Default to a wall if map is too small
                    continue;
                }

                // Get the character from our layout
                char tileChar = stage1_layout[row].charAt(col);

                // If the character is 'W', it's a wall. Treat anything else as floor.
                boolean isWall = (tileChar == 'W');

                maze[row][col] = new Tile(isWall);
            }
        }

        // Ensure player start position is clear, just in case.
        maze[1][1].setWall(false);


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
            case KeyEvent.VK_E:     crow(); return; // Call crow() and stop
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
            SoundManager.playSound("assets/teleport.wav");
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
                showStoryMessage("You need the key to unlock her cage!\nFind the key first!", 150, "assets/key_1.png");
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
                checkCollectibleCollection();

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
        deathCollectibles.clear();

        // Reset player movement - free movement in stage 2
        player.resetMoveCount();
        playerTurn = true;
        
        showStoryMessage("You've entered the farmer's lair!\nFind the key to unlock your beloved's cage!\ntry to wake the farmer up\nYou can Crow by pressing E", 400, "assets/key_1.png");
    }

    private void createStage2Maze() {
        this.houseRow = -1;
        this.houseCol = -1;

        // Create a simpler maze layout for stage 2 (farmer's lair)
        for (int row = 0; row < MAZE_HEIGHT; row++) {
            for (int col = 0; col < MAZE_WIDTH; col++) {
                if (row >= stage2_layout.length || col >= stage2_layout[row].length()) {
                    maze[row][col] = new Tile(true); // Default to a wall
                    continue;
                }

                char tileChar = stage2_layout[row].charAt(col);
                boolean isWall = (tileChar == 'W' || tileChar == 'D' || tileChar == 'H');


                maze[row][col] = new Tile(isWall);

                // If we find the door, save its coordinates
                if (tileChar == 'D') {
                    this.doorRow = row;
                    this.doorCol = col;
                }
                if (tileChar == 'H' && this.houseRow == -1) {
                    // ...save this as the top-left corner.
                    this.houseRow = row;
                    this.houseCol = col;
                }
                if (tileChar == 'M') {
                    this.farmer = new Farmer(row, col, TILE_SIZE);
                }

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
//        int attempts = 0;
//        do {
//            int row = MAZE_HEIGHT / 2 + random.nextInt(3) - 1; // Around middle height
//            int col = MAZE_WIDTH / 2 + random.nextInt(5) - 2; // Around middle width
//
//            if (row > 0 && row < MAZE_HEIGHT - 1 && col > 0 && col < MAZE_WIDTH - 1 &&
//                !maze[row][col].isWall() &&
//                !(row == player.getRow() && col == player.getCol()) &&
//                !(row == MAZE_HEIGHT - 2 && col == MAZE_WIDTH / 2)) { // Not at female position
//                key = new KeyItem(row, col, TILE_SIZE);
//                break;
//            }
//            attempts++;
//        } while (attempts < 50);
//
//        // Fallback if no valid position found
//        if (key == null) {
//            key = new KeyItem(MAZE_HEIGHT / 2, MAZE_WIDTH / 2 - 3, TILE_SIZE);
//        }
    }

    private void collectKey() {
        SoundManager.playSound("assets/key.wav");
        keyCollected = true;
        if (key != null) {
            key.setActive(false); // the key object is inactive
        }
        key = null; // removes the key from the GamePanel's direct knowledge

        // Open the door
        maze[doorRow][doorCol].setWall(false);

        showStoryMessage("You found the key! A nearby door unlocked.", 200, "assets/key_1.png");
    }


    
    private void rescueFemale() {
        femaleRescued = true;
        SoundManager.playSound("assets/rescue.wav"); // Play the victory jingle!
//        femaleRooster.setActive(false);
        showStoryMessage("Congratulations! You've rescued your beloved!\nYou both escape to safety and live happily ever after!", 400, "assets/betina-idle1.png");
        SoundManager.playSound("assets/victory.wav");
        // End the game successfully after a delay
        Timer endGameTimer = new Timer(5000, e -> {
            gameOver = true;
            AyamJagoLabirinGame.showVictory(playerLevel, totalEnemiesKilled, survivalTime);
        });
        endGameTimer.setRepeats(false);
        endGameTimer.start();
    }
    
    private void showStoryMessage(String message, int duration) {
        currentPortrait = null;
        storyMessage = message;
        storyDisplayTime = duration;
    }
    private void showStoryMessage(String message, int duration, String portraitPath) {
        this.storyMessage = message;
        this.storyDisplayTime = duration;
        if (portraitPath != null && !portraitPath.isEmpty()) {
            try {
                currentPortrait = ImageIO.read(new File(portraitPath));
            } catch (IOException e) {
                System.out.println("Error loading portrait: " + portraitPath + " - " + e.getMessage());
                currentPortrait = null; // Handle loading failure
            }
        } else {
            currentPortrait = null; // No portrait for this message
        }
    }




    private void handlePlayerAttackCollision() {
        // Player attacks enemy without moving - both take damage
        SoundManager.playSound("assets/attack.wav"); // Play the attack sound
        player.setHealth(player.getHealth() - 1);
        enemy.setHealth(enemy.getHealth() - 1);
        SoundManager.playSound("assets/hurt.wav"); // Play the damage sound
        System.out.println("Player attacks (collision)! Player stays in place. Player health: " + player.getHealth() + ", Enemy health: " + enemy.getHealth());
        // Calculate the midpoint pixel position between player and enemy
// 1. Get the center pixel coordinates of the player
        int playerCenterX = player.getCol() * TILE_SIZE + (TILE_SIZE / 2);
        int playerCenterY = player.getRow() * TILE_SIZE + (TILE_SIZE / 2);

// 2. Get the center pixel coordinates of the enemy
        int enemyCenterX = enemy.getCol() * TILE_SIZE + (TILE_SIZE / 2);
        int enemyCenterY = enemy.getRow() * TILE_SIZE + (TILE_SIZE / 2);

// 3. Calculate the point exactly halfway between them
        int midX = (playerCenterX + enemyCenterX) / 2;
        int midY = (playerCenterY + enemyCenterY) / 2;

// 4. Create the effect at that precise midpoint
        attackEffects.add(new AttackEffect(midX, midY, player.getCurrentDirection()));


        // Check if enemy is defeated
        if (enemy.getHealth() <= 0) {
            System.out.println("Enemy defeated!");
            deathCollectibles.add(new DeathCollectible(enemy.getRow(), enemy.getCol(), TILE_SIZE));

            // Increase kill and death counters
            playerKillCount++;
            totalEnemiesKilled++; //

            enemyDeathCount++;
            
            // Check for portal spawning in stage 1
            if (currentStage == 1 && totalEnemiesKilled >= KILLS_FOR_PORTAL && portal == null) {
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
            if (!playerIsDead) { // Check the flag to ensure this only runs once
                System.out.println("Game Over! Player defeated.");
                playerIsDead = true;
                SoundManager.playSound("assets/lose.wav");
                // Create a Timer for the delay
                javax.swing.Timer gameOverTimer = new javax.swing.Timer(2000, e -> {
                    // runs after the delay (2000ms = 2 seconds)
                   gameOver = true; // Stop game logic and input
                    AyamJagoLabirinGame.showGameOver(playerLevel, playerKillCount, survivalTime);
                });

                gameOverTimer.setRepeats(false);
                gameOverTimer.start();
            }



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
                SoundManager.playSound("assets/portal_open.wav"); // Play sound on spawn
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
        SoundManager.playSound("assets/attack.wav"); // Enemy also makes an attack sound
        player.setHealth(player.getHealth() - 1);
        SoundManager.playSound("assets/hurt.wav"); // Player takes damage
        System.out.println("Enemy attacks (collision)! Enemy stays in place. Player health: " + player.getHealth());
// 1. Get the center pixel coordinates of the player
        int playerCenterX = player.getCol() * TILE_SIZE + (TILE_SIZE / 2);
        int playerCenterY = player.getRow() * TILE_SIZE + (TILE_SIZE / 2);

// 2. Get the center pixel coordinates of the enemy
        int enemyCenterX = enemy.getCol() * TILE_SIZE + (TILE_SIZE / 2);
        int enemyCenterY = enemy.getRow() * TILE_SIZE + (TILE_SIZE / 2);

// 3. Calculate the point exactly halfway between them
        int midX = (playerCenterX + enemyCenterX) / 2;
        int midY = (playerCenterY + enemyCenterY) / 2;
        int dx = enemy.getCol() - player.getCol();


// 4. Create the effect at that precise midpoint
        attackEffects.add(new AttackEffect(midX, midY, enemy.getCurrentDirection()));


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
            SoundManager.playSound("assets/eat.wav");
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
        // Update all active attack effects
        for (AttackEffect effect : attackEffects) {
            effect.update();
        }
        // Remove effects that are no longer active
        attackEffects.removeIf(effect -> !effect.isActive());

        // Only update enemy in stage 1
        if (currentStage == 1 && enemy != null) {
            enemy.update();
        }
        if (currentStage == 2 && farmer != null) {
            // We now check our new flag instead of if the key is null
            if (farmer.isGone() && !keyHasBeenDropped) {
                // Farmer has left and we haven't dropped the key yet. Let's do it!
                key = new KeyItem(farmer.getRow(), farmer.getCol(), TILE_SIZE);

                // NOW, WE SET THE FLAG so this code never runs again
                keyHasBeenDropped = true;

                showStoryMessage("The startled farmer runs off, dropping a key!", 150, "assets/farmer_wake_1.png");
            }
            farmer.update();
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

        // Draw all the decorations on top of the floor
        if (decorations != null) {
            for (Decoration deco : decorations) {
                deco.draw(g);
            }
        }

        for (DeathCollectible collectible : deathCollectibles) {
            collectible.draw(g);
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
        if (playerIsDead) {
            // If player is dead, draw the death sprite at their last location
            if (deathSprite != null) {
                g.drawImage(deathSprite, player.getCol() * TILE_SIZE, player.getRow() * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            }
        } else {
            // If player is alive, draw them normally
            player.draw(g);
        }
        if (currentStage == 2 && farmer != null) {
            farmer.draw(g);
        }
        // 2. Draw the large house image on top of the base tiles
        if (houseImage != null && houseRow != -1) {
            g.drawImage(houseImage, houseCol * TILE_SIZE - 16, houseRow * TILE_SIZE - 60, null);
        }

        // Draw all active attack effects on top of everything
        for (AttackEffect effect : attackEffects) {
            effect.draw(g);
        }

        // Draw story message if active
        if (storyDisplayTime > 0 && !storyMessage.isEmpty()) {
            drawStoryMessage(g);
        }

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
            stageText = "Stage 1: Defeat Guards (" + totalEnemiesKilled + "/" + KILLS_FOR_PORTAL + ")";
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
        int boxHeight = 150;
        int boxY = (MAZE_HEIGHT * TILE_SIZE) - boxHeight - 20; // 20 pixels of padding from the bottom

        // Draw semi-transparent background
        g.setColor(new Color(0, 0, 0, 50)); // Made it slightly more opaque
        g.fillRect(50, boxY, MAZE_WIDTH * TILE_SIZE - 100, boxHeight);

        // Draw border
        g.setColor(Color.YELLOW);
        g.drawRect(50, boxY, MAZE_WIDTH * TILE_SIZE - 100, boxHeight);

        // Draw story text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));

        // Split message into lines
        String[] lines = storyMessage.split("\n");
        // The text's Y position is now relative to the box's Y position
        int startY = boxY + 30;
        int textX = 60;

        if (currentPortrait != null) {
            int portraitX = 700; // Adjust position as needed
            int portraitY = boxY + (boxHeight / 2) - (currentPortrait.getHeight() / 2);
            g.drawImage(currentPortrait, portraitX, portraitY, player.getSize()*3, player.getSize()*3, null);
            textX = portraitX + currentPortrait.getWidth() + 20; // Adjust text X to be to the right
        } else {
            textX = 70; // Reset text X if no portrait
        }

        for (int i = 0; i < lines.length; i++) {
            g.drawString(lines[i], 60, startY + (i * 25));
        }
    }

    private void loadDecorationImages() {
        try {
            // Use the path from the Character class, which is already set up
            String assetsPath = model.character.Character.IMAGE_BASE_PATH;

            File grassFile = new File(assetsPath + "grass.png");
            if (grassFile.exists()) {
                grassImage = ImageIO.read(grassFile);
            }

            File rockFile = new File(assetsPath + "rock.png");
            if (rockFile.exists()) {
                rockImage = ImageIO.read(rockFile);
            }

            File flowersFile = new File(assetsPath + "flowers.png");
            if (flowersFile.exists()) {
                flowersImage = ImageIO.read(flowersFile);
            }


            File deathFile = new File(model.character.Character.IMAGE_BASE_PATH + "death_sprite.png");
            if (deathFile.exists()) {
                deathSprite = ImageIO.read(deathFile);
                DeathCollectible.setImage(deathSprite); // Set the static image for the class
            }

            File houseFile = new File(model.character.Character.IMAGE_BASE_PATH + "house.png");
            if (houseFile.exists()) {
                houseImage = ImageIO.read(houseFile);
            }

        } catch (IOException e) {
            System.out.println("Error loading decoration images: " + e.getMessage());
        }
    }
    private void generateDecorations(int count) {
        decorations = new java.util.ArrayList<>();
        java.util.List<BufferedImage> possibleDecorations = new java.util.ArrayList<>();

        // Add all loaded decoration images to a list
        if (grassImage != null) possibleDecorations.add(grassImage);
        if (rockImage != null) possibleDecorations.add(rockImage);
        if (flowersImage != null) possibleDecorations.add(flowersImage);

        if (possibleDecorations.isEmpty()) {
            System.out.println("No decoration images loaded, skipping generation.");
            return; // Can't generate if no images were loaded
        }

        int generatedCount = 0;
        while (generatedCount < count) {
            // Pick a random spot in the maze
            int row = random.nextInt(MAZE_HEIGHT);
            int col = random.nextInt(MAZE_WIDTH);

            // Check if the spot is a floor tile (not a wall) and not the player's start
            if (!maze[row][col].isWall() && !(row == 1 && col == 1)) {
                // Pick a random decoration image from our list
                BufferedImage randomImage = possibleDecorations.get(random.nextInt(possibleDecorations.size()));

                // Create the new decoration and add it to our list
                decorations.add(new Decoration(row, col, TILE_SIZE, randomImage));
                generatedCount++;
            }
        }
    }
    private void crow() {
        // Crow ability only works in stage 2 when the farmer is sleeping

        if (currentStage != 2 || farmer == null || !farmer.isSleeping()) {
            return;
        }

        // Define the range of the crow
        final int CROW_RANGE = 3;

        // Calculate distance from player to farmer
        int distance = Math.abs(player.getRow() - farmer.getRow()) + Math.abs(player.getCol() - farmer.getCol());

        if (distance <= CROW_RANGE) {
            showStoryMessage("You let out a mighty crow! The farmer stirs...", 120, "assets/player-idle1.png");
            farmer.wakeUp();
            SoundManager.playSound("assets/crow_sound.wav");

        } else {
            SoundManager.playSound("assets/crow_sound.wav");
            showStoryMessage("You crow, but are too far away to be heard.", 120, "assets/player-idle1.png");
        }
    }

    private void checkCollectibleCollection() {
        // Use an iterator to safely remove items while looping
        java.util.Iterator<DeathCollectible> iterator = deathCollectibles.iterator();
        while (iterator.hasNext()) {
            DeathCollectible collectible = iterator.next();
            if (player.getRow() == collectible.getRow() && player.getCol() == collectible.getCol()) {
                SoundManager.playSound("assets/eat.wav");
                // Player is on the same tile, collect it!
                boolean isHealthy = Math.random() < 0.8; // 80% chance to heal, 20% to get poisoned
                if (isHealthy) {
                    player.setHealth(player.getHealth() + 1); // Regenerate 1 health
                    System.out.println("Player collected food! Health +1. Current health: " + player.getHealth());
                } else{
                    player.setHealth(player.getHealth() - 1); // Regenerate 1 health
                    System.out.println("Poisoned food! Health -1. Current health: " + player.getHealth());
                    // Check if player is defeated - GAME OVER (no respawn)
                    if (player.getHealth() <= 0) {
                        System.out.println("Game Over! Player defeated.");
// Inside the 'if (player.getHealth() <= 0)' block
                        if (!playerIsDead) { // Check the flag to ensure this only runs once
                            System.out.println("Game Over! Player defeated.");
                            playerIsDead = true;

                            // Create a Timer for the delay
                            javax.swing.Timer gameOverTimer = new javax.swing.Timer(2000, e -> {
                                // This code runs after the delay (2000ms = 2 seconds)
                                gameOver = true; // Stop game logic and input
                                AyamJagoLabirinGame.showGameOver(playerLevel, playerKillCount, survivalTime);
                            });

                            gameOverTimer.setRepeats(false); // Make sure it only runs once
                            gameOverTimer.start();
                        }
                    }
                }
                iterator.remove(); // Remove the collectible from the list
            }
        }
    }



}
