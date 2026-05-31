package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.unpuppyable.dogerdager.PlayScreen;

public final class Boss extends Entity {

    public enum Kind {
        ONE, TWO, THREE, ARM
    }

    public static final float SIZE = 96;
    private static final float DESCEND = 180;
    private static final float PATROL = 150;
    private static final Color ONE_COL = new Color(1f, 0.48f, 0.2f, 1f);
    private static final Color TWO_COL = new Color(0.78f, 0.24f, 1f, 1f);

    private final Kind kind;
    private final PlayScreen screen;
    private final Player target;
    private final float worldW;
    private final float restY;

    private float vx = PATROL;
    private boolean settled;
    private float atkTimer;
    private boolean altAttack;
    private float minionTimer = 4f;
    private float fireTimer;
    private int rocketsInBurst;
    private boolean armsSpawned;
    private float phaseTimer;
    private int phase = 1;
    private float laserTimer = 4f;

    public Boss(Kind kind, float x, float restY, float worldW, PlayScreen screen, Player target) {
        super(x, restY + 220, SIZE);
        this.kind = kind;
        this.restY = restY;
        this.worldW = worldW;
        this.screen = screen;
        this.target = target;
    }

    public boolean arm() {
        return kind == Kind.ARM;
    }

    @Override
    public int contactDamage() {
        return 2;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public void update(float delta) {
        if (!settled) {
            bounds.y -= DESCEND * delta;
            if (bounds.y <= restY) {
                bounds.y = restY;
                settled = true;
            }
            return;
        }

        if (kind == Kind.ARM)
            return;

        bounds.x += vx * delta;
        if (bounds.x <= 0 || bounds.x >= worldW - SIZE)
            vx = -vx;

        if (kind == Kind.THREE) {
            updateBoss3(delta);
        } else if (kind == Kind.ONE) {
            updateBoss1(delta);
        } else {
            updateBoss2(delta);
        }
    }

    // ONE: telegraphed fans aimed at the player; widens and speeds up when enraged.
    private void updateBoss1(float delta) {
        phaseTimer += delta;
        boolean rage = phaseTimer > 10f;
        atkTimer -= delta;
        if (atkTimer <= 0) {
            fireFan(rage ? 7 : 5, rage ? 0.7f : 0.5f, 150f);
            atkTimer = rage ? 1.3f : 1.8f;
        }
    }

    // TWO: alternates a homing volley and a rotating ring, trickles minions, escalates.
    private void updateBoss2(float delta) {
        phaseTimer += delta;
        boolean rage = phaseTimer > 12f;
        atkTimer -= delta;
        if (atkTimer <= 0) {
            if (altAttack) {
                fireRing(rage ? 16 : 12, 110f);
            } else {
                for (int i = 0; i < 3; i++) {
                    screen.add(new Bullet(Bullet.Kind.HOMING, bounds.x + SIZE / 2, bounds.y, worldW, target));
                }
            }
            altAttack = !altAttack;
            atkTimer = rage ? 1.4f : 1.9f;
        }
        minionTimer -= delta;
        if (minionTimer <= 0) {
            screen.spawn(Enemy.Kind.SMART);
            minionTimer = 5f;
        }
    }

    private void fireFan(int n, float spread, float speed) {
        float cx = bounds.x + SIZE / 2f;
        float cy = bounds.y;
        float aim = MathUtils.atan2(target.bounds().y - cy, target.bounds().x - cx);
        for (int i = 0; i < n; i++) {
            float t = n == 1 ? 0.5f : i / (float) (n - 1);
            float a = aim - spread / 2f + spread * t;
            screen.add(new Bullet(cx, cy, MathUtils.cos(a) * speed, MathUtils.sin(a) * speed, worldW));
        }
    }

    private void fireRing(int n, float speed) {
        float cx = bounds.x + SIZE / 2f;
        float cy = bounds.y + SIZE / 2f;
        for (int i = 0; i < n; i++) {
            float a = i * (MathUtils.PI2 / n) + phaseTimer;
            screen.add(new Bullet(cx, cy, MathUtils.cos(a) * speed, MathUtils.sin(a) * speed, worldW));
        }
    }

    private void updateBoss3(float delta) {
        if (!armsSpawned) {
            screen.add(new Boss(Kind.ARM, clampArm(bounds.x - 110), restY, worldW, screen, target));
            screen.add(new Boss(Kind.ARM, clampArm(bounds.x + 110), restY, worldW, screen, target));
            armsSpawned = true;
        }

        phaseTimer += delta;
        phase = Math.min(4, 1 + (int) (phaseTimer / 12f));

        fireTimer -= delta;
        if (fireTimer <= 0) {
            screen.add(new Bullet(Bullet.Kind.ROCKET, bounds.x + SIZE / 2, bounds.y, worldW, target));
            rocketsInBurst++;
            int burst = phase >= 3 ? 6 : 4;
            if (rocketsInBurst >= burst) {
                rocketsInBurst = 0;
                fireTimer = phase >= 3 ? 2f : 3f;
            } else {
                fireTimer = phase >= 3 ? 0.22f : 0.4f;
            }
        }

        if (phase >= 2) {
            laserTimer -= delta;
            if (laserTimer <= 0) {
                screen.spawnLaserWall();
                laserTimer = phase >= 4 ? 3.5f : 5f;
            }
        }
    }

    private float clampArm(float x) {
        return MathUtils.clamp(x, 0, worldW - SIZE);
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        if (kind == Kind.THREE && settled && fireTimer < 0.3f) {
            float intensity = 1f - fireTimer / 0.3f;
            shapes.setColor(1f, 0.25f * intensity, 0.1f, 1f);
            shapes.rectLine(bounds.x + SIZE / 2f, bounds.y, target.bounds().x + 8f, target.bounds().y + 8f, 1.5f);
        }
        Color body = switch (kind) {
            case ARM -> Color.MAROON;
            case ONE -> ONE_COL;
            case TWO -> TWO_COL;
            case THREE -> phase <= 1 ? Color.FIREBRICK : phase == 2 ? Color.ORANGE : phase == 3 ? Color.SCARLET : Color.VIOLET;
        };
        if ((kind == Kind.ONE || kind == Kind.TWO) && settled && atkTimer < 0.25f) {
            body = body.cpy().lerp(Color.WHITE, 1f - atkTimer / 0.25f);
        }
        shapes.setColor(body);
        shapes.rect(bounds.x, bounds.y, SIZE, SIZE);
    }
}
