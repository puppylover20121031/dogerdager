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

public final class CreditsScreen extends ScreenAdapter {

    private final DogerDager game;
    private final PostProcessor post;
    private final Viewport viewport = new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final String[] creditsLines = {
            "DOGER DAGER",
            "",
            "Made with LibGDX",
            "",
            "Art, audio, and gameplay by the team",
            "",
            "RIP Honey Bun",
            "In loving memory",
            "",
            "Unpuppyable",
            "Owner / Developer",
            "",
            "The Doger Dager team",
            "",
            "Esc  back"
    };
    private float scrollY;
    private boolean switching;

    public CreditsScreen(DogerDager game, PostProcessor post) {
        this.game = game;
        this.post = post;
    }

    @Override
    public void render(float delta) {
        render(delta, post);
    }

    public void render(float delta, PostProcessor post) {
        if (switching) return;
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            switching = true;
            game.setScreen(new MenuScreen(game, post));
            dispose();
            return;
        }

        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        scrollY += delta * 80f;
        float left = 40f;
        float right = PlayScreen.WORLD_W - 40f;
        float top = PlayScreen.WORLD_H - 40f;
        float lineGap = 44f;

        font.getData().setScale(2.0f);
        font.setColor(Color.WHITE);
        layout.setText(font, "CREDITS");
        font.draw(batch, "CREDITS", left, top - scrollY);

        font.getData().setScale(1.35f);
        font.setColor(Color.LIGHT_GRAY);
        float y = top - 90f - scrollY;
        for (String line : creditsLines) {
            if (line.isEmpty()) {
                y -= lineGap + 8f;
                continue;
            }
            layout.setText(font, line);
            float x = left;
            if (line.equals("DOGER DAGER") || line.equals("CREDITS")) {
                x = left;
            }
            font.draw(batch, line, x, y);
            y -= lineGap;
        }
        batch.end();
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
