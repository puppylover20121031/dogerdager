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

public final class AchievementsScreen extends ScreenAdapter {

    private final DogerDager game;
    private final PostProcessor post;
    private final Stage stage = new Stage(new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H));
    private final Progress progress = new Progress();
    private boolean switching;

    public AchievementsScreen(DogerDager game, PostProcessor post) {
        this.game = game;
        this.post = post;
        build();
    }

    private void build() {
        var root = new VisTable();
        root.setFillParent(true);
        root.defaults().pad(3);

        var title = new VisLabel("ACHIEVEMENTS");
        title.setFontScale(1.8f);
        root.add(title).colspan(2).padBottom(18).row();

        int done = 0;
        for (var a : Achievement.values()) {
            boolean got = progress.achieved(a);
            if (got) done++;
            var name = new VisLabel((got ? "[x]  " : "[ ]  ") + a.title);
            name.setColor(got ? Color.WHITE : Color.GRAY);
            var desc = new VisLabel(a.desc);
            desc.setColor(Color.GRAY);
            root.add(name).left();
            root.add(desc).left().padLeft(30).row();
        }

        root.add(new VisLabel(done + " / " + Achievement.values().length)).colspan(2).padTop(16).row();

        var back = new VisLabel("Esc  back");
        back.setColor(Color.GRAY);
        root.add(back).colspan(2).padTop(14);

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
