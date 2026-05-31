package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.unpuppyable.dogerdager.PlayScreen;

public final class Boss extends Entity {

    public enum Kind { ONE, TWO, THREE, ARM }

    public static final float SIZE = 96;
    private static final float DESCEND = 180;
    private static final float PATROL = 150;

    private final Kind kind;
    private final PlayScreen screen;
    private final Player target;
    private final float worldW;
    private final float restY;

    private float vx = PATROL;
    private boolean settled;
    private float atkTimer;
    private float burstTimer;
    private float minionTimer = 4f;
    private float phaseTimer;
    private int phase = 1;
    private boolean armsSpawned;

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
    public void update(float delta) {
        if (!settled) {
            bounds.y -= DESCEND * delta;
            if (bounds.y <= restY) {
                bounds.y = restY;
                settled = true;
            }
            return;
        }

        if (kind == Kind.ARM) return;

        bounds.x += vx * delta;
        if (bounds.x <= 0 || bounds.x >= worldW - SIZE) vx = -vx;

        if (kind == Kind.THREE) {
            updateBoss3(delta);
        } else {
            atkTimer -= delta;
            if (atkTimer <= 0) {
                fireBasic();
                atkTimer = kind == Kind.ONE ? 1.5f : 1.6f;
            }
            if (kind == Kind.TWO) {
                minionTimer -= delta;
                if (minionTimer <= 0) {
                    screen.spawn(Enemy.Kind.SMART);
                    minionTimer = 4f;
                }
            }
        }
    }

    private void fireBasic() {
        if (kind == Kind.TWO) {
            screen.addBullet(new Bullet(Bullet.Kind.HOMING, bounds.x + SIZE / 2, bounds.y, worldW, target));
        } else {
            screen.addBullet(new Bullet(Bullet.Kind.FALLING, bounds.x + 20, bounds.y, worldW, target));
            screen.addBullet(new Bullet(Bullet.Kind.FALLING, bounds.x + SIZE - 32, bounds.y, worldW, target));
        }
    }

    private void updateBoss3(float delta) {
        if (!armsSpawned) {
            screen.addBoss(new Boss(Kind.ARM, clampArm(bounds.x - 110), restY, worldW, screen, target));
            screen.addBoss(new Boss(Kind.ARM, clampArm(bounds.x + 110), restY, worldW, screen, target));
            armsSpawned = true;
        }

        phaseTimer += delta;
        int prev = phase;
        phase = phaseTimer < 20 ? 1 : phaseTimer < 40 ? 2 : 3;
        if (phase != prev) {
            for (int i = 0; i < 3; i++) screen.spawn(Enemy.Kind.NORMAL);
        }

        atkTimer -= delta;
        burstTimer -= delta;
        switch (phase) {
            case 1 -> {
                if (atkTimer <= 0) { twin(); atkTimer = 0.8f; }
            }
            case 2 -> {
                if (atkTimer <= 0) { twin(); atkTimer = 0.5f; }
                if (burstTimer <= 0) { burst(12); burstTimer = 2.5f; }
            }
            default -> {
                if (atkTimer <= 0) { salvo(); atkTimer = 0.25f; }
                if (burstTimer <= 0) { burst(20); burstTimer = 1.6f; }
            }
        }
    }

    private void twin() {
        screen.addBullet(new Bullet(Bullet.Kind.FALLING, bounds.x + 12, bounds.y, worldW, target));
        screen.addBullet(new Bullet(Bullet.Kind.FALLING, bounds.x + SIZE - 24, bounds.y, worldW, target));
    }

    private void salvo() {
        for (int i = 0; i < 3; i++) {
            screen.addBullet(new Bullet(Bullet.Kind.FALLING, bounds.x + 10 + i * 28, bounds.y, worldW, target));
        }
    }

    private void burst(int count) {
        for (int i = 0; i < count; i++) {
            screen.addBullet(new Bullet(Bullet.Kind.FALLING, bounds.x + MathUtils.random(SIZE), bounds.y, worldW, target));
        }
    }

    private float clampArm(float x) {
        return MathUtils.clamp(x, 0, worldW - SIZE);
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        shapes.setColor(switch (kind) {
            case ARM -> Color.MAROON;
            case THREE -> phase == 1 ? Color.FIREBRICK : phase == 2 ? Color.ORANGE : Color.SCARLET;
            default -> Color.RED;
        });
        shapes.rect(bounds.x, bounds.y, SIZE, SIZE);
    }
}
