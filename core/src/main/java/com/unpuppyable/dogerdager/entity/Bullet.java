package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public final class Bullet extends Entity {

    public enum Kind { FALLING, HOMING, ROCKET }

    private final Kind kind;
    private final int damage;
    private final float worldW;
    private final Player target;
    private float vx;
    private float vy;
    private float life;

    public Bullet(Kind kind, float x, float y, float worldW, Player target) {
        super(x, y, kind == Kind.FALLING ? 32 : kind == Kind.ROCKET ? 18 : 16);
        this.kind = kind;
        this.worldW = worldW;
        this.target = target;
        this.damage = switch (kind) {
            case FALLING -> 10;
            case HOMING -> 16;
            case ROCKET -> 22;
        };
        if (kind == Kind.FALLING) {
            vx = MathUtils.random(-180f, 180f);
            vy = -300f;
        } else {
            life = kind == Kind.ROCKET ? 8f : 6f;
        }
    }

    public int damage() {
        return damage;
    }

    public boolean rocket() {
        return kind == Kind.ROCKET;
    }

    @Override
    public void update(float delta) {
        if (kind != Kind.FALLING) {
            float speed = kind == Kind.ROCKET ? 200f : 120f;
            float dx = target.bounds().x - bounds.x;
            float dy = target.bounds().y - bounds.y;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len > 0.001f) {
                vx = dx / len * speed;
                vy = dy / len * speed;
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
        shapes.setColor(switch (kind) {
            case ROCKET -> Color.GOLD;
            case HOMING -> Color.ROYAL;
            case FALLING -> Color.RED;
        });
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
