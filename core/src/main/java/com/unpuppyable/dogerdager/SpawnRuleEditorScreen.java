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

public class SpawnRuleEditorScreen extends ScreenAdapter {

    private final DogerDager game;
    private final SpawnSettings settings;
    private final SpawnSchedule schedule;
    private final SpawnRule rule;
    private final Viewport viewport = new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private int selectedIndex;

    public SpawnRuleEditorScreen(DogerDager game, SpawnSchedule schedule, int ruleIndex) {
        this.game = game;
        this.settings = new SpawnSettings();
        this.schedule = schedule;
        this.rule = schedule.rules.get(ruleIndex);
        this.selectedIndex = 0;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        font.setColor(Color.WHITE);
        title("Spawn Rule", 460);
        drawField(0, "Min floor:", String.valueOf(rule.minFloor));
        drawField(1, "Type:", rule.type.name());
        drawField(2, "Weight:", String.valueOf(rule.weight));
        drawField(3, "Save & back", "");
        drawHelp();
        batch.end();

        handleInput();
    }

    private void drawField(int row, String label, String value) {
        float y = 380 - row * 50;
        font.setColor(selectedIndex == row ? Color.YELLOW : Color.WHITE);
        font.draw(batch, label, 32, y);
        font.draw(batch, value, 280, y);
    }

    private void drawHelp() {
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Use UP/DOWN to move, LEFT/RIGHT to change, ENTER save, ESC back.", 32, 80);
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            selectedIndex = (selectedIndex + 3) % 4;
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % 4;
        }
        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            adjustValue(-1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            adjustValue(1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            if (selectedIndex == 3) {
                saveAndReturn();
            }
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            saveAndReturn();
        }
    }

    private void adjustValue(int delta) {
        switch (selectedIndex) {
            case 0:
                rule.minFloor = Math.max(1, rule.minFloor + delta);
                break;
            case 1:
                SpawnRule.Type[] types = SpawnRule.Type.values();
                int current = rule.type.ordinal();
                int next = (current + delta + types.length) % types.length;
                rule.type = types[next];
                break;
            case 2:
                rule.weight = Math.max(1, rule.weight + delta * 5);
                break;
        }
    }

    private void saveAndReturn() {
        settings.saveSpawnSchedule(schedule);
        settings.apply();
        game.setScreen(new SpawnEditorScreen(game));
        dispose();
    }

    private void title(String text, float y) {
        font.getData().setScale(1.8f);
        font.setColor(Color.WHITE);
        layout.setText(font, text);
        font.draw(batch, text, (PlayScreen.WORLD_W - layout.width) / 2f, y);
        font.getData().setScale(1f);
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
