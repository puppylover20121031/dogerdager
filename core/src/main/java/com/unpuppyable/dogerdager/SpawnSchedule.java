package com.unpuppyable.dogerdager;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.List;

public final class SpawnSchedule {

    public final List<SpawnRule> rules = new ArrayList<>();

    public SpawnSchedule() {
    }

    public SpawnSchedule(List<SpawnRule> initialRules) {
        if (initialRules != null) {
            rules.addAll(initialRules);
        }
    }

    public SpawnRule choose(int floor) {
        float totalWeight = 0f;
        for (SpawnRule rule : rules) {
            if (rule.minFloor <= floor && rule.weight > 0) {
                totalWeight += rule.weight;
            }
        }
        if (totalWeight <= 0f) {
            return null;
        }

        float selector = MathUtils.random() * totalWeight;
        for (SpawnRule rule : rules) {
            if (rule.minFloor <= floor && rule.weight > 0) {
                selector -= rule.weight;
                if (selector <= 0f) {
                    return rule;
                }
            }
        }

        for (int i = rules.size() - 1; i >= 0; i--) {
            SpawnRule last = rules.get(i);
            if (last.minFloor <= floor && last.weight > 0) {
                return last;
            }
        }
        return null;
    }
}
