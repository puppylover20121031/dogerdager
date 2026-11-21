package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public final class SpookyTerminalScreen extends ScreenAdapter {

    private static final float AUTO_CLOSE_DELAY = 7f;

    private static final String[] QUESTIONS = {
        "Did you hear the menu music after the game closed?",
        "If the lights go out, will you stay here?",
        "Do you think the figure in the background is moving?",
        "Would you let it play one round for you?",
        "If it knows your name, will you answer?"
    };

    private final Preferences preferences;
    private final FitViewport viewport = new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final SpriteBatch textBatch = new SpriteBatch();
    private final boolean[] answers = new boolean[QUESTIONS.length];
    private int questionIndex;
    private boolean selectedYes = true;
    private boolean finished;
    private float elapsed;
    private float closeTimer;
    private float glitchFlashTimer;
    private float nextGlitchFlash = 1.5f;

    public SpookyTerminalScreen(Preferences preferences) {
        this.preferences = preferences;
        preferences.putBoolean("hauntedWinSeen", true);
        preferences.flush();
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        if (finished) {
            closeTimer += delta;
            if (closeTimer >= AUTO_CLOSE_DELAY) {
                Gdx.app.exit();
                return;
            }
        } else if (elapsed >= nextGlitchFlash) {
            glitchFlashTimer = 0.08f;
            nextGlitchFlash = elapsed + MathUtils.random(1.2f, 4f);
        }
        glitchFlashTimer = Math.max(0, glitchFlashTimer - delta);
        handleInput();
        float flicker = glitchFlashTimer > 0 ? 0.025f : 0f;
        ScreenUtils.clear(0.005f + flicker, 0.015f, 0.008f, 1f);
        viewport.apply();
        float shakeX = glitchFlashTimer > 0 ? MathUtils.random(-7f, 7f) : MathUtils.random(-1f, 1f);
        float shakeY = glitchFlashTimer > 0 ? MathUtils.random(-5f, 5f) : 0f;
        viewport.getCamera().position.set(
            PlayScreen.WORLD_W / 2f + shakeX,
            PlayScreen.WORLD_H / 2f + shakeY,
            0f
        );
        viewport.getCamera().update();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.015f + flicker, 0.035f, 0.02f, 1f);
        shapes.rect(90, 90, PlayScreen.WORLD_W - 180, PlayScreen.WORLD_H - 180);
        shapes.setColor(0.08f, 0.015f, 0.015f, 1f);
        shapes.rect(90, PlayScreen.WORLD_H - 220, PlayScreen.WORLD_W - 180, 3f);
        shapes.setColor(0.025f, 0.055f, 0.03f, 1f);
        for (int y = 110; y < PlayScreen.WORLD_H - 100; y += 12) {
            shapes.rect(95, y, PlayScreen.WORLD_W - 190, 1f);
        }
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(finished && ((int) (elapsed * 5) % 2 == 0)
            ? Color.SCARLET : new Color(0.25f, 0.85f, 0.38f, 1f));
        shapes.rect(90, 90, PlayScreen.WORLD_W - 180, PlayScreen.WORLD_H - 180);
        shapes.end();

        textBatch.setProjectionMatrix(viewport.getCamera().combined);
        textBatch.begin();
        font.setColor(finished && ((int) (elapsed * 5) % 2 == 0)
            ? Color.SCARLET : new Color(0.35f, 1f, 0.48f, 1f));
        font.getData().setScale(1.25f);
        font.draw(textBatch, "DOGER DAGER // RECOVERY CONSOLE", 140, PlayScreen.WORLD_H - 145);
        font.getData().setScale(1f);
        if (!finished) {
            font.draw(textBatch, "CONNECTION RESTORED. IT HAS BEEN WAITING.", 140, PlayScreen.WORLD_H - 230);
            font.draw(textBatch, "QUESTION " + (questionIndex + 1) + " / " + QUESTIONS.length,
                140, PlayScreen.WORLD_H - 330);
            font.draw(textBatch, "> " + QUESTIONS[questionIndex], 140, PlayScreen.WORLD_H - 405);
            drawAnswer("YES", selectedYes, 140, PlayScreen.WORLD_H - 520);
            drawAnswer("NO", !selectedYes, 285, PlayScreen.WORLD_H - 520);
            font.draw(textBatch, "Y / N to answer    LEFT / RIGHT to choose    ENTER to confirm",
                140, 180);
        } else {
            int yesCount = 0;
            for (boolean answer : answers) {
                if (answer) yesCount++;
            }
            font.draw(textBatch, "ALL ANSWERS RECORDED. YES: " + yesCount
                + " / NO: " + (answers.length - yesCount), 140, PlayScreen.WORLD_H - 280);
            font.setColor(0.8f, 0.22f, 0.22f, 1f);
            font.getData().setScale(1.6f);
            font.draw(textBatch, "REOPEN THE GAME", 140, PlayScreen.WORLD_H - 410);
            font.getData().setScale(1f);
            font.setColor(Color.SCARLET);
            font.draw(textBatch, "SESSION TERMINATING IN " +
                Math.max(0, (int) Math.ceil(AUTO_CLOSE_DELAY - closeTimer)), 140, 180);
            font.setColor(new Color(0.35f, 1f, 0.48f, 1f));
            if (((int) (elapsed * 2) % 2) == 0) {
                font.draw(textBatch, "DON'T LOOK BEHIND YOU.", 140, 245);
            }
        }
        textBatch.end();
        if (glitchFlashTimer > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0.6f, 0.01f, 0.01f, 0.08f);
            shapes.rect(0, MathUtils.random(0, (int) PlayScreen.WORLD_H), PlayScreen.WORLD_W, 28);
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    private void drawAnswer(String text, boolean selected, float x, float y) {
        font.setColor(selected ? Color.WHITE : new Color(0.2f, 0.55f, 0.28f, 1f));
        font.draw(textBatch, (selected ? "> " : "  ") + text, x, y);
    }

    private void handleInput() {
        if (!finished) {
            if (Gdx.input.isKeyJustPressed(Keys.LEFT) || Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
                selectedYes = !selectedYes;
            }
            if (Gdx.input.isKeyJustPressed(Keys.Y)) {
                answer(true);
            } else if (Gdx.input.isKeyJustPressed(Keys.N)) {
                answer(false);
            } else if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                answer(selectedYes);
            }
            return;
        }

    }

    private void answer(boolean yes) {
        answers[questionIndex] = yes;
        questionIndex++;
        if (questionIndex >= QUESTIONS.length) {
            finished = true;
            closeTimer = 0;
            preferences.putBoolean("hauntedDemoPending", true);
            preferences.flush();
        } else {
            selectedYes = true;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        textBatch.dispose();
        font.dispose();
        shapes.dispose();
    }
}
