package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.kotcrab.vis.ui.VisUI;
import com.unpuppyable.dogerdager.multiplayer.client.WebsocketClient;
import com.unpuppyable.dogerdager.multiplayer.host.Websocket;

public class DogerDager extends Game {
    private static boolean multiplayer = false;
    public static boolean multiplayerGameStarted = false;
    public static DogerDager instance;
    private Icons icons;
    public PostProcessor post;
    private Sound menuMove;
    private Sound menuConfirm;

    @Override
    public void create() {
        instance = this;
        VisUI.load();
        icons = new Icons(26);
        post = new PostProcessor();
        menuMove = Gdx.audio.newSound(Gdx.files.internal("menu-move.mp3"));
        menuConfirm = Gdx.audio.newSound(Gdx.files.internal("menu-confirm.mp3"));
        new Settings().apply(this);
        setScreen(new MenuScreen(this, post));
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
        Pad.poll();
        if (Gdx.input.isKeyJustPressed(Keys.F1)) post.toggleGlitch();
        if (Gdx.input.isKeyJustPressed(Keys.F11)) toggleFullscreen();
        post.capture();
        super.render();
        post.render(Gdx.graphics.getDeltaTime());
        ErrorNotifier.render(Gdx.graphics.getDeltaTime());
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
        Websocket.dispose();
        WebsocketClient.dispose();
        if (getScreen() != null) getScreen().dispose();
        post.dispose();
        ErrorNotifier.dispose();
        icons.dispose();
        menuMove.dispose();
        menuConfirm.dispose();
        VisUI.dispose();
        System.exit(0);
    }

    public void setBingo(boolean bingo) {
        
    }

    public static PostProcessor getPost() {
        return instance.post;
    }

    public static boolean getMultiplayer() {
        return multiplayer;
    }

    public static void setMultiplayer(boolean Bool) {
        multiplayer = Bool;
    }

    public static DogerDager getGameInstance() { return instance; }
}
