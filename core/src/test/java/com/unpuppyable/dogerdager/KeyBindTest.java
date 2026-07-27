package com.unpuppyable.dogerdager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class KeyBindTest {

    @BeforeAll
    static void initGdx() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new com.badlogic.gdx.ApplicationAdapter() {}, config);
    }

    @Test
    void storesAndReadsBindings() {
        KeyBind bind = new KeyBind();
        bind.set(KeyBind.Action.MOVE_UP, 100);
        assertEquals(100, bind.get(KeyBind.Action.MOVE_UP));
    }
}
