package com.unpuppyable.dogerdager;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

/**
 * Entry point of the game on the libGDX side. For now it just clears the
 * screen; gameplay (player, enemies, bosses, audio, menus) is ported on top of
 * this skeleton step by step, using the legacy AWT build only as a behaviour
 * reference.
 */
public class DogerDager extends ApplicationAdapter {

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
