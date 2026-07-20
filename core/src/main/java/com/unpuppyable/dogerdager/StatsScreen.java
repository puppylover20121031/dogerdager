package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

public final class StatsScreen extends ScreenAdapter {

    private final DogerDager game;
    private final PostProcessor post;
    private final Stage stage = new Stage(new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H));
    private final Progress progress = new Progress();
    private boolean switching;

    public StatsScreen(DogerDager game, PostProcessor post) {
        this.game = game;
        this.post = post;
        build();
    }

    private void build() {
        var root = new VisTable();
        root.setFillParent(true);
        root.defaults().pad(3);

        var title = new VisLabel("STATISTICS");
        title.setFontScale(1.8f);
        root.add(title).colspan(3).padBottom(20).row();

        root.add(new VisLabel("MODE")).left();
        root.add(new VisLabel("BEST")).padLeft(40);
        root.add(new VisLabel("CLEARED")).padLeft(40).row();

        for (var difficulty : Difficulty.values()) {
            root.add(new VisLabel(difficulty.name())).left();
            root.add(new VisLabel(String.valueOf(progress.bestScore(difficulty)))).padLeft(40);
            root.add(new VisLabel(progress.cleared(difficulty) ? "yes" : "-")).padLeft(40).row();
        }

        root.add(new VisLabel("Runs  " + progress.runs())).colspan(3).padTop(18).row();
        root.add(new VisLabel("Hardcore  " + (progress.hardcoreUnlocked() ? "unlocked" : "locked")))
                .colspan(3).row();

        var back = new VisLabel("Esc  back");
        back.setColor(Color.GRAY);
        root.add(back).colspan(3).padTop(20);

        stage.addActor(root);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        render(delta, post);
    }

    public void render(float delta, PostProcessor post) {
        if (switching) return;
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            switching = true;
            game.setScreen(new MenuScreen(game, post));
            dispose();
            return;
        }
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
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
