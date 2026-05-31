package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Game;

public class DogerDager extends Game {

    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
    }
}
