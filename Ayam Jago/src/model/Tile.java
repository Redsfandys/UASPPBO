package model;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class Tile {
    private boolean isWall;
    private static BufferedImage wallImage;
    private static BufferedImage floorImage;
    private static boolean imagesLoaded = false;
    private static boolean loadAttempted = false;
    
    // Define path to assets as a configurable property
    public static String ASSETS_PATH = "c:/Users/ASV/Downloads/Ayam Jago/assets/";
    
    public Tile(boolean isWall) {
        this.isWall = isWall;
        
        // Load images if not already loaded
        if (!imagesLoaded && !loadAttempted) {
            loadImages();
        }
    }
    
    private static void loadImages() {
        loadAttempted = true;
        System.out.println("Attempting to load tile images from: " + ASSETS_PATH);
        
        try {
            // Try loading wall image
            try {
                File wallFile = new File(ASSETS_PATH + "wall.png");
                if (wallFile.exists()) {
                    wallImage = ImageIO.read(wallFile);
                    System.out.println("Successfully loaded wall image");
                } else {
                    System.out.println("Wall image not found at: " + wallFile.getAbsolutePath());
                }
            } catch (IOException e) {
                System.out.println("Error loading wall image: " + e.getMessage());
            }

            // Try loading floor image with different extensions
            String[] floorExtensions = {"floor.jpeg", "floor.jpg", "floor.png"};
            for (String ext : floorExtensions) {
                try {
                    File floorFile = new File(ASSETS_PATH + ext);
                    if (floorFile.exists()) {
                        floorImage = ImageIO.read(floorFile);
                        System.out.println("Successfully loaded floor image: " + ext);
                        break;
                    }
                } catch (IOException e) {
                    // Just try the next extension
                }
            }
            
            if (floorImage == null) {
                System.out.println("Could not find floor image with any supported extension");
            }
            
            // Only set imagesLoaded if both images were loaded successfully
            imagesLoaded = (wallImage != null && floorImage != null);
            
        } catch (Exception e) {
            System.out.println("Unexpected error loading tile images: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set a custom assets path for tile images
     */
    public static void setAssetsPath(String path) {
        ASSETS_PATH = path;
        // Reset so we'll try to load images again
        imagesLoaded = false;
        loadAttempted = false;
    }
    
    public boolean isWall() {
        return isWall;
    }
    
    public void setWall(boolean wall) {
        isWall = wall;
    }
    
    // Method to draw the tile
    public void draw(Graphics g, int x, int y, int size) {
        if (imagesLoaded) {
            // Draw with images
            BufferedImage img = isWall ? wallImage : floorImage;
            g.drawImage(img, x, y, size, size, null);
        } else {
            // Fallback to simple colored squares if images aren't loaded
            g.setColor(isWall ? java.awt.Color.DARK_GRAY : java.awt.Color.WHITE);
            g.fillRect(x, y, size, size);
            g.setColor(java.awt.Color.BLACK);
            g.drawRect(x, y, size, size);
        }
    }
    
    @Override
    public String toString() {
        return "Tile[isWall=" + isWall + "]";
    }
}
