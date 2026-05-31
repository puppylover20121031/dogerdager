package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {

    protected final Rectangle bounds;
    protected boolean dead;

    protected Entity(float x, float y, float size) {
        bounds = new Rectangle(x, y, size, size);
    }

    protected Entity(float x, float y, float width, float height) {
        bounds = new Rectangle(x, y, width, height);
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

    // Hazard contract — defaults are inert; subclasses opt in.
    public int contactDamage() {
        return 0;
    }

    public boolean knocksBack() {
        return false;
    }

    public boolean diesOnPlayerHit() {
        return false;
    }

    public boolean isBoss() {
        return false;
    }

    public boolean heals() {
        return false;
    }
}
