package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class SpawnEditorScreen extends ScreenAdapter {

    private static final int VISIBLE_RULES = 5;

    private final DogerDager game;
    private final SpawnSettings settings = new SpawnSettings();
    private final SpawnSchedule schedule;
    private final Viewport viewport = new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private int index;
    private int scrollTop;
    private boolean switching;

    private float setSpawnStart;
    private float setSpawnRamp;
    private float setSpawnMin;
    private int setCentipedeFloor;
    private float setCentipedeChance;
    private int setSmartFloor;
    private float setFastChance;
    private float setSmartChance;
    private float setPotionChance;

    public SpawnEditorScreen(DogerDager game) {
        this.game = game;
        this.schedule = settings.customSpawnSchedule();
        this.setSpawnStart = settings.customSpawnStart();
        this.setSpawnRamp = settings.customSpawnRamp();
        this.setSpawnMin = settings.customSpawnMin();
        this.setCentipedeFloor = settings.customCentipedeFloor();
        this.setCentipedeChance = settings.customCentipedeChance();
        this.setSmartFloor = settings.customSmartFloor();
        this.setFastChance = settings.customFastChance();
        this.setSmartChance = settings.customSmartChance();
        this.setPotionChance = settings.customPotionChance();
        settings.apply();
    }

    @Override
    public void render(float delta) {
        if (switching) {
            return;
        }
        handleKeys();
        if (switching) {
            return;
        }
        draw();
    }

    private void handleKeys() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            switching = true;
            game.setScreen(new CustomScreen(game));
            dispose();
            return;
        }

        int items = 3 + schedule.rules.size() + 3;
        if (Gdx.input.isKeyJustPressed(Keys.W) || Gdx.input.isKeyJustPressed(Keys.UP)) {
            index = (index + items - 1) % items;
            scrollRules();
        }
        if (Gdx.input.isKeyJustPressed(Keys.S) || Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            index = (index + 1) % items;
            scrollRules();
        }

        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            int ruleStart = 3;
            int ruleEnd = ruleStart + schedule.rules.size();
            if (index >= ruleStart && index < ruleEnd) {
                switching = true;
                game.setScreen(new SpawnRuleEditorScreen(game, schedule, index - ruleStart));
                dispose();
                return;
            }
            if (index == ruleEnd) {
                addRule();
                return;
            }
            if (index == ruleEnd + 1) {
                removeRule();
                return;
            }
            if (index == ruleEnd + 2) {
                saveAndReturn();
                return;
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            adjustSpawnSettings(-1);
        } else if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            adjustSpawnSettings(1);
        }
    }

    private void adjustSpawnSettings(int delta) {
        if (index == 0 && setSpawnStart >= 1f) {
            setSpawnStart = MathUtils.clamp(setSpawnStart + delta * 0.1f, 1f, 8f);
            settings.setSpawnStart(setSpawnStart);
            settings.apply();
        } else if (index == 1) {
            setSpawnRamp = MathUtils.clamp(setSpawnRamp + delta * 0.05f, 0.05f, 1f);
            settings.setSpawnRamp(setSpawnRamp);
            settings.apply();
        } else if (index == 2) {
            setSpawnMin = MathUtils.clamp(setSpawnMin + delta * 0.1f, 0.5f, 3f);
            settings.setSpawnMin(setSpawnMin);
            settings.apply();
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        title("spawn editor", 470);
        line(0, "Spawn delay start", String.format("%.1f", setSpawnStart), 420);
        line(1, "Spawn ramp", String.format("%.2f", setSpawnRamp), 390);
        line(2, "Spawn min", String.format("%.1f", setSpawnMin), 360);

        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Spawn rules", 32, 330);

        if (schedule.rules.isEmpty()) {
            font.setColor(Color.GRAY);
            font.draw(batch, "No rules configured. Press Enter to add one.", 32, 300);
        }

        int ruleStart = 3;
        int ruleEnd = ruleStart + schedule.rules.size();
        int firstVisible = scrollTop;
        int lastVisible = Math.min(schedule.rules.size(), scrollTop + VISIBLE_RULES);
        for (int i = firstVisible; i < lastVisible; i++) {
            String label = "Rule " + (i + 1);
            String value = schedule.rules.get(i).displayName();
            line(ruleStart + i, label, value, 300 - (i - firstVisible) * 30);
        }

        int actionBase = ruleEnd;
        line(actionBase, "Add rule", "", 120);
        line(actionBase + 1, "Remove rule", "", 90);
        line(actionBase + 2, "Save & back", "", 60);

        font.setColor(Color.GRAY);
        centered("up/down select    left/right change    Enter activate    Esc cancel", 25);
        batch.end();
    }

    private void scrollRules() {
        int ruleStart = 3;
        int ruleIndex = index - ruleStart;
        if (ruleIndex >= 0 && ruleIndex < schedule.rules.size()) {
            if (ruleIndex < scrollTop) {
                scrollTop = ruleIndex;
            } else if (ruleIndex >= scrollTop + VISIBLE_RULES) {
                scrollTop = ruleIndex - VISIBLE_RULES + 1;
            }
        }
    }

    private void addRule() {
        schedule.rules.add(new SpawnRule(1, 10, SpawnRule.Type.NORMAL));
        settings.saveSpawnSchedule(schedule);
        settings.apply();
        index = 3 + schedule.rules.size() - 1;
        scrollTop = Math.max(0, schedule.rules.size() - VISIBLE_RULES);
    }

    private void removeRule() {
        if (schedule.rules.isEmpty()) {
            return;
        }
        int removeIndex = Math.max(0, index - 3);
        if (removeIndex < schedule.rules.size()) {
            schedule.rules.remove(removeIndex);
            settings.saveSpawnSchedule(schedule);
            settings.apply();
            int ruleEnd = 3 + schedule.rules.size();
            index = Math.min(index, ruleEnd + 2);
            scrollTop = Math.max(0, Math.min(scrollTop, Math.max(0, schedule.rules.size() - VISIBLE_RULES)));
        }
    }

    private void saveAndReturn() {
        settings.saveSpawnSchedule(schedule);
        settings.apply();
        switching = true;
        game.setScreen(new CustomScreen(game));
        dispose();
    }

    private void line(int i, String name, String value, float y) {
        font.setColor(i == index ? Color.YELLOW : Color.WHITE);
        font.draw(batch, (i == index ? "> " : "  ") + name, 32, y);
        font.draw(batch, value, 320, y);
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
