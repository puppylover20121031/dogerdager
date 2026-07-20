package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
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
    private final PostProcessor post;
    private final Stage stage = new Stage(new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H));
    private final Progress progress = new Progress();
    private final VisTextButton[] buttons = new VisTextButton[CHOICES.length];
    private VisTextButton creditsButton;

    private int index = Difficulty.NORMAL.ordinal();
    private boolean switching;

    private Music bgm;
    public MenuScreen(DogerDager game, PostProcessor post) {
        this.game = game;
        this.post = post;
        build(post);
    }

    private void build(PostProcessor post) {
                this.bgm = Gdx.audio.newMusic(Gdx.files.internal("menu.mp3"));
            this.bgm.setLooping(true);
            this.bgm.setVolume(1f);
            //this.bgm.play();
        var root = new VisTable();
        root.setFillParent(true);

        var title = new VisLabel("DOGER DAGER");
        title.setFontScale(2f);
        root.add(title).padBottom(8).row();

        boolean hardcoreUnlocked = progress.hardcoreUnlocked();
        for (int i = 0; i < CHOICES.length; i++) {
            final int index = i;
            var choice = CHOICES[i];
            boolean locked = choice == Difficulty.HARDCORE && !hardcoreUnlocked;
            var button = new VisTextButton(locked ? "HARDCORE  (clear HARD)" : choice.name());
            button.setDisabled(locked);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    start(choice, ((float) index), post);
                }
            });
            buttons[i] = button;
            root.add(button).width(220).height(34).pad(3).row();
        }

        creditsButton = new VisTextButton("CREDITS");
        creditsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                openCredits();
            }
        });
        root.add(creditsButton).width(220).height(34).padTop(8).row();

        root.add(new VisLabel("Best  " + progress.bestOverall())).padTop(20).row();

        var hint = new VisTable();
        hint.add(new Label(Icons.GAMEPAD, new Label.LabelStyle(game.icons().font(), Color.LIGHT_GRAY))).padRight(8);
        hint.add(new VisLabel("WASD move   Shift shield   Tab dash   F11 fullscreen   Esc menu"));
        root.add(hint).padTop(24).row();

        var nav = new VisLabel("T stats     O settings     A achievements     C credits");
        nav.setColor(Color.GRAY);
        root.add(nav).padTop(8);

        stage.addActor(root);
    }

    private void start(Difficulty difficulty, float delta, PostProcessor post) {
        bgm.stop();
        
        if (switching) return;
        if (difficulty == Difficulty.HARDCORE && !progress.hardcoreUnlocked()) {
            game.menuMove();
            return;
        }
        switching = true;
        game.menuConfirm();
        Gdx.input.setInputProcessor(null);
        if (difficulty != Difficulty.CUSTOM) {
        game.setScreen(new PlayScreen(game, difficulty, delta, post));
        } else {
            game.setScreen(new CustomScreen(game));
        }
        dispose();
    }

    private void openStats() {
        bgm.stop();
        if (switching) return;
        switching = true;
        game.menuMove();
        Gdx.input.setInputProcessor(null);
        game.setScreen(new StatsScreen(game, post));
        dispose();
    }

    private void openSettings() {
        bgm.stop();
        if (switching) return;
        switching = true;
        game.menuMove();
        Gdx.input.setInputProcessor(null);
        game.setScreen(new SettingsScreen(game, post));
        dispose();
    }

    private void openAchievements() {
        bgm.stop();
        if (switching) return;
        switching = true;
        game.menuMove();
        Gdx.input.setInputProcessor(null);
        game.setScreen(new AchievementsScreen(game, post));
        dispose();
    }

    private void openCredits() {
        bgm.stop();
        if (switching) return;
        switching = true;
        game.menuMove();
        Gdx.input.setInputProcessor(null);
        game.setScreen(new CreditsScreen(game, post));
        dispose();
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
        handleKeys(post);
        if (switching) return;
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        if (switching) return;
        stage.draw();
    }

    private void handleKeys(PostProcessor post) {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.W) || Gdx.input.isKeyJustPressed(Keys.UP) || Pad.justUp()) {
            index = (index - 1 + CHOICES.length + 1) % (CHOICES.length + 1);
            game.menuMove();
        }
        if (Gdx.input.isKeyJustPressed(Keys.S) || Gdx.input.isKeyJustPressed(Keys.DOWN) || Pad.justDown()) {
            index = (index + 1) % (CHOICES.length + 1);
            game.menuMove();
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE) || Pad.justA()) {
            if (index < CHOICES.length) {
                start(CHOICES[index], 0f, post);
            } else {
                openCredits();
            }
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.T)) {
            openStats();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.O)) {
            openSettings();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.A)) {
            openAchievements();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.C)) {
            openCredits();
            return;
        }
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isDisabled()) continue;
            buttons[i].setColor(i == index ? Color.YELLOW : Color.WHITE);
        }
        if (creditsButton != null) {
            creditsButton.setColor(index == CHOICES.length ? Color.YELLOW : Color.WHITE);
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        bgm.stop();
        stage.dispose();
    }
}
