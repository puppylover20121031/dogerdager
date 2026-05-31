package com.unpuppyable.dogerdager;

public enum Difficulty {
    EASY(300f, 300, 80),
    NORMAL(300f, 300, 200),
    HARD(600f, 300, 300),
    HARDCORE(600f, 10, 300);

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
