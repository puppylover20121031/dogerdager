package com.unpuppyable.dogerdager;

/** The full achievement set. Unlock state is persisted by {@link Progress}. */
public enum Achievement {
    FLOOR_5("Foothold", "Reach floor 5"),
    FLOOR_10("Ascendant", "Reach floor 10"),
    POTIONER("Lucky", "Grab a healing potion"),
    FIRST_DEATH("First Death", "Die for the first time"),
    CLEAR_NORMAL("Survivor", "Clear Normal"),
    CLEAR_HARD("Hardened", "Clear Hard"),
    CLEAR_HARDCORE("Untouchable", "Clear Hardcore"),
    CLEAR_ALL("God Climer", "Clear ALL of them"),
    NINJA("Like a ninja", "Clear ANY Difficulty and using the STRAFING feature 120 times"),
    GET_GLITCHED("Well thats weird..", "get a glitch powerup."),
    USE_GLITCH("Ninja Glitcher", "Use the glitch to \"Glitch\" like a Ninja Glitcher.");
    

    public final String title;
    public final String desc;

    Achievement(String title, String desc) {
        this.title = title;
        this.desc = desc;
    }
}
