package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class Hud {

    private static final float MAX_STAMINA = 1200;
    private static final float DRAIN = 300;
    private static final float REGEN = 60;
    private static final float HIT_GRACE = 0.7f;
    private static final float BAND = 72;

    private static final Color HEART_ON = Color.SCARLET;
    private static final Color HEART_OFF = new Color(0.22f, 0.10f, 0.12f, 1f);

    private final float worldH;
    private final float worldW;
    private final int maxHealth;
    private final GlyphLayout layout = new GlyphLayout();

    private int health;
    private float stamina = MAX_STAMINA;
    private int floor = 1;
    private float runTime;
    private float floorProgress;
    private int bestFloor;
    private boolean shieldActive;
    private boolean staminaLocked;
    private float invuln;
    private Difficulty difficulty;

    public Hud(Difficulty difficulty, int bestFloor, float worldW, float worldH) {
        this.worldW = worldW;
        this.worldH = worldH;
        this.maxHealth = difficulty.maxHealth;
        this.health = maxHealth;
        this.difficulty = difficulty;
        this.bestFloor = bestFloor;
    }

    public boolean update(float delta, boolean shieldHeld) {
        if (difficulty == Difficulty.HARDCORE || difficulty == Difficulty.HARD) stamina = 3600;
        if (invuln > 0) invuln -= delta;
        if (stamina <= 0) staminaLocked = true;
        else if (stamina >= 300) staminaLocked = false;
        shieldActive = shieldHeld && !staminaLocked && stamina > 0;
        if (shieldActive) stamina = Math.max(0, stamina - DRAIN * delta);
        else if (stamina < MAX_STAMINA) stamina = Math.min(MAX_STAMINA, stamina + REGEN * delta);
        runTime += delta;
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

    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public void healFull() {
        health = maxHealth;
    }

    public void refillStamina() {
        stamina = MAX_STAMINA;
        staminaLocked = false;
    }

    public int advanceFloor() {
        floor++;
        if (floor > bestFloor) bestFloor = floor;
        return floor;
    }

    public int floor() {
        return floor;
    }

    public void setFloorProgress(float fraction) {
        floorProgress = fraction;
    }

    public boolean dead() {
        return health <= 0;
    }

    public int highScore() {
        return bestFloor;
    }

    public float staminaFraction() {
        return stamina / MAX_STAMINA;
    }

    // Filled pass: top band, heart row, floor-progress bar.
    public void drawBars(ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setColor(0f, 0f, 0f, 0.45f);
        shapes.rect(0, worldH - BAND, worldW, BAND);

        float hx = 28, hy = worldH - 26, r = 6, gap = 22;
        for (int i = 0; i < maxHealth; i++) {
            heart(shapes, hx + i * gap, hy, r, i < health ? HEART_ON : HEART_OFF);
        }

        float pbX = 22, pbY = worldH - 46, pbW = 150, pbH = 5;
        shapes.setColor(0.15f, 0.15f, 0.15f, 1f);
        shapes.rect(pbX, pbY, pbW, pbH);
        shapes.setColor(Color.SKY);
        shapes.rect(pbX, pbY, pbW * Math.min(1f, floorProgress), pbH);

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void heart(ShapeRenderer shapes, float cx, float cy, float r, Color c) {
        shapes.setColor(c);
        shapes.circle(cx - r * 0.45f, cy + r * 0.35f, r * 0.6f);
        shapes.circle(cx + r * 0.45f, cy + r * 0.35f, r * 0.6f);
        shapes.triangle(cx - r, cy + r * 0.45f, cx + r, cy + r * 0.45f, cx, cy - r * 0.9f);
    }

    // Batch pass: floor (left), run time and best depth (right).
    public void drawText(SpriteBatch batch, BitmapFont font) {
        font.setColor(Color.WHITE);
        font.draw(batch, "FLOOR " + floor, 28, worldH - 52);
        drawRight(batch, font, "TIME " + time(), worldH - 18);
        drawRight(batch, font, "BEST F" + bestFloor, worldH - 40);
    }


    private String time() {
        int s = (int) runTime;
        return s / 60 + ":" + String.format("%02d", s % 60);
    }

    private void drawRight(SpriteBatch batch, BitmapFont font, String text, float y) {
        layout.setText(font, text);
        font.draw(batch, text, worldW - 18 - layout.width, y);
    }
}
