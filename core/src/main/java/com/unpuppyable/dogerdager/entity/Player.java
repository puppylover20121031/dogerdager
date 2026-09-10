package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector3;
import com.unpuppyable.dogerdager.*;
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

    private float shootCooldown = 0;
    private boolean playerShootingEnabled = false;
    private static final float PLAYER_SHOOT_SPEED = 380f;
    private static final float PLAYER_SHOOT_COOLDOWN = 0.18f;

    public static final float SIZE = 16;
    private static float SPEED = 300;
    private static float STRAFE_DIST = 110;
    private static final float STRAFE_INVULN = 0.2f;
    private static final float STRAFE_CD = 1.2f;

    public String username;
    private final float maxX;
    private final float maxY;
    private final KeyBind keyBind = new KeyBind();
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

    private float timeDelta = 0;

    private boolean shielded;
    public boolean invulnerable;
    private boolean staminaLocked;
    private float invulnerableFor = 0;
    public float stamina;

    public float health;
    public float maxHealth;
    private final boolean isYou;
    private final HashSet<String> multiplayerKeysDown = new HashSet<String>();

    private final PostProcessor post;
    private final Progress progress;
    private final Preferences prefs = Gdx.app.getPreferences("doger-dager");

    public Player(float worldW, float playTop, PostProcessor post, Progress progress, String username, Difficulty difficulty, boolean isHost) {
        super((worldW - SIZE) / 2f, (playTop - SIZE) / 2f, SIZE);
        maxX = worldW - SIZE;
        maxY = playTop - SIZE;
        this.post = post;
        this.progress = progress;
        this.maxHealth = difficulty.maxHealth;
        this.health = difficulty.maxHealth;
        this.stamina = MAX_STAMINA;
        this.isYou = isHost;
        this.username = username;
        this.name = "Player";
        if (progress.achieved(Achievement.CLEAR_NORMAL) || prefs.getBoolean("Easy_unlock", false))
            playerShootingEnabled = true;
        if (difficulty == Difficulty.HARD || difficulty == Difficulty.HARDCORE) playerShootingEnabled = true;
    }

    @Override
    public void update(float delta) {
        if (dead) return;
        anim += delta;
        if (invulnerableFor > 0) {
            invulnerableFor -= delta;
        } else {
            setInvulnerable(false);
        }

        if (strafeInvuln > 0) strafeInvuln -= delta;
        if (strafeCd > 0) strafeCd -= delta;

        if(this.post.getGlitch() && !DogerDager.getMultiplayer()) {
            STRAFE_DIST = 320;
            SPEED = 400;
            progress.unlock(Achievement.GET_GLITCHED);
        }

        if (stun > 0) {
            stun -= delta;
            bounds.x = MathUtils.clamp(bounds.x + kbX * delta, 0, maxX);
            bounds.y = MathUtils.clamp(bounds.y + kbY * delta, 0, maxY);
            return;
        }

        shield(delta);

        if (shootCooldown > 0)
            shootCooldown -= delta;

        if (isYou) {
            float vx = 0, vy = 0;
            if (keyBind.isPressed(KeyBind.Action.MOVE_LEFT)) vx -= SPEED;
            if (keyBind.isPressed(KeyBind.Action.MOVE_RIGHT)) vx += SPEED;
            if (keyBind.isPressed(KeyBind.Action.MOVE_UP)) vy += SPEED;
            if (keyBind.isPressed(KeyBind.Action.MOVE_DOWN)) vy -= SPEED;
            vx = MathUtils.clamp(vx + Pad.moveX() * SPEED, -SPEED, SPEED);
            vy = MathUtils.clamp(vy + Pad.moveY() * SPEED, -SPEED, SPEED);
            if (vx != 0 || vy != 0) {
                lastDx = vx / SPEED;
                lastDy = vy / SPEED;
            }
            bounds.x = MathUtils.clamp(bounds.x + vx * delta, 0, maxX);
            bounds.y = MathUtils.clamp(bounds.y + vy * delta, 0, maxY);
            if ((keyBind.isJustPressed(KeyBind.Action.STRAFE) || Pad.justA()))
                if (strafeCd <= 0) strafe();
            if (keyBind.isJustPressed(KeyBind.Action.SHOOT) || Pad.justB()) {
                Vector3 aim = localAim();
                boolean isPressed = Gdx.input.isTouched() || Gdx.input.isButtonPressed(Input.Buttons.LEFT);
                shoot(
                        (int) aim.x,
                        (int) aim.y,
                        isPressed
                );
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
            if (multiplayerKeysDown.contains("TAB"))
                strafe();
        }
        if (shielded) stamina = Math.max(0, stamina - DRAIN * delta);
        else if (stamina < MAX_STAMINA) stamina = Math.min(MAX_STAMINA, stamina + REGEN * delta);
    }

    private void strafe() {
        if (strafeCd > 0) return;
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

    private Vector3 localAim() {
        Vector3 aim = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        if (DogerDager.getGameInstance().getScreen() instanceof PlayScreen screen)
            screen.viewport().unproject(aim);
        return aim;
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

    public void healFull() {
        if (this.dead()) return;
        health = maxHealth;
    }

    public float staminaFraction() {
        return stamina / MAX_STAMINA;
    }


    public void refillStamina() {
        stamina = MAX_STAMINA;
        staminaLocked = false;
    }

    private void shield(float delta) {
        //if (difficulty == Difficulty.HARDCORE || difficulty == Difficulty.HARD) stamina = 1200;
        if (!isYou) {
            if (multiplayerKeysDown.contains("SHIFT_LEFT")) {
                if (stamina <=0) staminaLocked = true;
                else if (stamina >= 300) staminaLocked = false;
                shielded = !staminaLocked && stamina > 0;
            } else {
                shielded = false;
            }
        } else {
            if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)||Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) {
                if (stamina <=0) staminaLocked = true;
                else if (stamina >= 300) staminaLocked = false;
                shielded = !staminaLocked && stamina > 0;
            } else {
                shielded = false;
            }
        }
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
        shapes.rect(bounds.x + 2, bounds.y + 2, SIZE - 4, (SIZE - 4) * staminaFraction());
    }

    //actions

    public void shoot(int worldX, int worldY, boolean pressed) {
        if (dead()) return;
        if (!playerShootingEnabled || shootCooldown > 0) return;

        Vector3 aim = new Vector3(worldX, worldY, 0);
        float px = bounds().x + SIZE / 2f;
        float py = bounds().y + SIZE / 2f;
        float dx = 0f;
        float dy = 0f;
        if (pressed) {
            dx = aim.x - px;
            dy = aim.y - py;
        }
        if (Math.abs(dx) < 0.1f && Math.abs(dy) < 0.1f) {
            dx = lastDx;
            dy = lastDy;
        }
        if (Math.abs(dx) < 0.1f && Math.abs(dy) < 0.1f) {
            dx = 1f;
            dy = 0f;
        }
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        float vx = dx / len * PLAYER_SHOOT_SPEED;
        float vy = dy / len * PLAYER_SHOOT_SPEED;
        if (DogerDager.getGameInstance().getScreen() instanceof PlayScreen screen) {
            screen.add(new PlayerArrow(px - PlayerArrow.SIZE / 2f, py - PlayerArrow.SIZE / 2f, vx, vy, maxX + SIZE, maxY + SIZE, this));
        }
        shootCooldown = PLAYER_SHOOT_COOLDOWN;
    }



    //inputs
    public void keyDown(String key) {
        if (!multiplayerKeysDown.contains(key)) multiplayerKeysDown.add(key);
    }
    public void keyUp(String key) {
        multiplayerKeysDown.remove(key);
    }

}
