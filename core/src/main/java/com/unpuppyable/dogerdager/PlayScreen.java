package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.unpuppyable.dogerdager.entity.Arrow;
import com.unpuppyable.dogerdager.entity.Enemy;
import com.unpuppyable.dogerdager.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class PlayScreen implements Screen {

    static final float WORLD_W = 640;
    static final float WORLD_H = 477;
    static final float HUD_H = 72;
    static final float PLAY_TOP = WORLD_H - HUD_H;

    private enum State { PLAYING, GAME_OVER, WON }

    private final DogerDager game;
    private final Difficulty difficulty;

    private final Viewport viewport;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Preferences prefs = Gdx.app.getPreferences("doger-dager");

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Arrow> arrows = new ArrayList<>();
    private final Music bgm;
    private final Sound failSound;

    private Player player;
    private Hud hud;
    private Spawner spawner;
    private State state;

    public PlayScreen(DogerDager game, Difficulty difficulty) {
        this.game = game;
        this.difficulty = difficulty;
        this.viewport = new FitViewport(WORLD_W, WORLD_H);
        this.bgm = Gdx.audio.newMusic(Gdx.files.internal("song.wav"));
        this.bgm.setLooping(true);
        this.failSound = Gdx.audio.newSound(Gdx.files.internal("losing.wav"));
        reset();
    }

    private void reset() {
        enemies.clear();
        arrows.clear();
        player = new Player(WORLD_W, PLAY_TOP);
        hud = new Hud(difficulty, prefs.getInteger("highScore", 0), WORLD_W, WORLD_H);
        spawner = new Spawner(difficulty, hud, this);
        state = State.PLAYING;
        bgm.stop();
        bgm.play();
    }

    public void spawn(Enemy.Kind kind) {
        float x = MathUtils.random(0f, WORLD_W - 24);
        float y = MathUtils.random(0f, PLAY_TOP - 24);
        enemies.add(new Enemy(kind, x, y, difficulty.enemySpeed, WORLD_W, PLAY_TOP, player));
    }

    public void win() {
        if (state == State.PLAYING) {
            state = State.WON;
            bgm.stop();
            saveScore();
        }
    }

    @Override
    public void render(float delta) {
        if (state == State.PLAYING) update(delta);
        else if (Gdx.input.isKeyJustPressed(Keys.R)) reset();
        draw();
    }

    private void update(float delta) {
        boolean shield = hud.update(delta, Gdx.input.isKeyPressed(Keys.SHIFT_LEFT));
        player.setShielded(shield);
        player.update(delta);
        spawner.update(delta);

        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            arrows.add(new Arrow(player.bounds().x + Player.SIZE, player.bounds().y, WORLD_W));
        }

        for (var enemy : enemies) {
            enemy.update(delta);
            if (enemy.bounds().overlaps(player.bounds())) {
                hud.damage(difficulty.instantKill() ? hud.level() + 1_000 : 5);
            }
        }
        for (var arrow : arrows) {
            arrow.update(delta);
            for (var enemy : enemies) {
                if (!enemy.dead() && arrow.bounds().overlaps(enemy.bounds())) {
                    enemy.kill();
                    arrow.kill();
                }
            }
        }
        enemies.removeIf(Enemy::dead);
        arrows.removeIf(Arrow::dead);

        if (hud.dead()) {
            state = State.GAME_OVER;
            bgm.stop();
            failSound.play();
            saveScore();
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        player.draw(shapes);
        for (var enemy : enemies) enemy.draw(shapes);
        for (var arrow : arrows) arrow.draw(shapes);
        hud.drawBars(shapes);
        shapes.end();

        batch.begin();
        hud.drawText(batch, font);
        if (state != State.PLAYING) drawCentered(state == State.WON
                ? "YOU WON  -  press R" : "GAME OVER  -  press R");
        batch.end();
    }

    private void drawCentered(String text) {
        font.setColor(Color.WHITE);
        layout.setText(font, text);
        font.draw(batch, text, (WORLD_W - layout.width) / 2f, PLAY_TOP / 2f);
    }

    private void saveScore() {
        prefs.putInteger("highScore", hud.highScore());
        prefs.flush();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
        bgm.dispose();
        failSound.dispose();
    }

    @Override public void show() { }
    @Override public void hide() { }
    @Override public void pause() { }
    @Override public void resume() { }
}
