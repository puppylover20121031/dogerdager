package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class KeyBindScreen extends ScreenAdapter {

    private final DogerDager game;
    private final PostProcessor post;
    private final KeyBind keyBind = new KeyBind();
    private final Viewport viewport = new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final KeyBind.Action[] actions = {
            KeyBind.Action.MOVE_UP,
            KeyBind.Action.MOVE_DOWN,
            KeyBind.Action.MOVE_LEFT,
            KeyBind.Action.MOVE_RIGHT,
            KeyBind.Action.STRAFE,
            KeyBind.Action.PAUSE
    };
    private int index;
    private boolean switching;
    private boolean waitingForInput;
    private KeyBind.Action pendingAction;
    private final InputAdapter inputAdapter = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            if (!waitingForInput || pendingAction == null) {
                return false;
            }
            keyBind.set(pendingAction, keycode);
            waitingForInput = false;
            pendingAction = null;
            return true;
        }
    };

    public KeyBindScreen(DogerDager game, PostProcessor post) {
        this.game = game;
        this.post = post;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputAdapter);
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
        if (waitingForInput) {
            if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
                waitingForInput = false;
                pendingAction = null;
            }
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            switching = true;
            game.setScreen(new SettingsScreen(game, post));
            dispose();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.W) || Gdx.input.isKeyJustPressed(Keys.UP)) index = (index + actions.length - 1) % actions.length;
        if (Gdx.input.isKeyJustPressed(Keys.S) || Gdx.input.isKeyJustPressed(Keys.DOWN)) index = (index + 1) % actions.length;

        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Keys.LEFT) || Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            pendingAction = actions[index];
            waitingForInput = true;
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        title("KEYBINDS", 320);

        for (int i = 0; i < actions.length; i++) {
            line(i, actionName(actions[i]), keyBind.name(actions[i]), 250 - i * 28f);
        }

        font.setColor(Color.GRAY);
        centered(waitingForInput ? "press any key to bind    Esc cancel" : "up/down select    enter bind    Esc back", 50);
        batch.end();
    }

    private String actionName(KeyBind.Action action) {
        return switch (action) {
            case MOVE_UP -> "Move Up";
            case MOVE_DOWN -> "Move Down";
            case MOVE_LEFT -> "Move Left";
            case MOVE_RIGHT -> "Move Right";
            case STRAFE -> "Strafe";
            case PAUSE -> "Pause";
        };
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
        Gdx.input.setInputProcessor(null);
    }
}
