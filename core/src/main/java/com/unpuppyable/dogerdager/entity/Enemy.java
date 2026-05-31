package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public final class Enemy extends Entity {

    public enum Kind { NORMAL, FAST, SMART }

    private static final float SIZE = 24;

    private final Kind kind;
    private final float speed;
    private final float worldW;
    private final float playTop;
    private final Player target;
    private float vx;
    private float vy;

    public Enemy(Kind kind, float x, float y, float baseSpeed, float worldW, float playTop, Player target) {
        super(x, y, SIZE);
        this.kind = kind;
        this.speed = kind == Kind.FAST ? baseSpeed * 1.6f : baseSpeed;
        this.worldW = worldW;
        this.playTop = playTop;
        this.target = target;
        this.vx = speed;
        this.vy = speed;
    }

    @Override
    public void update(float delta) {
        if (kind == Kind.SMART) {
            float dx = target.bounds().x - bounds.x;
            float dy = target.bounds().y - bounds.y;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len > 0.001f) {
                vx = dx / len * speed;
                vy = dy / len * speed;
            }
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
            case FAST -> Color.ORANGE;
            case SMART -> Color.MAGENTA;
        });
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
