package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.util.UUID;

public abstract class Entity {

    protected final Rectangle bounds;
    protected boolean dead;
    public String id = UUID.randomUUID().toString();
    public String name;

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

    public void revive() { dead = false; }

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

    protected Player target;
    public Player getTarget() { return target; }
    public void setTarget(Player target) { this.target = target; }

    public boolean isBoss() {
        return false;
    }

    public boolean heals() {
        return false;
    }

    // Player-overlap footprint. Defaults to the bounds rectangle; multi-part
    // entities (e.g. a segmented body) override to test every piece.
    public boolean hits(Rectangle other) {
        return bounds.overlaps(other);
    }

    public boolean glitches() {
        // TODO Auto-generated method stub
        return false;
        //throw new UnsupportedOperationException("Unimplemented method 'glitches'");
    }
}
