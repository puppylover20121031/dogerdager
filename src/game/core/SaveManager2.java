package game.core;

import java.io.*;
import java.nio.file.*;

public class SaveManager2 {

    private static final String SAVE_FILE_PATH;
    private boolean curBool;

    static {
        // Save in user’s home directory for cross-platform safety
        String home = System.getProperty("user.home");
        SAVE_FILE_PATH = Paths.get(home, "unpuppyable_game_save2.sav").toString();
    }

    public SaveManager2() {
        load();
    }

    /** Loads save file (creates one if missing). */
    public void load() {
        Path path = Paths.get(SAVE_FILE_PATH);

        if (!Files.exists(path)) {
            curBool = Game.askYesNo("do you allow access to your desktop folder for creating/reading text files? this will only affect files created by the game. this WILL override any text files named \"its_time.txt\"", "its fine if you say no.");
            
            save();
            return;
        }

        try {
            String line = Files.readString(path).trim();
            curBool = Boolean.parseBoolean(line);
        } catch (Exception e) {
            // If corrupted, reset
            curBool = false;
            save();
        }
    }

    /** Saves current data to disk. */
    public synchronized void save() {
        Path path = Paths.get(SAVE_FILE_PATH);

        try {
            Files.writeString(path, Boolean.toString(curBool));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    // ------------------------------
    // Public API
    // ------------------------------

    public boolean getBool() {
        return curBool;
    }

    public void setBool(boolean newBool) {
        curBool = newBool;
        save();
    }
}
