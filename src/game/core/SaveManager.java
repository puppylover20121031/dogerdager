package game.core;

import java.io.*;
import java.nio.file.*;

public class SaveManager {

    private static final String SAVE_FILE_PATH;
    private int highScore;

    static {
        // Save in user’s home directory for cross-platform safety
        String home = System.getProperty("user.home");
        SAVE_FILE_PATH = Paths.get(home, "game_save.txt").toString();
    }

    public SaveManager() {
        load();
    }

    /** Loads save file (creates one if missing). */
    public void load() {
        Path path = Paths.get(SAVE_FILE_PATH);

        if (!Files.exists(path)) {
            highScore = 0;
            save();
            return;
        }

        try {
            String line = Files.readString(path).trim();
            highScore = Integer.parseInt(line);
        } catch (Exception e) {
            // If corrupted, reset
            highScore = 0;
            save();
        }
    }

    /** Saves current data to disk. */
    public synchronized void save() {
        Path path = Paths.get(SAVE_FILE_PATH);

        try {
            Files.writeString(path, Integer.toString(highScore));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    // ------------------------------
    // Public API
    // ------------------------------

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int newScore) {
        if (newScore > highScore) {
            highScore = newScore;
            save();
        }
    }
}
