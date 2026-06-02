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

    public int customHitBonus() {
        return prefs.getInteger("set.customHitBonus", -1);
    }
    public int customCentipedeFloor() {
        return prefs.getInteger("set.customCentipedeFloor", 5);
    }

    public int customSmartFloor() {
        return prefs.getInteger("set.customSmartFloor", 3);
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

    public void setCentipedeFloor(int value) {
        prefs.putInteger("set.customCentipedeFloor", value);
        prefs.flush();
    }

    public void setSmartFloor(int value) {
        prefs.putInteger("set.customSmartFloor", value);
        prefs.flush();
    }

    public void setHitBonus(int value) {
        prefs.putInteger("set.customHitBonus", value);
        prefs.flush();
    }

    public void apply(DogerDager game) {
        Difficulty.CUSTOM.setMaxHealth(customHealth());
        Difficulty.CUSTOM.setEnemySpeed(customEnemySpeed());
        Difficulty.CUSTOM.setWinFloor(customWinFloor());
        Difficulty.CUSTOM.setHitBonus(customHitBonus());
        Difficulty.CUSTOM.setCentipedeFloor(customCentipedeFloor());
        Difficulty.CUSTOM.setSmartFloor(customSmartFloor());
    }
}
