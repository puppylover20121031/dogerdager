package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.kotcrab.vis.ui.VisUI;

public class DogerDager extends Game {

    private Icons icons;
    private PostProcessor post;

    @Override
    public void create() {
        VisUI.load();
        icons = new Icons(26);
        post = new PostProcessor();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Keys.F1)) post.toggle();
        post.capture();
        super.render();
        post.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        post.resize(width, height);
        super.resize(width, height);
    }

    public Icons icons() {
        return icons;
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        post.dispose();
        icons.dispose();
        VisUI.dispose();
    }
}
