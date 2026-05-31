package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

public final class MenuScreen extends ScreenAdapter {

    private static final Difficulty[] CHOICES = Difficulty.values();

    private final DogerDager game;
    private final Stage stage = new Stage(new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H));
    private final Progress progress = new Progress();
    private final VisTextButton[] buttons = new VisTextButton[CHOICES.length];

    private int index = Difficulty.NORMAL.ordinal();
    private boolean switching;

    public MenuScreen(DogerDager game) {
        this.game = game;
        build();
    }

    private void build() {
        var root = new VisTable();
        root.setFillParent(true);

        var title = new VisLabel("DOGER DAGER");
        title.setFontScale(2f);
        root.add(title).padBottom(28).row();

        for (int i = 0; i < CHOICES.length; i++) {
            var choice = CHOICES[i];
            var button = new VisTextButton(choice.name());
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    start(choice);
                }
            });
            buttons[i] = button;
            root.add(button).width(220).height(34).pad(3).row();
        }

        root.add(new VisLabel("Best  " + progress.bestOverall())).padTop(20).row();

        var hint = new VisTable();
        hint.add(new Label(Icons.GAMEPAD, new Label.LabelStyle(game.icons().font(), Color.LIGHT_GRAY))).padRight(8);
        hint.add(new VisLabel("WASD move    Shift boost    F11 fullscreen    Esc menu"));
        root.add(hint).padTop(24);

        stage.addActor(root);
    }

    private void start(Difficulty difficulty) {
        if (switching) return;
        switching = true;
        game.menuConfirm();
        Gdx.input.setInputProcessor(null);
        game.setScreen(new PlayScreen(game, difficulty));
        dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        if (switching) return;
        handleKeys();
        if (switching) return;
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        if (switching) return;
        stage.draw();
    }

    private void handleKeys() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.W) || Gdx.input.isKeyJustPressed(Keys.UP)) {
            index = (index - 1 + CHOICES.length) % CHOICES.length;
            game.menuMove();
        }
        if (Gdx.input.isKeyJustPressed(Keys.S) || Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            index = (index + 1) % CHOICES.length;
            game.menuMove();
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            start(CHOICES[index]);
            return;
        }
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setColor(i == index ? Color.YELLOW : Color.WHITE);
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
