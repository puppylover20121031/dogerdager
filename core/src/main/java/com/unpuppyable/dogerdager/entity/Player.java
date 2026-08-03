package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.Gdx;
import com.unpuppyable.dogerdager.Difficulty;
import com.unpuppyable.dogerdager.DogerDager;
import com.unpuppyable.dogerdager.Pad;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.HashSet;

public final class Player extends Entity {

    private static final float MAX_STAMINA = 1200;
    private static final float DRAIN = 300;
    private static final float REGEN = 60;
    private static final float HIT_GRACE = 0.7f;
    private static final float BAND = 72;

    public static final float SIZE = 16;
    private static float SPEED = 300;
    private static final float STRAFE_DIST = 110;
    private static final float STRAFE_INVULN = 0.2f;
    private static final float STRAFE_CD = 1.2f;

    public final String username;
    private final float maxX;
    private final float maxY;
    private boolean shielded;
    public boolean invulnerable;
    private float anim;
    private float lastDx = 1;
    private float lastDy = 0;
    public float strafeInvuln;
    private float strafeCd;
    private float fromX;
    private float fromY;
    public float stun;
    private float kbX;
    private float kbY;

    private float invulnerableFor = 0;
    public float stamina;
    public float health;
    public float maxHealth;
    private boolean isYou;
    private HashSet<String> multiplayerKeysDown = new HashSet<String>();

    public Player(float worldW, float playTop, String uname, Difficulty difficulty, boolean isHost) {
        super((worldW - SIZE) / 2f, (playTop - SIZE) / 2f, SIZE);
        maxX = worldW - SIZE;
        maxY = playTop - SIZE;
        maxHealth = difficulty.maxHealth;
        health = difficulty.maxHealth;
        isYou = isHost;
        username = uname;
        this.name = "Player";
    }

    @Override
    public void update(float delta) {
        anim += delta;
        if (invulnerableFor > 0) {
            invulnerableFor -= delta;
        } else {
            setInvulnerable(false);
        }
        if (strafeInvuln > 0) strafeInvuln -= delta;
        if (strafeCd > 0) strafeCd -= delta;

        if (stun > 0) {
            stun -= delta;
            bounds.x = MathUtils.clamp(bounds.x + kbX * delta, 0, maxX);
            bounds.y = MathUtils.clamp(bounds.y + kbY * delta, 0, maxY);
            return;
        }

        if (isYou) {
            float vx = 0, vy = 0;
            if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) vx -= SPEED;
            if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) vx += SPEED;
            if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) vy += SPEED;
            if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) vy -= SPEED;
            vx = MathUtils.clamp(vx + Pad.moveX() * SPEED, -SPEED, SPEED);
            vy = MathUtils.clamp(vy + Pad.moveY() * SPEED, -SPEED, SPEED);
            if (vx != 0 || vy != 0) {
                lastDx = vx / SPEED;
                lastDy = vy / SPEED;
            }
            bounds.x = MathUtils.clamp(bounds.x + vx * delta, 0, maxX);
            bounds.y = MathUtils.clamp(bounds.y + vy * delta, 0, maxY);
            if ((Gdx.input.isKeyJustPressed(Keys.TAB) || Pad.justA())) {
                if (strafeCd > 0) return;
                strafe();
            }
            //multiplayer:
        } else {
            float vx = 0, vy = 0;
            if (multiplayerKeysDown.isEmpty()) return;
            if (multiplayerKeysDown.contains("W")||multiplayerKeysDown.contains("UP")) vy += SPEED;
            if (multiplayerKeysDown.contains("S")||multiplayerKeysDown.contains("DOWN")) vy -= SPEED;
            if (multiplayerKeysDown.contains("A")||multiplayerKeysDown.contains("LEFT")) vx -= SPEED;
            if (multiplayerKeysDown.contains("D")||multiplayerKeysDown.contains("RIGHT")) vx += SPEED;
            vx = MathUtils.clamp(vx + Pad.moveX() * SPEED, -SPEED, SPEED);
            vy = MathUtils.clamp(vy + Pad.moveY() * SPEED, -SPEED, SPEED);
            if (vx != 0 || vy != 0) {
                lastDx = vx / SPEED;
                lastDy = vy / SPEED;
            }
            bounds.x = MathUtils.clamp(bounds.x + vx * delta, 0, maxX);
            bounds.y = MathUtils.clamp(bounds.y + vy * delta, 0, maxY);
            if (multiplayerKeysDown.contains("TAB")) {
                if (strafeCd > 0) return;
                strafe();
            }
        }
    }

    private void strafe() {
        fromX = bounds.x;
        fromY = bounds.y;
        float len = (float) Math.sqrt(lastDx * lastDx + lastDy * lastDy);
        bounds.x = MathUtils.clamp(bounds.x + lastDx / len * STRAFE_DIST, 0, maxX);
        bounds.y = MathUtils.clamp(bounds.y + lastDy / len * STRAFE_DIST, 0, maxY);
        strafeInvuln = STRAFE_INVULN;
        strafeCd = STRAFE_CD;
    }

    public boolean strafing() {
        return strafeInvuln > 0;
    }

    public void setStamina(float fraction) {
        this.stamina = fraction;
    }

    public float aimX() {
        return lastDx;
    }

    public float aimY() {
        return lastDy;
    }

    public void knockback(float worldW, float playTop) {
        float dl = bounds.x;
        float dr = worldW - SIZE - bounds.x;
        float db = bounds.y;
        float dt = playTop - SIZE - bounds.y;
        float min = Math.min(Math.min(dl, dr), Math.min(db, dt));
        kbX = 0;
        kbY = 0;
        if (min == dl) kbX = -700;
        else if (min == dr) kbX = 700;
        else if (min == db) kbY = -700;
        else kbY = 700;
        stun = 1.5f;
    }

    public void setShielded(boolean shielded) {
        this.shielded = shielded;
    }

    public Boolean getShielded() { return this.shielded; }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        if (strafeInvuln > 0) {
            shapes.setColor(0.4f, 0.7f, 1f, 1f);
            shapes.rect(fromX, fromY, bounds.width, bounds.height);
        }
        if (invulnerable && (int) (anim * 10) % 2 == 0) return;

        Color body = stun > 0 ? Color.GRAY : shielded ? Color.SKY : Color.CYAN;
        shapes.setColor(body);
        shapes.rect(bounds.x, bounds.y, SIZE, SIZE);
        shapes.setColor(Color.BLACK);
        shapes.rect(bounds.x + 2, bounds.y + 2, SIZE - 4, SIZE - 4);
        shapes.setColor(body);
        shapes.rect(bounds.x + 2, bounds.y + 2, SIZE - 4, (SIZE - 4) * stamina);
    }

    //multiplayer:

    //player entity health update
    public boolean damage(int amount) {
        if (this.dead) return false;
        if (shielded || invulnerableFor > 0) return false;
        health = Math.max(0, health - amount);
        invulnerableFor = HIT_GRACE;
        setInvulnerable(true);
        if (health <= 0) this.kill();
        return true;
    }
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }

    //inputs
    public void keyDown(String key) {
        if (!multiplayerKeysDown.contains(key)) multiplayerKeysDown.add(key);
    }
    public void keyUp(String key) {
        multiplayerKeysDown.remove(key);
    }

}
