package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class Potion extends Entity {

    public Potion(float x, float y) {
        super(x, y, 16);
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        shapes.setColor(Color.CYAN);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
