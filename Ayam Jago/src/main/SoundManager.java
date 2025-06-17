package main;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {

    // --- Singleton Pattern: Ensures there is only one SoundManager for the whole game ---
    private static SoundManager instance;

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    // -----------------------------------------------------------------------------------

    private Clip musicClip; // We store the music clip to control its volume later

    private SoundManager() {
        // Private constructor for the Singleton pattern
    }

    /**
     * Plays and continuously loops the background music.
     * @param musicFilePath Path to the music file.
     */
    public void playMusic(String musicFilePath) {
        try {
            // Stop any previously playing music
            if (musicClip != null && musicClip.isOpen()) {
                musicClip.stop();
                musicClip.close();
            }

            File musicFile = new File(musicFilePath);
            if (musicFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
                musicClip = AudioSystem.getClip();
                musicClip.open(audioStream);
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                musicClip.start();
                System.out.println("Successfully started background music.");
            } else {
                System.out.println("Could not find music file: " + musicFilePath);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Error playing music: " + e.getMessage());
        }
    }

    /**
     * Plays a short sound effect once.
     * @param soundFilePath Path to the sound effect file.
     */
    public static void playSound(String soundFilePath) {
        try {
            File soundFile = new File(soundFilePath);
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start(); // Play once, does not loop
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Error playing sound effect: " + e.getMessage());
        }
    }

    /**
     * Sets the volume of the background music.
     * @param volume A value from 0.0 (silent) to 1.0 (full volume).
     */
    public void setMusicVolume(float volume) {
        if (musicClip == null) return;

        // Clamp the volume value between 0.0 and 1.0
        volume = Math.max(0.0f, Math.min(1.0f, volume));

        try {
            FloatControl gainControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
            // Java's volume control is logarithmic (in decibels), not linear.
            // This formula converts a linear 0.0-1.0 scale to the decibel scale.
            float dB = (float) (Math.log10(volume) * 20.0);
            if (volume == 0.0) {
                dB = -80.0f; // A special value representing mute
            }
            gainControl.setValue(dB);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not set volume: " + e.getMessage());
        }
    }
}