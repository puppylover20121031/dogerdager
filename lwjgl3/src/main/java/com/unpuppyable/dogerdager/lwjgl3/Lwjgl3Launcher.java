package com.unpuppyable.dogerdager.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.unpuppyable.dogerdager.DogerDager;

public class Lwjgl3Launcher {

    void main() {
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Doger Dager");
        config.setWindowedMode(640, 477);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setIdleFPS(60);
        new Lwjgl3Application(new DogerDager(), config);
    }
}
