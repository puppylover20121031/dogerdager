package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class Potion extends Entity {
    public static final float SIZE = 16;
    public float life = 8f;
    private float anim;

    public Potion(float x, float y) {
        super(x, y, SIZE);
        this.name = "Potion";
    }

    @Override
    public boolean heals() {
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
        shapes.setColor(Color.CYAN);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
