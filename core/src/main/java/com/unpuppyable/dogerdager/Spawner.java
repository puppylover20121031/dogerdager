package com.unpuppyable.dogerdager;

import com.badlogic.gdx.math.MathUtils;
import com.unpuppyable.dogerdager.entity.Enemy;

// Floor director: each floor is a fixed stretch of time. While a floor runs it
// trickles obstructing enemies (denser deeper down); when the clock runs out the
// screen advances a floor (heal + clear). No level counter -- floors/time/HP.
public final class Spawner {

    private static final float FLOOR_TIME = 30f;

    private final Difficulty difficulty;
    private final Hud hud;
    private final PlayScreen screen;
    private float floorTimer;
    private float spawnTimer = 2f;

    public Spawner(Difficulty difficulty, Hud hud, PlayScreen screen) {
        this.difficulty = difficulty;
        this.hud = hud;
        this.screen = screen;
    }

    public void update(float delta) {
        floorTimer += delta;
        hud.setFloorProgress(floorTimer / FLOOR_TIME);
        if (floorTimer >= FLOOR_TIME) {
            floorTimer = 0;
            screen.nextFloor();
        }

        if (screen.bossActive()) return;
        spawnTimer -= delta;
        if (spawnTimer <= 0) {
            spawnWave();
            spawnTimer = Math.max(1.3f, 4f - hud.floor() * 0.2f);
        }
    }

    private void spawnWave() {
        int floor = hud.floor();
        boolean tough = difficulty == Difficulty.HARD || difficulty == Difficulty.HARDCORE;
        float r = MathUtils.random();
        if (floor >= 5 && r < 0.22f) {
            screen.spawnCentipede();
        } else if (r < 0.45f) {
            screen.spawn(Enemy.Kind.FAST);
        } else if (floor >= 3 && r < 0.65f) {
            screen.spawn(tough ? Enemy.Kind.SMART : Enemy.Kind.NORMAL);
        } else {
            screen.spawn(Enemy.Kind.NORMAL);
        }
        if (MathUtils.random() < 0.10f) {
            screen.spawnPotion();
        }
    }
}
