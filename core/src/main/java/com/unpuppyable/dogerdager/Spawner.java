package com.unpuppyable.dogerdager;

import com.unpuppyable.dogerdager.entity.Boss;
import com.unpuppyable.dogerdager.entity.Enemy;

public final class Spawner {

    private static final float LEVEL_TIME = 200f / 60f;

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
            case 5, 47, 92, 132 -> screen.spawn(Enemy.Kind.FAST);
            case 6, 8, 10 -> screen.spawn(tough ? Enemy.Kind.SMART : Enemy.Kind.NORMAL);
            case 25, 52, 120 -> screen.spawn(Enemy.Kind.SMART);
            case 12 -> screen.spawnBoss(Boss.Kind.ONE);
            case 35, 80 -> screen.spawnBoss(Boss.Kind.TWO);
            case 100 -> screen.spawnBoss(Boss.Kind.THREE);
            default -> {
                if (level > 25 && level % 10 == 0) {
                    screen.spawn(tough ? Enemy.Kind.SMART : Enemy.Kind.NORMAL);
                }
            }
        }

        if (level % 4 == 0) {
            screen.spawnPotion();
        }

        if (level >= difficulty.winLevel) {
            screen.win();
        }
    }
}
