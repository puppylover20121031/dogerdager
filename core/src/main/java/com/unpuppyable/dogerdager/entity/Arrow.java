package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class Arrow extends Entity {

    private static final float SIZE = 12;
    private static final float SPEED = 600;

    private final float worldW;

    public Arrow(float x, float y, float worldW) {
        super(x, y, SIZE);
        this.worldW = worldW;
    }

    @Override
    public void update(float delta) {
        bounds.x += SPEED * delta;
        if (bounds.x > worldW) dead = true;
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        shapes.setColor(Color.CYAN);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
