package com.unpuppyable.dogerdager;

public enum Difficulty {
    EASY(220f, 6, 12, -1),
    NORMAL(300f, 5, 15, 0),
    HARD(430f, 4, 18, 1),
    HARDCORE(520f, 3, 18, 0),
    CUSTOM(220f, 6, 12, -1);

    public float enemySpeed;
    public int maxHealth;   // hearts
    public int winFloor;
    public final int hitBonus;    // added to each hit's heart cost (clamped to >= 1)

    Difficulty(float enemySpeed, int maxHealth, int winFloor, int hitBonus) {
        this.enemySpeed = enemySpeed;
        this.maxHealth = maxHealth;
        this.winFloor = winFloor;
        this.hitBonus = hitBonus;
    }

    public void setEnemySpeed(float speed) {
        enemySpeed = speed;
    }

    public void setMaxHealth(int health) {
        maxHealth = health;
    }

    public void setWinFloor(int floor) {
        winFloor = floor;
    }

    public int getMaxHealth(int health) {
        return maxHealth;
    }

    

    public boolean instantKill() {
        return this == HARDCORE;
    }
}
