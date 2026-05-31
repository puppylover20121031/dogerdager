package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.Gdx;
import com.unpuppyable.dogerdager.Pad;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public final class Player extends Entity {

    public static final float SIZE = 16;
    private static final float SPEED = 300;
    private static final float STRAFE_DIST = 110;
    private static final float STRAFE_INVULN = 0.2f;
    private static final float STRAFE_CD = 1.2f;

    private final float maxX;
    private final float maxY;
    private boolean shielded;
    private boolean invulnerable;
    private float anim;
    private float lastDx = 1;
    private float lastDy = 0;
    private float strafeInvuln;
    private float strafeCd;
    private float fromX;
    private float fromY;
    private float stun;
    private float kbX;
    private float kbY;
    private float stamina;

    public Player(float worldW, float playTop) {
        super((worldW - SIZE) / 2f, (playTop - SIZE) / 2f, SIZE);
        maxX = worldW - SIZE;
        maxY = playTop - SIZE;
    }

    @Override
    public void update(float delta) {
        anim += delta;
        if (strafeInvuln > 0) strafeInvuln -= delta;
        if (strafeCd > 0) strafeCd -= delta;

        if (stun > 0) {
            stun -= delta;
            bounds.x = MathUtils.clamp(bounds.x + kbX * delta, 0, maxX);
            bounds.y = MathUtils.clamp(bounds.y + kbY * delta, 0, maxY);
            return;
        }

        float vx = 0, vy = 0;
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT))  vx -= SPEED;
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) vx += SPEED;
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP))    vy += SPEED;
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN))  vy -= SPEED;
        vx = MathUtils.clamp(vx + Pad.moveX() * SPEED, -SPEED, SPEED);
        vy = MathUtils.clamp(vy + Pad.moveY() * SPEED, -SPEED, SPEED);
        if (vx != 0 || vy != 0) {
            lastDx = vx / SPEED;
            lastDy = vy / SPEED;
        }
        bounds.x = MathUtils.clamp(bounds.x + vx * delta, 0, maxX);
        bounds.y = MathUtils.clamp(bounds.y + vy * delta, 0, maxY);

        if ((Gdx.input.isKeyJustPressed(Keys.TAB) || Pad.justA()) && strafeCd <= 0) {
            strafe();
        }
    }

    private void strafe() {
        fromX = bounds.x;
        fromY = bounds.y;
        float len = (float) Math.sqrt(lastDx * lastDx + lastDy * lastDy);
        bounds.x = MathUtils.clamp(bounds.x + lastDx / len * STRAFE_DIST, 0, maxX);
        bounds.y = MathUtils.clamp(bounds.y + lastDy / len * STRAFE_DIST, 0, maxY);
        strafeInvuln = STRAFE_INVULN;
        strafeCd = STRAFE_CD;
    }

    public boolean strafing() {
        return strafeInvuln > 0;
    }

    public void setStamina(float fraction) {
        this.stamina = fraction;
    }

    public void knockback(float worldW, float playTop) {
        float dl = bounds.x;
        float dr = worldW - SIZE - bounds.x;
        float db = bounds.y;
        float dt = playTop - SIZE - bounds.y;
        float min = Math.min(Math.min(dl, dr), Math.min(db, dt));
        kbX = 0;
        kbY = 0;
        if (min == dl) kbX = -700;
        else if (min == dr) kbX = 700;
        else if (min == db) kbY = -700;
        else kbY = 700;
        stun = 1.5f;
    }

    public void setShielded(boolean shielded) {
        this.shielded = shielded;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        if (strafeInvuln > 0) {
            shapes.setColor(0.4f, 0.7f, 1f, 1f);
            shapes.rect(fromX, fromY, bounds.width, bounds.height);
        }
        if (invulnerable && (int) (anim * 10) % 2 == 0) return;

        Color body = stun > 0 ? Color.GRAY : shielded ? Color.SKY : Color.CYAN;
        shapes.setColor(body);
        shapes.rect(bounds.x, bounds.y, SIZE, SIZE);
        shapes.setColor(Color.BLACK);
        shapes.rect(bounds.x + 2, bounds.y + 2, SIZE - 4, SIZE - 4);
        shapes.setColor(body);
        shapes.rect(bounds.x + 2, bounds.y + 2, SIZE - 4, (SIZE - 4) * stamina);
    }
}
