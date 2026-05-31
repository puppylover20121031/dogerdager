package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public final class Enemy extends Entity {

    public enum Kind { NORMAL, FAST, SMART }

    private static final float SIZE = 24;

    private final Kind kind;
    private final float speed;
    private final int damage;
    private final float worldW;
    private final float playTop;
    private final Player target;
    private float vx;
    private float vy;

    public Enemy(Kind kind, float x, float y, float baseSpeed, float worldW, float playTop, Player target) {
        super(x, y, SIZE);
        this.kind = kind;
        this.speed = switch (kind) {
            case FAST -> baseSpeed * 1.6f;
            case SMART -> baseSpeed * 0.6f;
            case NORMAL -> baseSpeed;
        };
        this.damage = switch (kind) {
            case NORMAL -> 10;
            case FAST -> 14;
            case SMART -> 18;
        };
        this.worldW = worldW;
        this.playTop = playTop;
        this.target = target;
        this.vx = speed;
        this.vy = speed;
    }

    public int damage() {
        return damage;
    }

    @Override
    public void update(float delta) {
        if (kind == Kind.SMART) {
            float desired = MathUtils.atan2(target.bounds().y - bounds.y, target.bounds().x - bounds.x);
            float cur = MathUtils.atan2(vy, vx);
            float diff = desired - cur;
            while (diff > MathUtils.PI) diff -= MathUtils.PI2;
            while (diff < -MathUtils.PI) diff += MathUtils.PI2;
            cur += MathUtils.clamp(diff, -1.8f * delta, 1.8f * delta);
            vx = MathUtils.cos(cur) * speed;
            vy = MathUtils.sin(cur) * speed;
        } else {
            if (bounds.x <= 0 || bounds.x >= worldW - SIZE) vx = -vx;
            if (bounds.y <= 0 || bounds.y >= playTop - SIZE) vy = -vy;
        }
        bounds.x = MathUtils.clamp(bounds.x + vx * delta, 0, worldW - SIZE);
        bounds.y = MathUtils.clamp(bounds.y + vy * delta, 0, playTop - SIZE);
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        shapes.setColor(switch (kind) {
            case NORMAL -> Color.RED;
            case FAST -> Color.GRAY;
            case SMART -> Color.GREEN;
        });
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
