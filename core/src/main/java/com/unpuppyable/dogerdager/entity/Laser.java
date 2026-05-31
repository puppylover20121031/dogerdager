package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class Laser extends Entity {

    private static final float TELEGRAPH = 1.2f;
    private static final float ACTIVE = 1.0f;

    private float telegraph = TELEGRAPH;
    private float active = ACTIVE;

    public Laser(float x, float width, float playTop) {
        super(x, 0, width, playTop);
    }

    private boolean firing() {
        return telegraph <= 0 && active > 0;
    }

    @Override
    public void update(float delta) {
        if (telegraph > 0) {
            telegraph -= delta;
        } else {
            active -= delta;
            if (active <= 0) dead = true;
        }
    }

    @Override
    public int contactDamage() {
        return firing() ? 30 : 0;
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        if (telegraph > 0) {
            shapes.setColor(0.6f, 0.05f, 0.05f, 1f);
            float cx = bounds.x + bounds.width / 2f;
            shapes.rect(cx - 1.5f, bounds.y, 3, bounds.height);
        } else {
            shapes.setColor(1f, 0.25f, 0.2f, 1f);
            shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}
