package com.unpuppyable.dogerdager;

public enum Difficulty {
    EASY(220f, 300, 80),
    NORMAL(300f, 300, 150),
    HARD(430f, 300, 220),
    HARDCORE(520f, 300, 220);

    public final float enemySpeed;
    public final int maxHealth;
    public final int winLevel;

    Difficulty(float enemySpeed, int maxHealth, int winLevel) {
        this.enemySpeed = enemySpeed;
        this.maxHealth = maxHealth;
        this.winLevel = winLevel;
    }

    public boolean instantKill() {
        return this == HARDCORE;
    }
}
