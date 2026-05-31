package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.VisUI;

public class DogerDager extends Game {

    private Icons icons;

    @Override
    public void create() {
        VisUI.load();
        icons = new Icons(26);
        setScreen(new MenuScreen(this));
    }

    public Icons icons() {
        return icons;
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        icons.dispose();
        VisUI.dispose();
    }
}
