package com.unpuppyable.dogerdager.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.unpuppyable.dogerdager.DogerDager;

/** Desktop (LWJGL 3) launcher; spins up the libGDX application window. */
public final class Lwjgl3Launcher {

    private Lwjgl3Launcher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Doger Dager");
        // Match the legacy AWT window dimensions for now.
        config.setWindowedMode(640, 477);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new DogerDager(), config);
    }
}
