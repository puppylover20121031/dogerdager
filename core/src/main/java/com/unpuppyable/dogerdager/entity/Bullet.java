package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public final class Bullet extends Entity {

    public enum Kind { FALLING, HOMING, ROCKET }

    private static final float ROCKET_SPEED = 200f;
    private static final float ROCKET_TURN = 2.2f;
    private static final int TRAIL = 6;

    private final Kind kind;
    private final int damage;
    private final float worldW;
    private final Player target;
    private final float[] tx = new float[TRAIL];
    private final float[] ty = new float[TRAIL];
    private int head;
    private int filled;
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
        if (kind == Kind.ROCKET) {
            float dx = target.bounds().x - x;
            float dy = target.bounds().y - y;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            vx = len > 0.001f ? dx / len * ROCKET_SPEED : 0;
            vy = len > 0.001f ? dy / len * ROCKET_SPEED : -ROCKET_SPEED;
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
        if (kind == Kind.ROCKET) {
            // Steer toward the player at a limited turn rate so it has momentum:
            // a clean dodge makes it overshoot and fly off-screen.
            float desired = MathUtils.atan2(target.bounds().y - bounds.y, target.bounds().x - bounds.x);
            float cur = MathUtils.atan2(vy, vx);
            float diff = desired - cur;
            while (diff > MathUtils.PI) diff -= MathUtils.PI2;
            while (diff < -MathUtils.PI) diff += MathUtils.PI2;
            cur += MathUtils.clamp(diff, -ROCKET_TURN * delta, ROCKET_TURN * delta);
            vx = MathUtils.cos(cur) * ROCKET_SPEED;
            vy = MathUtils.sin(cur) * ROCKET_SPEED;
            life -= delta;
            if (life <= 0) dead = true;
        } else if (kind == Kind.HOMING) {
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
        if (kind == Kind.ROCKET) {
            tx[head] = bounds.x + bounds.width / 2f;
            ty[head] = bounds.y + bounds.height / 2f;
            head = (head + 1) % TRAIL;
            if (filled < TRAIL) filled++;
        }
        if (bounds.x < -50 || bounds.x > worldW + 50 || bounds.y < -50 || bounds.y > worldW) dead = true;
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        if (kind == Kind.ROCKET) {
            for (int i = 0; i < filled; i++) {
                int idx = (head - 1 - i + 2 * TRAIL) % TRAIL;
                float t = 1f - (float) i / TRAIL;
                float s = 7f * t;
                shapes.setColor(0.9f * t, 0.7f * t, 0.1f * t, 1f);
                shapes.rect(tx[idx] - s / 2f, ty[idx] - s / 2f, s, s);
            }
            float cx = bounds.x + bounds.width / 2f;
            float cy = bounds.y + bounds.height / 2f;
            float a = MathUtils.atan2(vy, vx);
            float cos = MathUtils.cos(a);
            float sin = MathUtils.sin(a);
            float nose = 13, back = 8, side = 6;
            shapes.setColor(Color.GOLD);
            shapes.triangle(
                    cx + cos * nose, cy + sin * nose,
                    cx - cos * back - sin * side, cy - sin * back + cos * side,
                    cx - cos * back + sin * side, cy - sin * back - cos * side);
            return;
        }
        shapes.setColor(kind == Kind.HOMING ? Color.ROYAL : Color.RED);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
