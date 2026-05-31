package com.unpuppyable.dogerdager.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.unpuppyable.dogerdager.DogerDager;

public class Lwjgl3Launcher {

    void main() {
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Doger Dager");
        var mode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        config.setDecorated(false);
        config.setWindowedMode(mode.width, mode.height);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setIdleFPS(60);
        new Lwjgl3Application(new DogerDager(), config);
    }
}
