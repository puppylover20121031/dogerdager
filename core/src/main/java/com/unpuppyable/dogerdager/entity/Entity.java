package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public sealed abstract class Entity permits Player, Enemy, Boss, Bullet, Potion {

    protected final Rectangle bounds;
    protected boolean dead;

    protected Entity(float x, float y, float size) {
        bounds = new Rectangle(x, y, size, size);
    }

    public abstract void update(float delta);

    public abstract void draw(ShapeRenderer shapes);

    public Rectangle bounds() {
        return bounds;
    }

    public boolean dead() {
        return dead;
    }

    public void kill() {
        dead = true;
    }
}
