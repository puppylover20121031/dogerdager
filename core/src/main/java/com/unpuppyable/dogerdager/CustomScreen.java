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

public final class CustomScreen extends ScreenAdapter {

    private static final int MENU_ITEMS = 4;

    private final DogerDager game;
    private final Custom custom = new Custom();
    private final Viewport viewport = new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private int index;
    private boolean switching;

    private int setHealth;
    private int setWinFloor;
    private int setEnemySpeed;

    public CustomScreen(DogerDager game) {
        this.game = game;
        this.setHealth = custom.customHealth();
        this.setEnemySpeed = Math.round(custom.customEnemySpeed());
        this.setWinFloor = custom.customWinFloor();
        custom.apply(game);
    }

    @Override
    public void render(float delta) {
        if (switching)
            return;
        handleKeys();
        if (switching)
            return;
        draw();
    }

    private void handleKeys() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            switching = true;
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Keys.W) || Gdx.input.isKeyJustPressed(Keys.UP))
            index = (index + MENU_ITEMS - 1) % MENU_ITEMS;
        if (Gdx.input.isKeyJustPressed(Keys.S) || Gdx.input.isKeyJustPressed(Keys.DOWN))
            index = (index + 1) % MENU_ITEMS;

        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            if (index == 3) {
                custom.setHealth(setHealth);
                custom.setEnemySpeed(setEnemySpeed);
                custom.setWinFloor(setWinFloor);
                custom.apply(game);
                switching = true;
                game.setScreen(new PlayScreen(game, Difficulty.CUSTOM, 0f));
                dispose();
                return;
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            if (index == 0 && setHealth > 2) {
                setHealth -= 1;
                custom.setHealth(setHealth);
                custom.apply(game);
            } else if (index == 1 && setEnemySpeed > 100) {
                setEnemySpeed -= 10;
                custom.setEnemySpeed(setEnemySpeed);
                custom.apply(game);
            } else if (index == 2 && setWinFloor > 1) {
                setWinFloor -= 1;
                custom.setWinFloor(setWinFloor);
                custom.apply(game);
            }
        } else if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            if (index == 0 && setHealth < 24) {
                setHealth += 1;
                custom.setHealth(setHealth);
                custom.apply(game);
            } else if (index == 1 && setEnemySpeed < 600) {
                setEnemySpeed += 10;
                custom.setEnemySpeed(setEnemySpeed);
                custom.apply(game);
            } else if (index == 2 && setWinFloor < 20) {
                setWinFloor += 1;
                custom.setWinFloor(setWinFloor);
                custom.apply(game);
            }
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        title("custom", 300);
        line(0, "Amount of health", Integer.toString(setHealth), 260);
        line(1, "Enemy speed", Integer.toString(setEnemySpeed), 220);
        line(2, "Win floor", Integer.toString(setWinFloor), 180);
        line(3, "Start game", "", 140);
        font.setColor(Color.GRAY);
        centered("up/down select    left/right change    Enter start    Esc back", 60);
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
