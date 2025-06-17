package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

import model.HighScoreEntry;
import model.Tile;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import java.io.File;
import java.util.List;

public class AyamJagoLabirinGame {
    private static JFrame frame;

    public static void main(String[] args) {
        // Set up the assets path before creating any game components
        SoundManager.getInstance().playMusic("assets/background_music.wav");

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
        String playerName = getPlayerNameInput();

        if (playerName != null && !playerName.isEmpty()) {
            saveHighScore(playerName, playerLevel, enemiesKilled, survivalTime, false);
        }
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
            frame.getContentPane().removeAll();
            GamePanel newGamePanel = new GamePanel();
            frame.add(newGamePanel);
            frame.revalidate();
            frame.repaint();
            newGamePanel.requestFocusInWindow();
        } else {
            System.exit(0);
        }

        showHighScores();
    }


    public static void showVictory(int playerLevel, int totalEnemiesKilled, int survivalTime) {
        String playerName = getPlayerNameInput();

        if (playerName != null && !playerName.isEmpty()) {
            saveHighScore(playerName, playerLevel, totalEnemiesKilled, survivalTime, true);
        }
        String message = "🎉 VICTORY! 🎉\n\n" +
                "You successfully rescued your beloved!\n\n" +
                "Final Stats:\n" +
                "Player Level: " + playerLevel + "\n" +
                "Enemies Defeated: " + totalEnemiesKilled + "\n" + // Changed this line
                "Total Time: " + survivalTime + " turns\n\n" +
                "You both live happily ever after!\n" +
                "Play again?";


        int choice = JOptionPane.showConfirmDialog(frame, message, "Victory!",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            frame.getContentPane().removeAll();
            GamePanel newGamePanel = new GamePanel();
            frame.add(newGamePanel);
            frame.revalidate();
            frame.repaint();
            newGamePanel.requestFocusInWindow();
        } else {
            System.exit(0);
        }

        showHighScores();

    }

    private static void setupAssetsPaths() {
        // Get the current working directory (your project's root folder)
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentDir);

        // Define the assets path relative to the project folder
        String assetsPath = currentDir + File.separator + "assets" + File.separator;

        // Create the assets directory if it doesn't exist
        File assetsDir = new File(assetsPath);
        if (!assetsDir.exists()) {
            boolean created = assetsDir.mkdirs();
            // This line is slightly different from my last suggestion to be less confusing
            System.out.println("Assets directory exists: " + !created);
        }

        // Set the assets path for tiles and characters - use forward slashes for Java compatibility
        String javaCompatiblePath = assetsPath.replace('\\', '/');
        System.out.println("Setting assets path to: " + javaCompatiblePath);

        try {
            // Ensure the static fields exist in these classes
            Tile.ASSETS_PATH = javaCompatiblePath;
            model.character.Character.IMAGE_BASE_PATH = javaCompatiblePath; // Use fully qualified name
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

    private static final String HIGH_SCORE_FILE = "highscores.dat";

    public static List<HighScoreEntry> loadHighScores() {
        List<HighScoreEntry> scores = new ArrayList<>();
        File file = new File(HIGH_SCORE_FILE);

        if (!file.exists()) return scores;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String name = parts[0];
                    int level = Integer.parseInt(parts[1]);
                    int kills = Integer.parseInt(parts[2]);
                    int time = Integer.parseInt(parts[3]);
                    boolean won = parts.length > 4 && parts[4].equals("1");
                    scores.add(new HighScoreEntry(name, level, kills, time, won));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading high scores: " + e.getMessage());
        }

        scores.sort(Collections.reverseOrder()); // Sort by score descending
        return scores;
    }

    public static void saveHighScore(String name, int level, int kills, int time, boolean won) {
        List<HighScoreEntry> scores = loadHighScores();
        scores.add(new HighScoreEntry(name, level, kills, time, won));
        scores.sort(Collections.reverseOrder());
        if (scores.size() > 10) {
            scores = scores.subList(0, 10); // Keep top 10
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(HIGH_SCORE_FILE))) {
            for (HighScoreEntry entry : scores) {
                writer.println(entry.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }

    private static String getPlayerNameInput() {
        String input = JOptionPane.showInputDialog(
                frame,
                "Enter your 3-character code (e.g., JAG):",
                "Leaderboard Entry",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        input = input.trim().toUpperCase();

        if (input.matches("[A-Z0-9]{1,3}")) {
            return input;
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Only letters and numbers allowed.\nPlease enter up to 3 characters.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
            return getPlayerNameInput(); // Recursive call to retry
        }
    }

    private static void showHighScores() {
        List<HighScoreEntry> scores = loadHighScores();

        StringBuilder sb = new StringBuilder("TOP SCORES:\n");
        for (int i = 0; i < Math.min(scores.size(), 10); i++) {
            sb.append(i + 1).append(". ").append(scores.get(i).getDisplayString()).append("\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);
        textArea.setFont(new Font("monospaced", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        JOptionPane.showMessageDialog(
                frame,
                scrollPane,
                "Leaderboard",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}