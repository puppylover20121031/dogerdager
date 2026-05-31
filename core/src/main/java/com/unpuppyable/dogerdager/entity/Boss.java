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

    private final Kind kind;
    private final PlayScreen screen;
    private final Player target;
    private final float worldW;
    private final float restY;

    private float vx = PATROL;
    private boolean settled;
    private float atkTimer;
    private float minionTimer = 4f;
    private float fireTimer;
    private int rocketsInBurst;
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
    public int contactDamage() {
        return 25;
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
            screen.add(new Bullet(Bullet.Kind.HOMING, bounds.x + SIZE / 2, bounds.y, worldW, target));
        } else {
            screen.add(new Bullet(Bullet.Kind.FALLING, bounds.x + 20, bounds.y, worldW, target));
            screen.add(new Bullet(Bullet.Kind.FALLING, bounds.x + SIZE - 32, bounds.y, worldW, target));
        }
    }

    private void updateBoss3(float delta) {
        if (!armsSpawned) {
            screen.add(new Boss(Kind.ARM, clampArm(bounds.x - 110), restY, worldW, screen, target));
            screen.add(new Boss(Kind.ARM, clampArm(bounds.x + 110), restY, worldW, screen, target));
            armsSpawned = true;
        }

        fireTimer -= delta;
        if (fireTimer <= 0) {
            screen.add(new Bullet(Bullet.Kind.ROCKET, bounds.x + SIZE / 2, bounds.y, worldW, target));
            rocketsInBurst++;
            if (rocketsInBurst >= 4) {
                rocketsInBurst = 0;
                fireTimer = 3f;
            } else {
                fireTimer = 0.4f;
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
        shapes.setColor(switch (kind) {
            case ARM -> Color.MAROON;
            case THREE -> Color.SCARLET;
            default -> Color.RED;
        });
        shapes.rect(bounds.x, bounds.y, SIZE, SIZE);
    }
}
