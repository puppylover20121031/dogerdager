package com.unpuppyable.dogerdager;

public final class SpawnRule {

    public enum Type {
        NORMAL,
        FAST,
        SMART,
        CENTIPEDE,
        POTION
    }

    public int minFloor;
    public int weight;
    public Type type;

    public SpawnRule(int minFloor, int weight, Type type) {
        this.minFloor = minFloor;
        this.weight = weight;
        this.type = type;
    }

    public String displayName() {
        return "F" + minFloor + " " + type.name() + " " + weight;
    }
}
