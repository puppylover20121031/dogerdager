package com.unpuppyable.dogerdager;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class Hud {

    private static final float MAX_STAMINA = 1200;
    private static final float DRAIN = 300;
    private static final float REGEN = 60;
    private static final float BAR_W = 600;
    private static final float HIT_GRACE = 0.7f;

    private final float worldW;
    private final float worldH;
    private final int maxHealth;
    private final float scoreRate;

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

    public void damage(int amount) {
        if (shieldActive || invuln > 0) return;
        health = Math.max(0, health - amount);
        invuln = HIT_GRACE;
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

    public void drawBars(ShapeRenderer shapes) {
        float healthY = worldH - 32;
        float staminaY = worldH - 58;

        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(20, healthY, BAR_W, 22);
        shapes.setColor(Color.GREEN);
        shapes.rect(20, healthY, BAR_W * health / maxHealth, 22);

        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(20, staminaY, BAR_W, 18);
        shapes.setColor(shieldActive ? Color.SKY : Color.YELLOW);
        shapes.rect(20, staminaY, BAR_W * stamina / MAX_STAMINA, 18);
    }

    public void drawText(SpriteBatch batch, BitmapFont font) {
        font.setColor(Color.WHITE);
        font.draw(batch, "Score " + score + "    Level " + level + "    Best " + highScore, 20, worldH - 64);
    }
}
