package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
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
    private int windowEffectPhase;
    private float windowEffectTimer;
    private float windowEffectDuration;
    private int windowEffectStartWidth;
    private int windowEffectStartHeight;
    private int windowEffectTargetWidth;
    private int windowEffectTargetHeight;
    private int windowEffectLastWidth;
    private int windowEffectLastHeight;
    private int windowEffectRestoreWidth;
    private int windowEffectRestoreHeight;
    private boolean windowEffectRestoreFullscreen;

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

    public void unfullscreenWithShrink() {
        if (windowEffectPhase != 0) return;

        windowEffectRestoreFullscreen = Gdx.graphics.isFullscreen();
        windowEffectRestoreWidth = Gdx.graphics.getWidth();
        windowEffectRestoreHeight = Gdx.graphics.getHeight();

        var display = Gdx.graphics.getDisplayMode();
        windowEffectStartWidth = windowEffectRestoreFullscreen ? display.width : windowEffectRestoreWidth;
        windowEffectStartHeight = windowEffectRestoreFullscreen ? display.height : windowEffectRestoreHeight;
        windowEffectTargetWidth = Math.max(640, Math.round(windowEffectStartWidth * 0.75f));
        windowEffectTargetHeight = Math.max(360, Math.round(windowEffectStartHeight * 0.75f));

        if (windowEffectRestoreFullscreen) {
            Gdx.graphics.setWindowedMode(windowEffectStartWidth, windowEffectStartHeight);
            windowEffectRestoreWidth = display.width;
            windowEffectRestoreHeight = display.height;
        }

        windowEffectLastWidth = windowEffectStartWidth;
        windowEffectLastHeight = windowEffectStartHeight;
        windowEffectDuration = 4f;
        windowEffectTimer = 0;
        windowEffectPhase = 1;
    }

    private void updateWindowShrink(float delta) {
        if (windowEffectPhase == 0) return;

        windowEffectTimer += delta;

        if (windowEffectPhase == 1) {
            float progress = MathUtils.clamp(windowEffectTimer / windowEffectDuration, 0f, 1f);
            progress *= progress;
            resizeWindowSmoothly(windowEffectStartWidth, windowEffectStartHeight,
                windowEffectTargetWidth, windowEffectTargetHeight, progress);

            if (windowEffectTimer >= windowEffectDuration) {
                windowEffectTimer = 0;
                windowEffectDuration = 1.5f;
                windowEffectPhase = 2;
            }
        } else if (windowEffectPhase == 2) {
            if (windowEffectTimer >= windowEffectDuration) {
                windowEffectTimer = 0;
                windowEffectDuration = 3.5f;
                windowEffectPhase = 3;
            }
        } else if (windowEffectPhase == 3) {
            float progress = MathUtils.clamp(windowEffectTimer / windowEffectDuration, 0f, 1f);
            progress *= progress * progress;
            resizeWindowSmoothly(windowEffectTargetWidth, windowEffectTargetHeight,
                windowEffectRestoreWidth, windowEffectRestoreHeight, progress);

            if (windowEffectTimer >= windowEffectDuration) {
                if (windowEffectRestoreFullscreen) {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                } else {
                    Gdx.graphics.setWindowedMode(windowEffectRestoreWidth, windowEffectRestoreHeight);
                }
                windowEffectPhase = 0;
                windowEffectTimer = 0;
            }
        }
    }

    private void resizeWindowSmoothly(int startWidth, int startHeight, int targetWidth,
                                      int targetHeight, float progress) {
        int width = Math.round(MathUtils.lerp(startWidth, targetWidth, progress));
        int height = Math.round(MathUtils.lerp(startHeight, targetHeight, progress));
        if (width != windowEffectLastWidth || height != windowEffectLastHeight) {
            Gdx.graphics.setWindowedMode(width, height);
            windowEffectLastWidth = width;
            windowEffectLastHeight = height;
        }
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
        updateWindowShrink(Gdx.graphics.getDeltaTime());
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
