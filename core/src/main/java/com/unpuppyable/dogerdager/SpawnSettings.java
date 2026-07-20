package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.ArrayList;
import java.util.List;

/** Persisted custom spawn settings for the CUSTOM difficulty. */
public final class SpawnSettings {

    private static final Preferences prefs = Gdx.app.getPreferences("doger-dager");

    public float customSpawnStart() {
        return prefs.getFloat("set.customSpawnStart", 4f);
    }

    public float customSpawnRamp() {
        return prefs.getFloat("set.customSpawnRamp", 0.2f);
    }

    public float customSpawnMin() {
        return prefs.getFloat("set.customSpawnMin", 1.3f);
    }

    public int customCentipedeFloor() {
        return prefs.getInteger("set.customCentipedeFloor", 5);
    }

    public float customCentipedeChance() {
        return prefs.getFloat("set.customCentipedeChance", 0.22f);
    }

    public int customSmartFloor() {
        return prefs.getInteger("set.customSmartFloor", 3);
    }

    public float customFastChance() {
        return prefs.getFloat("set.customFastChance", 0.45f);
    }

    public float customSmartChance() {
        return prefs.getFloat("set.customSmartChance", 0.65f);
    }

    public float customPotionChance() {
        return prefs.getFloat("set.customPotionChance", 0.10f);
    }

    public int customSpawnRuleCount() {
        return prefs.getInteger("set.spawnRule.count", 0);
    }

    public SpawnRule customSpawnRule(int index) {
        int minFloor = prefs.getInteger("set.spawnRule." + index + ".floor", 1);
        int weight = prefs.getInteger("set.spawnRule." + index + ".weight", 10);
        String typeName = prefs.getString("set.spawnRule." + index + ".type", SpawnRule.Type.NORMAL.name());
        SpawnRule.Type type = SpawnRule.Type.NORMAL;
        try {
            type = SpawnRule.Type.valueOf(typeName);
        } catch (IllegalArgumentException ignored) {
        }
        return new SpawnRule(minFloor, weight, type);
    }

    public SpawnSchedule customSpawnSchedule() {
        int count = customSpawnRuleCount();
        if (count <= 0) {
            return defaultSchedule();
        }

        SpawnSchedule schedule = new SpawnSchedule();
        for (int i = 0; i < count; i++) {
            schedule.rules.add(customSpawnRule(i));
        }
        return schedule;
    }

    public void saveSpawnSchedule(SpawnSchedule schedule) {
        clearSpawnRules();
        int count = schedule.rules.size();
        prefs.putInteger("set.spawnRule.count", count);
        for (int i = 0; i < count; i++) {
            SpawnRule rule = schedule.rules.get(i);
            prefs.putInteger("set.spawnRule." + i + ".floor", Math.max(1, rule.minFloor));
            prefs.putInteger("set.spawnRule." + i + ".weight", Math.max(1, rule.weight));
            prefs.putString("set.spawnRule." + i + ".type", rule.type.name());
        }
        prefs.flush();
    }

    public void clearSpawnRules() {
        int count = customSpawnRuleCount();
        for (int i = 0; i < count; i++) {
            prefs.remove("set.spawnRule." + i + ".floor");
            prefs.remove("set.spawnRule." + i + ".weight");
            prefs.remove("set.spawnRule." + i + ".type");
        }
        prefs.remove("set.spawnRule.count");
        prefs.flush();
    }

    public void setSpawnStart(float value) {
        prefs.putFloat("set.customSpawnStart", value);
        prefs.flush();
    }

    public void setSpawnRamp(float value) {
        prefs.putFloat("set.customSpawnRamp", value);
        prefs.flush();
    }

    public void setSpawnMin(float value) {
        prefs.putFloat("set.customSpawnMin", value);
        prefs.flush();
    }

    public void setCentipedeFloor(int value) {
        prefs.putInteger("set.customCentipedeFloor", value);
        prefs.flush();
    }

    public void setCentipedeChance(float value) {
        prefs.putFloat("set.customCentipedeChance", value);
        prefs.flush();
    }

    public void setSmartFloor(int value) {
        prefs.putInteger("set.customSmartFloor", value);
        prefs.flush();
    }

    public void setFastChance(float value) {
        prefs.putFloat("set.customFastChance", value);
        prefs.flush();
    }

    public void setSmartChance(float value) {
        prefs.putFloat("set.customSmartChance", value);
        prefs.flush();
    }

    public void setPotionChance(float value) {
        prefs.putFloat("set.customPotionChance", value);
        prefs.flush();
    }

    public SpawnSchedule defaultSchedule() {
        SpawnSchedule schedule = new SpawnSchedule();
        schedule.rules.add(new SpawnRule(1, 50, SpawnRule.Type.NORMAL));
        schedule.rules.add(new SpawnRule(1, 30, SpawnRule.Type.FAST));
        schedule.rules.add(new SpawnRule(3, 25, SpawnRule.Type.SMART));
        schedule.rules.add(new SpawnRule(5, 15, SpawnRule.Type.CENTIPEDE));
        schedule.rules.add(new SpawnRule(1, 10, SpawnRule.Type.POTION));
        schedule.rules.add(new SpawnRule(1, 6, SpawnRule.Type.Powerup1));
        return schedule;
    }

    public void apply() {
        Difficulty.CUSTOM.setSpawnStart(customSpawnStart());
        Difficulty.CUSTOM.setSpawnRamp(customSpawnRamp());
        Difficulty.CUSTOM.setSpawnMin(customSpawnMin());
        Difficulty.CUSTOM.setCentipedeFloor(customCentipedeFloor());
        Difficulty.CUSTOM.setCentipedeChance(customCentipedeChance());
        Difficulty.CUSTOM.setSmartFloor(customSmartFloor());
        Difficulty.CUSTOM.setFastChance(customFastChance());
        Difficulty.CUSTOM.setSmartChance(customSmartChance());
        Difficulty.CUSTOM.setPotionChance(customPotionChance());
        Difficulty.CUSTOM.setSpawnSchedule(customSpawnSchedule());
    }
}
