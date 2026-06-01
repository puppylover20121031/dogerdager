package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Persisted video settings, applied at launch and when changed. */
public final class Custom {

    public final static Preferences prefs = Gdx.app.getPreferences("doger-dager");

    public int customHealth() {
        return prefs.getInteger("set.customHealth", 6);
    }

    public float customEnemySpeed() {
        return prefs.getFloat("set.customEnemySpeed", 220f);
    }

    public int customWinFloor() {
        return prefs.getInteger("set.customWinFloor", 12);
    }

    public void setHealth(int value) {
        prefs.putInteger("set.customHealth", value);
        prefs.flush();
    }

    public void setEnemySpeed(float value) {
        prefs.putFloat("set.customEnemySpeed", value);
        prefs.flush();
    }

    public void setWinFloor(int value) {
        prefs.putInteger("set.customWinFloor", value);
        prefs.flush();
    }

    public void apply(DogerDager game) {
        Difficulty.CUSTOM.setMaxHealth(customHealth());
        Difficulty.CUSTOM.setEnemySpeed(customEnemySpeed());
        Difficulty.CUSTOM.setWinFloor(customWinFloor());
    }
}
