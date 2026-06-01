package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class PlayerArrow extends Entity {

    public static final float SIZE = 8f;

    private final float worldW;
    private final float playTop;
    private final float vx;
    private final float vy;

    public PlayerArrow(float x, float y, float vx, float vy, float worldW, float playTop) {
        super(x, y, SIZE);
        this.vx = vx;
        this.vy = vy;
        this.worldW = worldW;
        this.playTop = playTop;
    }

    @Override
    public void update(float delta) {
        bounds.x += vx * delta;
        bounds.y += vy * delta;
        if (bounds.x < -50 || bounds.x > worldW + 50 || bounds.y < -50 || bounds.y > playTop + 50) {
            dead = true;
        }
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        shapes.setColor(Color.GOLD);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public int contactDamage() {
        return 0;
    }

    @Override
    public boolean diesOnPlayerHit() {
        return false;
    }
}
