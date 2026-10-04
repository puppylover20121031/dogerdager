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

    private static final float BAND = 72;
    public static final int MAX_CARDS = 10;

    private static final Color HEART_ON = Color.SCARLET;
    private static final Color HEART_OFF = new Color(0.22f, 0.10f, 0.12f, 1f);
    private static final Color CARD_ON = new Color(0.25f, 0.75f, 1f, 1f);
    private static final Color CARD_OFF = new Color(0.18f, 0.22f, 0.25f, 1f);

    private final float worldH;
    private final float worldW;
    private final GlyphLayout layout = new GlyphLayout();

    private int floor = 1;
    private float runTime;
    private float floorProgress;
    private int bestFloor;
    private final boolean[] cards = new boolean[MAX_CARDS];
    private Player player;

    public Hud(Difficulty difficulty, int bestFloor, float worldW, float worldH, Player player) {
        this.worldW = worldW;
        this.worldH = worldH;
        this.bestFloor = bestFloor;
        this.player = player;
    }
    //moved all player logic right where it belongs: into the trash
    //jk it's in Player
    public void update(float delta, Player player) {
        runTime += delta;
        this.player = player;
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

    public int cardCount() {
        int count = 0;
        for (boolean active : cards) {
            if (active) count++;
        }
        return count;
    }

    public void setCardCount(int count) {
        int remaining = Math.max(0, Math.min(MAX_CARDS, count));
        for (int i = 0; i < MAX_CARDS; i++) {
            cards[i] = i < remaining;
        }
        updateCardEffects();
    }

    public void changeCards(int amount) {
        if (amount > 0) {
            for (int i = 0; i < MAX_CARDS && amount > 0; i++) {
                if (!cards[i]) {
                    cards[i] = true;
                    amount--;
                }
            }
        } else {
            for (int i = MAX_CARDS - 1; i >= 0 && amount < 0; i--) {
                if (cards[i]) {
                    cards[i] = false;
                    amount++;
                }
            }
        }
        updateCardEffects();
    }

    public void addCard() {
        changeCards(1);
    }

    public void removeCard() {
        changeCards(-1);
    }

    public boolean cardActive(int index) {
        checkCardIndex(index);
        return cards[index];
    }

    public void setCardActive(int index, boolean active) {
        checkCardIndex(index);
        cards[index] = active;
        updateCardEffects();
    }

    public void toggleCard(int index) {
        checkCardIndex(index);
        cards[index] = !cards[index];
        updateCardEffects();
    }

    private void updateCardEffects() {
        player.setBonusMaxHealth(cards[0] ? 4 : 0);
    }

    private void checkCardIndex(int index) {
        if (index < 0 || index >= MAX_CARDS) {
            throw new IndexOutOfBoundsException("Card index must be between 0 and " + (MAX_CARDS - 1));
        }
    }

    public int highScore() {
        return bestFloor;
    }

    // Filled pass: top band, heart row, floor-progress bar.
    public void drawBars(ShapeRenderer shapes, Player player) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setColor(0f, 0f, 0f, 0.45f);
        shapes.rect(0, worldH - BAND, worldW, BAND);

        float hx = 28, hy = worldH - 26, r = 6, gap = 22;
        for (int i = 0; i < player.maxHealth; i++) {
            heart(shapes, hx + i * gap, hy, r, i < player.health ? HEART_ON : HEART_OFF);
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
