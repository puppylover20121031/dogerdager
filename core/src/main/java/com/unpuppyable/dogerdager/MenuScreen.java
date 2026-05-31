package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class MenuScreen extends ScreenAdapter {

    private static final float W = PlayScreen.WORLD_W;
    private static final float H = PlayScreen.WORLD_H;
    private static final Difficulty[] CHOICES = Difficulty.values();

    private final DogerDager game;
    private final Viewport viewport = new FitViewport(W, H);
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Preferences prefs = Gdx.app.getPreferences("doger-dager");

    private int index = Difficulty.NORMAL.ordinal();
    private boolean info;

    public MenuScreen(DogerDager game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        handleInput();
        draw();
    }

    private void handleInput() {
        if (info) {
            if (Gdx.input.isKeyJustPressed(Keys.A)
                    || Gdx.input.isKeyJustPressed(Keys.ESCAPE)
                    || Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                info = false;
            }
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.W) || Gdx.input.isKeyJustPressed(Keys.UP)) {
            index = (index - 1 + CHOICES.length) % CHOICES.length;
        }
        if (Gdx.input.isKeyJustPressed(Keys.S) || Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            index = (index + 1) % CHOICES.length;
        }
        if (Gdx.input.isKeyJustPressed(Keys.A)) {
            info = true;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            game.setScreen(new PlayScreen(game, CHOICES[index]));
            dispose();
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        if (info) drawInfo();
        else drawMenu();
        batch.end();
    }

    private void drawMenu() {
        title("DOGER DAGER", H - 70);
        for (int i = 0; i < CHOICES.length; i++) {
            boolean on = i == index;
            font.setColor(on ? Color.YELLOW : Color.GRAY);
            centered((on ? ">  " : "   ") + CHOICES[i].name(), H - 170 - i * 34);
        }
        font.setColor(Color.WHITE);
        centered("Best  " + prefs.getInteger("highScore", 0), 110);
        font.setColor(Color.GRAY);
        centered("Enter  play       A  info", 60);
    }

    private void drawInfo() {
        title("CONTROLS", H - 70);
        font.setColor(Color.WHITE);
        centered("WASD / Arrows   move", H - 170);
        centered("Space   shoot", H - 200);
        centered("Shift   shield", H - 230);
        centered("M   music        R   retry        Esc   menu", H - 260);
        font.setColor(Color.GRAY);
        centered("press A or Esc to go back", 60);
    }

    private void title(String text, float y) {
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
        centered(text, y);
        font.getData().setScale(1f);
    }

    private void centered(String text, float y) {
        layout.setText(font, text);
        font.draw(batch, text, (W - layout.width) / 2f, y);
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
