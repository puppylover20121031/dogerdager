package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unpuppyable.dogerdager.entity.Player;

public final class Hud {

    private static final float MAX_STAMINA = 1200;
    private static final float DRAIN = 300;
    private static final float REGEN = 60;
    private static final float HIT_GRACE = 0.7f;
    private static final float BAND = 72;

    private final float worldW;
    private final float worldH;
    private final int maxHealth;
    private final int winLevel;
    private final float scoreRate;
    private final GlyphLayout layout = new GlyphLayout();

    private int health;
    private float stamina = MAX_STAMINA;
    private float scoreAcc;
    private int score;
    private int level = 1;
    private int highScore;
    private boolean shieldActive;
    private float invuln;

    public Hud(Difficulty difficulty, int highScore, float worldW, float worldH) {
        this.maxHealth = difficulty.maxHealth;
        this.winLevel = difficulty.winLevel;
        this.scoreRate = REGEN * difficulty.scoreMultiplier;
        this.health = maxHealth;
        this.highScore = highScore;
        this.worldW = worldW;
        this.worldH = worldH;
    }

    public boolean update(float delta, boolean shieldHeld) {
        if (invuln > 0) invuln -= delta;
        shieldActive = shieldHeld && stamina > 3;
        if (shieldActive) stamina = Math.max(0, stamina - DRAIN * delta);
        else if (stamina < MAX_STAMINA) stamina = Math.min(MAX_STAMINA, stamina + REGEN * delta);

        scoreAcc += scoreRate * delta;
        while (scoreAcc >= 1) {
            scoreAcc -= 1;
            score++;
        }
        if (score > highScore) highScore = score;
        return shieldActive;
    }

    public boolean damage(int amount) {
        if (shieldActive || invuln > 0) return false;
        health = Math.max(0, health - amount);
        invuln = HIT_GRACE;
        return true;
    }

    public boolean invulnerable() {
        return invuln > 0;
    }

    public void healFull() {
        health = maxHealth;
    }

    public int nextLevel() {
        return ++level;
    }

    public boolean dead() {
        return health <= 0;
    }

    public int level() {
        return level;
    }

    public int highScore() {
        return highScore;
    }

    // Filled pass: top band + corner stat bars.
    public void drawBars(ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setColor(0f, 0f, 0f, 0.45f);
        shapes.rect(0, worldH - BAND, worldW, BAND);

        float hpX = 18, hpY = worldH - 28, hpW = 150, hpH = 12;
        shapes.setColor(0.15f, 0.15f, 0.15f, 1f);
        shapes.rect(hpX, hpY, hpW, hpH);
        shapes.setColor(Color.LIME);
        shapes.rect(hpX, hpY, hpW * health / maxHealth, hpH);

        float lvY = worldH - 46, lvW = 110, lvH = 5;
        shapes.setColor(0.15f, 0.15f, 0.15f, 1f);
        shapes.rect(hpX, lvY, lvW, lvH);
        shapes.setColor(Color.SKY);
        shapes.rect(hpX, lvY, lvW * Math.min(1f, (float) level / winLevel), lvH);

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // Line pass: stamina + dash readiness as rings around the player.
    public void drawRings(ShapeRenderer shapes, Player player) {
        float cx = player.bounds().x + Player.SIZE / 2f;
        float cy = player.bounds().y + Player.SIZE / 2f;

        float staminaFrac = stamina / MAX_STAMINA;
        shapes.setColor(shieldActive ? Color.SKY : Color.GOLD);
        shapes.arc(cx, cy, 16, 90, 360 * staminaFrac);
        shapes.arc(cx, cy, 17, 90, 360 * staminaFrac);

        float dash = player.dashCharge();
        shapes.setColor(dash >= 1f ? Color.LIME : Color.GRAY);
        shapes.arc(cx, cy, 21, 90, 360 * dash);
    }

    // Batch pass: HP/level (left), score/best (right).
    public void drawText(SpriteBatch batch, BitmapFont font) {
        font.setColor(Color.WHITE);
        font.draw(batch, health + "/" + maxHealth, 174, worldH - 18);
        font.draw(batch, "LV " + level, 18, worldH - 50);
        drawRight(batch, font, "SCORE " + score, worldH - 18);
        drawRight(batch, font, "BEST " + highScore, worldH - 40);
    }

    private void drawRight(SpriteBatch batch, BitmapFont font, String text, float y) {
        layout.setText(font, text);
        font.draw(batch, text, worldW - 18 - layout.width, y);
    }
}
