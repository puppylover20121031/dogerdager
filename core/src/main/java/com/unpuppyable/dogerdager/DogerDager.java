package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.kotcrab.vis.ui.VisUI;

public class DogerDager extends Game {

    private Icons icons;
    private PostProcessor post;
    private Sound menuMove;
    private Sound menuConfirm;

    @Override
    public void create() {
        VisUI.load();
        icons = new Icons(26);
        post = new PostProcessor();
        menuMove = Gdx.audio.newSound(Gdx.files.internal("menu-move.mp3"));
        menuConfirm = Gdx.audio.newSound(Gdx.files.internal("menu-confirm.mp3"));
        new Settings().apply(this);
        setScreen(new MenuScreen(this));
    }

    public void setGlitch(boolean on) {
        post.setGlitch(on);
    }

    public void menuMove() {
        menuMove.play();
    }

    public void menuConfirm() {
        menuConfirm.play();
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Keys.F1)) post.toggleGlitch();
        if (Gdx.input.isKeyJustPressed(Keys.F11)) toggleFullscreen();
        post.capture();
        super.render();
        post.render(Gdx.graphics.getDeltaTime());
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(1280, 720);
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
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
        menuMove.dispose();
        menuConfirm.dispose();
        VisUI.dispose();
    }
}
