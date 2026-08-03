package com.unpuppyable.dogerdager;

public enum Difficulty {
    EASY(220f, 6, 12, -1, 5, 3, 4f, 0.2f, 1.3f, 0.22f, 0.45f, 0.65f, 0.10f),
    NORMAL(300f, 5, 15, 0, 5, 3, 4f, 0.2f, 1.3f, 0.22f, 0.45f, 0.65f, 0.10f),
    HARD(470f, 3, 20, 2, 4, 2, 3.4f, 0.25f, 1.0f, 0.28f, 0.50f, 0.75f, 0.08f),
    HARDCORE(560f, 2, 22, 2, 3, 2, 3.0f, 0.30f, 0.9f, 0.35f, 0.55f, 0.80f, 0.06f),
    CUSTOM(220f, 6, 12, -1, 5, 3, 4f, 0.2f, 1.3f, 0.22f, 0.45f, 0.65f, 0.10f);

    public float enemySpeed;
    public int maxHealth;   // hearts
    public int winFloor;
    public int hitBonus;    // added to each hit's heart cost (clamped to >= 1)
    public int centipedeFloor;
    public int smartFloor;
    public float spawnStart;
    public float spawnRamp;
    public float spawnMin;
    public float centipedeChance;
    public float fastChance;
    public float smartChance;
    public float potionChance;
    public SpawnSchedule spawnSchedule;

    Difficulty(float enemySpeed, int maxHealth, int winFloor, int hitBonus, int centipedeFloor, int smartFloor,
               float spawnStart, float spawnRamp, float spawnMin,
               float centipedeChance, float fastChance, float smartChance, float potionChance) {
        this.enemySpeed = enemySpeed;
        this.maxHealth = maxHealth;
        this.winFloor = winFloor;
        this.hitBonus = hitBonus;
        this.centipedeFloor = centipedeFloor;
        this.smartFloor = smartFloor;
        this.spawnStart = spawnStart;
        this.spawnRamp = spawnRamp;
        this.spawnMin = spawnMin;
        this.centipedeChance = centipedeChance;
        this.fastChance = fastChance;
        this.smartChance = smartChance;
        this.potionChance = potionChance;
        this.spawnSchedule = defaultSchedule();
    }

    private static SpawnSchedule defaultSchedule() {
        SpawnSchedule schedule = new SpawnSchedule();
        schedule.rules.add(new SpawnRule(1, 16, SpawnRule.Type.NORMAL));
        schedule.rules.add(new SpawnRule(1, 8, SpawnRule.Type.FAST));
        schedule.rules.add(new SpawnRule(3, 6, SpawnRule.Type.SMART));
        schedule.rules.add(new SpawnRule(5, 4, SpawnRule.Type.CENTIPEDE));
        schedule.rules.add(new SpawnRule(1, 3, SpawnRule.Type.POTION));
        schedule.rules.add(new SpawnRule(10, 2, SpawnRule.Type.Powerup1));
        return schedule;
    }

    public void setSpawnSchedule(SpawnSchedule schedule) {
        if (schedule != null) {
            this.spawnSchedule = schedule;
        }
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

    public void setHitBonus(int bonus) {
        hitBonus = bonus;
    }

    public void setCentipedeFloor(int floor) {
        centipedeFloor = floor;
    }

    public void setSmartFloor(int floor) {
        smartFloor = floor;
    }

    public void setSpawnStart(float value) {
        spawnStart = value;
    }

    public void setSpawnRamp(float value) {
        spawnRamp = value;
    }

    public void setSpawnMin(float value) {
        spawnMin = value;
    }

    public void setCentipedeChance(float value) {
        centipedeChance = value;
    }

    public void setFastChance(float value) {
        fastChance = value;
    }

    public void setSmartChance(float value) {
        smartChance = value;
    }

    public void setPotionChance(float value) {
        potionChance = value;
    }

    public int getMaxHealth(int health) {
        return maxHealth;
    }

    

    public boolean instantKill() {
        return this == HARDCORE;
    }
}
