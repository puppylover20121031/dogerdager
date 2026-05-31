package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

public final class Icons implements Disposable {

    public static final String PLAY = "";
    public static final String GAMEPAD = "";
    public static final String GEAR = "";
    public static final String INFO = "";
    public static final String TROPHY = "";

    private final BitmapFont font;

    public Icons(int size) {
        var generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/fa-solid-900.ttf"));
        var param = new FreeTypeFontParameter();
        param.size = size;
        param.characters = PLAY + GAMEPAD + GEAR + INFO + TROPHY;
        font = generator.generateFont(param);
        generator.dispose();
    }

    public BitmapFont font() {
        return font;
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}
