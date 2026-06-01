package com.unpuppyable.dogerdager;

/** The full achievement set. Unlock state is persisted by {@link Progress}. */
public enum Achievement {
    FLOOR_5("Foothold", "Reach floor 5"),
    FLOOR_10("Ascendant", "Reach floor 10"),
    POTIONER("Lucky", "Grab a healing potion"),
    CLEAR_NORMAL("Survivor", "Clear Normal"),
    CLEAR_HARD("Hardened", "Clear Hard"),
    CLEAR_HARDCORE("Untouchable", "Clear Hardcore");

    public final String title;
    public final String desc;

    Achievement(String title, String desc) {
        this.title = title;
        this.desc = desc;
    }
}
