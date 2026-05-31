package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public final class Bullet extends Entity {

    public enum Kind { FALLING, HOMING }

    private final Kind kind;
    private final int damage;
    private final float worldW;
    private final Player target;
    private float vx;
    private float vy;
    private float life;

    public Bullet(Kind kind, float x, float y, float worldW, Player target) {
        super(x, y, kind == Kind.HOMING ? 16 : 32);
        this.kind = kind;
        this.worldW = worldW;
        this.target = target;
        if (kind == Kind.FALLING) {
            damage = 5;
            vx = MathUtils.random(-180f, 180f);
            vy = -300f;
        } else {
            damage = 10;
            life = 6f;
        }
    }

    public int damage() {
        return damage;
    }

    @Override
    public void update(float delta) {
        if (kind == Kind.HOMING) {
            float dx = target.bounds().x - bounds.x;
            float dy = target.bounds().y - bounds.y;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len > 0.001f) {
                vx = dx / len * 120f;
                vy = dy / len * 120f;
            }
            life -= delta;
            if (life <= 0) dead = true;
        }
        bounds.x += vx * delta;
        bounds.y += vy * delta;
        if (bounds.y < -bounds.height || bounds.x < -bounds.height || bounds.x > worldW) dead = true;
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        shapes.setColor(kind == Kind.HOMING ? Color.ROYAL : Color.RED);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
