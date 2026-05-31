package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** All persisted player data: best scores, clears, unlocks, achievements. */
public final class Progress {

    private final Preferences prefs = Gdx.app.getPreferences("doger-dager");

    public int bestScore(Difficulty difficulty) {
        return prefs.getInteger("floor." + difficulty.name(), 0);
    }

    public int bestOverall() {
        int best = 0;
        for (Difficulty d : Difficulty.values()) best = Math.max(best, bestScore(d));
        return best;
    }

    public boolean cleared(Difficulty difficulty) {
        return prefs.getBoolean("cleared." + difficulty.name(), false);
    }

    public int runs() {
        return prefs.getInteger("runs", 0);
    }

    public int hardClears() {
        return prefs.getInteger("hardClears", 0);
    }

    public boolean hardcoreUnlocked() {
        return cleared(Difficulty.HARD);
    }

    public boolean achieved(String id) {
        return prefs.getBoolean("ach." + id, false);
    }

    public void unlock(String id) {
        prefs.putBoolean("ach." + id, true);
        prefs.flush();
    }

    public boolean flag(String id) {
        return prefs.getBoolean("flag." + id, false);
    }

    public void setFlag(String id) {
        prefs.putBoolean("flag." + id, true);
        prefs.flush();
    }

    public void recordRun(Difficulty difficulty, int score, boolean won) {
        prefs.putInteger("runs", runs() + 1);
        if (score > bestScore(difficulty)) {
            prefs.putInteger("floor." + difficulty.name(), score);
        }
        if (won) {
            prefs.putBoolean("cleared." + difficulty.name(), true);
            if (difficulty == Difficulty.HARD) {
                prefs.putInteger("hardClears", hardClears() + 1);
            }
        }
        prefs.flush();
    }
}
