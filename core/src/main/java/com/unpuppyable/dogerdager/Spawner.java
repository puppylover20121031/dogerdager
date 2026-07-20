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
            spawnTimer = Math.max(difficulty.spawnMin, difficulty.spawnStart - hud.floor() * difficulty.spawnRamp);
        }
    }

    private void spawnWave() {
        int floor = hud.floor();
        boolean tough = difficulty == Difficulty.HARD || difficulty == Difficulty.HARDCORE;
        SpawnRule rule = difficulty.spawnSchedule.choose(floor);
        if (rule != null) {
            switch (rule.type) {
                case CENTIPEDE:
                    screen.spawnCentipede();
                    break;
                case LongGuy:
                    screen.spawnCentipede();
                    break;
                case FAST:
                    screen.spawn(Enemy.Kind.FAST);
                    break;
                case SMART:
                    screen.spawn(tough ? Enemy.Kind.SMART : Enemy.Kind.NORMAL);
                    break;
                case POTION:
                    screen.spawnPotion();
                    break;
                case Powerup1:
                    screen.spawnPowerup1();
                    break;
                case NORMAL:
                default:
                    screen.spawn(Enemy.Kind.NORMAL);
                    break;
            }
        } else {
            screen.spawn(Enemy.Kind.NORMAL);
        }

        if (MathUtils.random() < difficulty.potionChance) {
            screen.spawnPotion();
        }
    }
}
