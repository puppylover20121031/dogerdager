package com.unpuppyable.dogerdager;

import com.unpuppyable.dogerdager.entity.Boss;
import com.unpuppyable.dogerdager.entity.Enemy;

public final class Spawner {

    private static final float LEVEL_TIME = 3.0f;

    private final Difficulty difficulty;
    private final Hud hud;
    private final PlayScreen screen;
    private float timer;

    public Spawner(Difficulty difficulty, Hud hud, PlayScreen screen) {
        this.difficulty = difficulty;
        this.hud = hud;
        this.screen = screen;
    }

    public void update(float delta) {
        timer += delta;
        while (timer >= LEVEL_TIME) {
            timer -= LEVEL_TIME;
            advance();
        }
    }

    private void advance() {
        int level = hud.nextLevel();
        boolean tough = difficulty == Difficulty.HARD || difficulty == Difficulty.HARDCORE;

        switch (level) {
            case 12 -> screen.spawnBoss(Boss.Kind.ONE);
            case 35, 80 -> screen.spawnBoss(Boss.Kind.TWO);
            case 100 -> screen.spawnBoss(Boss.Kind.THREE);
            default -> {
                if (!screen.bossActive()) spawnEnemy(level, tough);
            }
        }

        if (level % 5 == 0) {
            screen.spawnPotion();
        }


        if (level >= difficulty.winLevel) {
            screen.win();
        }
    }

    private void spawnEnemy(int level, boolean tough) {
        if (level == 5 || level == 47 || level == 92 || level == 132) {
            screen.spawn(Enemy.Kind.FAST);
        } else if (level == 6 || level == 8 || level == 10) {
            screen.spawn(tough ? Enemy.Kind.SMART : Enemy.Kind.NORMAL);
        } else if (level == 25 || level == 52 || level == 120) {
            screen.spawn(Enemy.Kind.SMART);
        } else if (level == 18 || level == 60 || level == 110) {
            screen.spawnCentipede();
        } else if (level > 25 && level % 10 == 0) {
            screen.spawn(tough ? Enemy.Kind.SMART : Enemy.Kind.NORMAL);
        }
    }
}
