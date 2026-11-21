package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

public final class HauntedWinScreen extends ScreenAdapter {

    private static final float DURATION = 18f;

    private final DogerDager game;
    private final Texture background = new Texture(Gdx.files.internal("glitch-background.jpg"));
    private final Texture brokenOverlay = new Texture(Gdx.files.internal("broken.png"));
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private float elapsed;

    public HauntedWinScreen(DogerDager game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        elapsed += Math.min(delta, 0.1f);
        if (elapsed >= DURATION) {
            Gdx.graphics.setWindowedMode(960, 540);
            game.setScreen(new MenuScreen(game, game.post));
            dispose();
            return;
        }

        float width = Gdx.graphics.getBackBufferWidth();
        float height = Gdx.graphics.getBackBufferHeight();
        camera.setToOrtho(false, width, height);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        ScreenUtils.clear(Color.BLACK);

        float zoom = 1.04f + 0.08f * MathUtils.clamp(elapsed / DURATION, 0f, 1f);
        float drawWidth = width * zoom;
        float drawHeight = height * zoom;
        float shake = elapsed > 10f ? 3f : 0.6f;
        float x = (width - drawWidth) / 2f + MathUtils.random(-shake, shake);
        float y = (height - drawHeight) / 2f + MathUtils.random(-shake, shake);
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        batch.begin();
        batch.setBlendFunction(
            com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
            com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA
        );
        batch.setColor(Color.WHITE);
        batch.draw(background, x, y, drawWidth, drawHeight);
        float overlayAlpha = 0.82f + 0.18f * MathUtils.clamp(elapsed / DURATION, 0f, 1f);
        batch.setColor(1f, 1f, 1f, overlayAlpha);
        batch.draw(brokenOverlay, x, y, drawWidth, drawHeight);
        batch.setColor(Color.WHITE);
        batch.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.5f);
        shapes.rect(0f, 0f, width, height);
        shapes.setColor(0.35f, 0f, 0f, elapsed > 10f ? 0.13f : 0.04f);
        shapes.rect(0f, 0f, width, height);
        shapes.setColor(0.02f, 0.03f, 0.02f, 0.32f);
        for (float scanY = 0; scanY < height; scanY += 5f) {
            shapes.rect(0f, scanY, width, 1f);
        }
        shapes.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        String message = messageAt(elapsed);
        if (!message.isEmpty()) {
            batch.begin();
            font.getData().setScale(Math.max(1f, height / 420f));
            font.setColor(elapsed > 10f && ((int) (elapsed * 5f) % 2 == 0)
                ? Color.SCARLET : Color.WHITE);
            font.draw(batch, message, 48f, height * 0.52f);
            font.getData().setScale(1f);
            batch.end();
        }
    }

    private String messageAt(float time) {
        if (time < 2f) return "";
        if (time < 6f) return "FLOOR 5 COMPLETE";
        if (time < 10f) return "YOU WON. IT WAS WATCHING.";
        if (time < 14f) return "THANK YOU FOR PLAYING";
        return "GOODBYE";
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
    }

    @Override
    public void dispose() {
        background.dispose();
        brokenOverlay.dispose();
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }
}
