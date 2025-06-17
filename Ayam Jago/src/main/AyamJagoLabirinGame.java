package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import model.Tile;
import model.character.Character;
import java.io.File;

public class AyamJagoLabirinGame {
    private static JFrame frame;
    
    public static void main(String[] args) {
        // Set up the assets path before creating any game components
        setupAssetsPaths();
        
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Ayam Jago Labirin - Survival Mode");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Updated size for larger maze (25x20) plus UI space
            frame.setSize(25 * 34 + 16, 20 * 34 + 200); // Width: 866px, Height: 880px
            frame.setLocationRelativeTo(null);
            
            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);
            
            frame.setVisible(true);
            
            // Ensure the frame is resized to fit the game panel exactly
            frame.pack();
        });
    }
    
    public static void showGameOver(int playerLevel, int enemiesKilled, int survivalTime) {
        String message = "GAME OVER!\n\n" +
                        "Final Stats:\n" +
                        "Player Level: " + playerLevel + "\n" +
                        "Enemies Killed: " + enemiesKilled + "\n" +
                        "Survival Time: " + survivalTime + " turns\n\n" +
                        "Your beloved is still captured...\n" +
                        "Play again?";
        
        int choice = JOptionPane.showConfirmDialog(frame, message, "Game Over", 
                                                  JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            // Restart the game
            frame.getContentPane().removeAll();
            GamePanel newGamePanel = new GamePanel();
            frame.add(newGamePanel);
            frame.revalidate();
            frame.repaint();
            newGamePanel.requestFocusInWindow();
        } else {
            // Exit the game
            System.exit(0);
        }
    }
    
    public static void showVictory(int playerLevel, int enemiesKilled, int survivalTime) {
        String message = "🎉 VICTORY! 🎉\n\n" +
                        "You successfully rescued your beloved!\n\n" +
                        "Final Stats:\n" +
                        "Player Level: " + playerLevel + "\n" +
                        "Enemies Defeated: " + enemiesKilled + "\n" +
                        "Total Time: " + survivalTime + " turns\n\n" +
                        "You both live happily ever after!\n" +
                        "Play again?";
        
        int choice = JOptionPane.showConfirmDialog(frame, message, "Victory!", 
                                                  JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            // Restart the game
            frame.getContentPane().removeAll();
            GamePanel newGamePanel = new GamePanel();
            frame.add(newGamePanel);
            frame.revalidate();
            frame.repaint();
            newGamePanel.requestFocusInWindow();
        } else {
            // Exit the game
            System.exit(0);
        }
    }
    
    private static void setupAssetsPaths() {
        // Get the current working directory
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentDir);
        
        // Define the assets path - using hardcoded path to ensure reliability
        String assetsPath = "c:" + File.separator + "Users" + File.separator + "ASV" + 
                         File.separator + "Downloads" + File.separator + "Ayam Jago" + 
                         File.separator + "assets" + File.separator;
        
        // Create the assets directory if it doesn't exist
        File assetsDir = new File(assetsPath);
        if (!assetsDir.exists()) {
            boolean created = assetsDir.mkdirs();
            System.out.println("Created assets directory: " + (created ? "success" : "failed"));
        }
        
        // Set the assets path for tiles and characters - use forward slashes for Java compatibility
        String javaCompatiblePath = assetsPath.replace('\\', '/');
        System.out.println("Setting assets path to: " + javaCompatiblePath);
        
        try {
            // Ensure the static fields exist in these classes
            Tile.ASSETS_PATH = javaCompatiblePath;
            Character.IMAGE_BASE_PATH = javaCompatiblePath;
        } catch (Exception e) {
            System.out.println("Error setting asset paths: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Verify that the assets directory contains the required files
        String[] requiredFiles = {"wall.png", "floor.png", "floor.jpeg", "player.png", "enemy.png", "corn.png"};
        System.out.println("Checking for asset files:");
        for (String file : requiredFiles) {
            File assetFile = new File(assetsPath + file);
            System.out.println(file + ": " + (assetFile.exists() ? "Found" : "Missing"));
        }
    }
}
