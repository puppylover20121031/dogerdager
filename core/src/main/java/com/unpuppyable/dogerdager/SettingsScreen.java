package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class SettingsScreen extends ScreenAdapter {

    private static final int[] FPS = {60, 120, 144, 0};

    private final DogerDager game;
    private final PostProcessor post;
    private final Settings settings = new Settings();
    private final Viewport viewport = new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private int index;
    private boolean switching;

    public SettingsScreen(DogerDager game, PostProcessor post) {
        this.game = game;
        this.post = post;
    }

    @Override
    public void render(float delta) {
        render(delta, post);
    }

    public void render(float delta, PostProcessor post) {
        if (switching) return;
        handleKeys(post);
        if (switching) return;
        draw();
    }

    private void handleKeys(PostProcessor post) {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            switching = true;
            game.setScreen(new MenuScreen(game, post));
            dispose();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.W) || Gdx.input.isKeyJustPressed(Keys.UP) || Pad.justUp()) index = (index + 6) % 7;
        if (Gdx.input.isKeyJustPressed(Keys.S) || Gdx.input.isKeyJustPressed(Keys.DOWN) || Pad.justDown()) index = (index + 1) % 7;

        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Keys.LEFT) || Gdx.input.isKeyJustPressed(Keys.RIGHT)
                || Pad.justA()) {
            switch (index) {
                case 0 -> settings.setVsync(!settings.vsync());
                case 1 -> settings.setFullscreen(!settings.fullscreen());
                case 2 -> settings.setMusic(!settings.music());
                case 3 -> settings.setFps(nextFps(settings.fps()));
                case 4 -> settings.setGlitch(!settings.glitch());
                case 5 -> settings.setBingo(!Settings.bingo());
                case 6 -> {
                    switching = true;
                    game.setScreen(new KeyBindScreen(game, post));
                    dispose();
                    return;
                }
            }
            settings.apply(game);
        }
    }

    private int nextFps(int current) {
        for (int i = 0; i < FPS.length; i++) {
            if (FPS[i] == current) return FPS[(i + 1) % FPS.length];
        }
        return 60;
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        title("SETTINGS", 320);

        line(0, "VSync", settings.vsync() ? "ON" : "OFF", 278);
        line(1, "Fullscreen", settings.fullscreen() ? "ON" : "OFF", 240);
        line(2, "In Game Music", settings.music() ? "ON" : "OFF", 212);
        line(3, "FPS", settings.fps() == 0 ? "uncapped" : String.valueOf(settings.fps()), 184);
        line(4, "Glitch", settings.glitch() ? "ON" : "OFF", 156);
        line(5, "bingo heeler mode\n(needs restart)", Settings.bingo() ? "ON" : "OFF", 124);
        line(6, "Keybinds", "change", 92);

        font.setColor(Color.GRAY);
        centered("up/down select    enter/open    Esc back", 50);
        batch.end();
    }

    private void line(int i, String name, String value, float y) {
        font.setColor(i == index ? Color.YELLOW : Color.WHITE);
        font.draw(batch, (i == index ? "> " : "  ") + name, 210, y);
        font.draw(batch, value, 390, y);
    }

    private void title(String text, float y) {
        font.getData().setScale(1.8f);
        font.setColor(Color.WHITE);
        centered(text, y);
        font.getData().setScale(1f);
    }

    private void centered(String text, float y) {
        layout.setText(font, text);
        font.draw(batch, text, (PlayScreen.WORLD_W - layout.width) / 2f, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
