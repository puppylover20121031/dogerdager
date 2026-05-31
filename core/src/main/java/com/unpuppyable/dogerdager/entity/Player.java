package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public final class Player extends Entity {

    public static final float SIZE = 16;
    private static final float SPEED = 300;

    private final float maxX;
    private final float maxY;
    private boolean shielded;
    private boolean invulnerable;
    private float anim;

    public Player(float worldW, float playTop) {
        super((worldW - SIZE) / 2f, (playTop - SIZE) / 2f, SIZE);
        maxX = worldW - SIZE;
        maxY = playTop - SIZE;
    }

    @Override
    public void update(float delta) {
        anim += delta;
        float vx = 0, vy = 0;
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT))  vx -= SPEED;
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) vx += SPEED;
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP))    vy += SPEED;
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN))  vy -= SPEED;
        bounds.x = MathUtils.clamp(bounds.x + vx * delta, 0, maxX);
        bounds.y = MathUtils.clamp(bounds.y + vy * delta, 0, maxY);
    }

    public void setShielded(boolean shielded) {
        this.shielded = shielded;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        if (invulnerable && (int) (anim * 10) % 2 == 0) return;
        shapes.setColor(shielded ? Color.SKY : Color.WHITE);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
