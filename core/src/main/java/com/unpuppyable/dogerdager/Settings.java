package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Persisted video settings, applied at launch and when changed. */
public final class Settings {

    public final static Preferences prefs = Gdx.app.getPreferences("doger-dager");

    public boolean vsync() {
        return prefs.getBoolean("set.vsync", true);
    }

    public boolean fullscreen() {
        return prefs.getBoolean("set.fullscreen", true);
    }

    public int fps() {
        return prefs.getInteger("set.fps", 60);
    }

    public boolean glitch() {
        return prefs.getBoolean("set.glitch", false);
    }

    public void setVsync(boolean value) {
        prefs.putBoolean("set.vsync", value);
        prefs.flush();
    }

    public void setFullscreen(boolean value) {
        prefs.putBoolean("set.fullscreen", value);
        prefs.flush();
    }

    public void setFps(int value) {
        prefs.putInteger("set.fps", value);
        prefs.flush();
    }

    public void setGlitch(boolean value) {
        prefs.putBoolean("set.glitch", value);
        prefs.flush();
    }

    public void apply(DogerDager game) {
        Gdx.graphics.setVSync(vsync());
        Gdx.graphics.setForegroundFPS(fps());
        if (fullscreen() && !Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else if (!fullscreen() && Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(1280, 720);
        }
        game.setGlitch(glitch());
        game.setBingo(bingo());
    }
    
    public void setBingo(boolean value) {
        prefs.putBoolean("set.bingo", value);
        prefs.flush();
    }
    public static boolean bingo() {
        return prefs.getBoolean("set.bingo", false);
    }
}
