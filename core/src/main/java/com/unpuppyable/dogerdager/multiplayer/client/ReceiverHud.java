package com.unpuppyable.dogerdager.multiplayer.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unpuppyable.dogerdager.Difficulty;

public final class ReceiverHud {
    private static final float BAND = 72;
    public static final int MAX_CARDS = 5;

    private static final Color HEART_ON = Color.SCARLET;
    private static final Color HEART_OFF = new Color(0.22f, 0.10f, 0.12f, 1f);

    private static final Color CARD_ON = new Color(0.25f, 0.75f, 1f, 1f);
    private static final Color CARD_OFF = new Color(0.18f, 0.22f, 0.25f, 1f);

    private final float worldH;
    private final float worldW;
    private int maxHealth;
    private final GlyphLayout layout = new GlyphLayout();

    private int floor = 1;
    private float runTime;
    private float floorProgress;
    private int bestFloor;
    private final boolean[] cards = new boolean[MAX_CARDS];
    private ClientPlayScreen.EntityState player;

    public ReceiverHud(Difficulty difficulty, int bestFloor, float worldW, float worldH) {
        this.worldW = worldW;
        this.worldH = worldH;
        this.maxHealth = difficulty.maxHealth;
        this.bestFloor = bestFloor;
    }

    public void update(float delta) {
        runTime += delta;
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

    public int highScore() {
        return bestFloor;
    }

    // Filled pass: top band, heart row, floor-progress bar.
    public void drawBars(ShapeRenderer shapes) {
        this.player = ClientPlayScreen.getInstance().getPlayer();
        if (player == null) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setColor(0f, 0f, 0f, 0.45f);
        shapes.rect(0, worldH - BAND, worldW, BAND);

        float hx = 28, hy = worldH - 26, r = 6, gap = 22;
        for (int i = 0; i < player.maxHp; i++) {
            heart(shapes, hx + i * gap, hy, r, i < player.hp ? HEART_ON : HEART_OFF);
        }

        float cardX = 255, cardY = worldH - 40, cardW = 14, cardH = 18, cardGap = 5;
        for (int i = 0; i < MAX_CARDS; i++) {
            card(shapes, cardX + i * (cardW + cardGap), cardY, cardW, cardH,
                    cards[i] ? CARD_ON : CARD_OFF);
        }

        float pbX = 22, pbY = worldH - 46, pbW = 150, pbH = 5;
        shapes.setColor(0.15f, 0.15f, 0.15f, 1f);
        shapes.rect(pbX, pbY, pbW, pbH);
        shapes.setColor(Color.SKY);
        shapes.rect(pbX, pbY, pbW * Math.min(1f, floorProgress), pbH);

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // Batch pass: floor (left), run time and best depth (right).
    public void drawText(SpriteBatch batch, BitmapFont font) {
        font.setColor(Color.WHITE);
        font.draw(batch, "FLOOR " + floor, 28, worldH - 52);
        drawRight(batch, font, "TIME " + time(), worldH - 18);
        drawRight(batch, font, "BEST F" + bestFloor, worldH - 40);
    }

    private void heart(ShapeRenderer shapes, float cx, float cy, float r, Color c) {
        shapes.setColor(c);
        shapes.circle(cx - r * 0.45f, cy + r * 0.35f, r * 0.6f);
        shapes.circle(cx + r * 0.45f, cy + r * 0.35f, r * 0.6f);
        shapes.triangle(cx - r, cy + r * 0.45f, cx + r, cy + r * 0.45f, cx, cy - r * 0.9f);
    }

    private void card(ShapeRenderer shapes, float x, float y, float width, float height, Color color) {
        shapes.setColor(color);
        shapes.rect(x, y, width, height);
        shapes.setColor(0.08f, 0.12f, 0.16f, 1f);
        float cx = x + width / 2f;
        float cy = y + height / 2f;
        shapes.triangle(cx, cy + 3, cx + 2, cy, cx, cy - 3);
        shapes.triangle(cx, cy + 3, cx - 2, cy, cx, cy - 3);
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
