package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class Powerup1 extends Entity {

    private float life = 8f;
    private float anim;

    public Powerup1(float x, float y) {
        super(x, y, 16);
    }

    @Override
    public boolean heals() {
        return false;
    }

    @Override
    public boolean glitches() {
        return true;
    }

    @Override
    public void update(float delta) {
        anim += delta;
        life -= delta;
        if (life <= 0) dead = true;
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        if (life < 2f && (int) (anim * 8) % 2 == 0) return;
        shapes.setColor(Color.WHITE);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
