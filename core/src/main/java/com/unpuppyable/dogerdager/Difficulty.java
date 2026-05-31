package com.unpuppyable.dogerdager;

public enum Difficulty {
    EASY(220f, 300, 80, 0.5f),
    NORMAL(300f, 300, 150, 1.0f),
    HARD(430f, 300, 220, 1.5f),
    HARDCORE(520f, 300, 220, 2.0f);

    public final float enemySpeed;
    public final int maxHealth;
    public final int winLevel;
    public final float scoreMultiplier;

    Difficulty(float enemySpeed, int maxHealth, int winLevel, float scoreMultiplier) {
        this.enemySpeed = enemySpeed;
        this.maxHealth = maxHealth;
        this.winLevel = winLevel;
        this.scoreMultiplier = scoreMultiplier;
    }

    public boolean instantKill() {
        return this == HARDCORE;
    }
}
