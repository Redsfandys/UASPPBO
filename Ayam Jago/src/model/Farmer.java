package model;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Farmer extends GameObject {

    // The states are now more accurate: Sleeping, Awake (for a moment), and Gone.
    private enum State { SLEEPING, AWAKE, GONE }
    private State currentState;

    // We now have 5 sleeping frames and 1 awake frame.
    private static BufferedImage[] sleepingFrames = new BufferedImage[5];
    private static BufferedImage awakeFrame;
    private static boolean imagesLoaded = false;

    private int animationCounter = 0;
    private int currentFrameIndex = 0;

    public Farmer(int row, int col, int size) {
        super(row, col, size);
        this.currentState = State.SLEEPING;
        if (!imagesLoaded) {
            loadImages();
        }
    }

    private static void loadImages() {
        try {
            String basePath = model.character.Character.IMAGE_BASE_PATH;
            // Load the 5 sleeping frames
            for (int i = 0; i < 5; i++) {
                sleepingFrames[i] = ImageIO.read(new File(basePath + "farmer_sleep_" + (i+1) + ".png"));
            }
            // Load the single awake frame
            awakeFrame = ImageIO.read(new File(basePath + "farmer_wake_1.png"));
            imagesLoaded = true;
            System.out.println("Successfully loaded farmer sprites (5 sleep, 1 awake).");
        } catch (IOException e) {
            System.out.println("Error loading farmer sprites: " + e.getMessage());
        }
    }

    public boolean isSleeping() {
        return currentState == State.SLEEPING;
    }

    public boolean isGone() {
        return currentState == State.GONE;
    }

    public void wakeUp() {
        if (currentState == State.SLEEPING) {
            currentState = State.AWAKE;
            animationCounter = 0; // Reset counter to use as a timer for the AWAKE state
        }
    }

    @Override
    public void update() {
        animationCounter++;

        if (currentState == State.SLEEPING) {
            // Cycle through sleeping animation
            if (animationCounter % 30 == 0) { // Every half-second or so
                currentFrameIndex = (currentFrameIndex + 1) % sleepingFrames.length;
            }
        } else if (currentState == State.AWAKE) {
            // In the AWAKE state, we just wait for a short duration before disappearing.
            // Let's wait for 60 frames (about 1 second).
            if (animationCounter > 60) {
                currentState = State.GONE;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!imagesLoaded || currentState == State.GONE) return;

        BufferedImage currentFrame = null;
        if (currentState == State.SLEEPING) {
            currentFrame = sleepingFrames[currentFrameIndex];
        } else if (currentState == State.AWAKE) {
            // When awake, always show the single awakeFrame
            currentFrame = awakeFrame;
        }

        if (currentFrame != null) {
            g.drawImage(currentFrame, col * size, row * size, size*2, size*2, null);
        }
    }
}