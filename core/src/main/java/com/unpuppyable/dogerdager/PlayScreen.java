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
import com.unpuppyable.dogerdager.entity.Boss;
import com.unpuppyable.dogerdager.entity.Bullet;
import com.unpuppyable.dogerdager.entity.Enemy;
import com.unpuppyable.dogerdager.entity.Player;
import com.unpuppyable.dogerdager.entity.Potion;

import java.util.ArrayList;
import java.util.List;

public final class PlayScreen implements Screen {

    static final float WORLD_W = 640;
    static final float WORLD_H = 360;
    static final float HUD_H = 72;
    static final float PLAY_TOP = WORLD_H - HUD_H;

    private static final int INSTANT_KILL = 100_000;
    private static final float MAX_STEP = 0.05f;

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
    private final List<Boss> bosses = new ArrayList<>();
    private final List<Boss> pendingBosses = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final Music bgm;
    private final Sound failSound;

    private Player player;
    private Hud hud;
    private Spawner spawner;
    private Potion potion;
    private State state;
    private boolean muted = true;

    public PlayScreen(DogerDager game, Difficulty difficulty) {
        this.game = game;
        this.difficulty = difficulty;
        this.viewport = new FitViewport(WORLD_W, WORLD_H);
        this.bgm = Gdx.audio.newMusic(Gdx.files.internal("puppysong.mp3"));
        this.bgm.setLooping(true);
        this.bgm.setVolume(0f);
        this.failSound = Gdx.audio.newSound(Gdx.files.internal("losing.wav"));
        reset();
    }

    private void reset() {
        enemies.clear();
        bosses.clear();
        pendingBosses.clear();
        bullets.clear();
        potion = null;
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

    public void spawnBoss(Boss.Kind kind) {
        clearHazards();
        float restY = PLAY_TOP - Boss.SIZE - 8;
        bosses.add(new Boss(kind, (WORLD_W - Boss.SIZE) / 2, restY, WORLD_W, this, player));
    }

    public void addBoss(Boss boss) {
        pendingBosses.add(boss);
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    public void spawnPotion() {
        potion = new Potion(MathUtils.random(0f, WORLD_W - 16), MathUtils.random(0f, PLAY_TOP - 16));
    }

    private void clearHazards() {
        enemies.clear();
        bullets.clear();
        bosses.clear();
        potion = null;
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
        if (Gdx.input.isKeyJustPressed(Keys.M)) {
            muted = !muted;
            bgm.setVolume(muted ? 0f : 0.5f);
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            bgm.stop();
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }
        if (state == State.PLAYING) update(Math.min(delta, MAX_STEP));
        else if (Gdx.input.isKeyJustPressed(Keys.R)) reset();
        draw();
    }

    private void update(float delta) {
        boolean shield = hud.update(delta, Gdx.input.isKeyPressed(Keys.SHIFT_LEFT));
        player.setShielded(shield);
        player.update(delta);
        spawner.update(delta);

        for (var enemy : enemies) {
            enemy.update(delta);
            if (enemy.bounds().overlaps(player.bounds())) hurt(enemy.damage());
        }
        for (var boss : bosses) {
            boss.update(delta);
            if (boss.bounds().overlaps(player.bounds())) hurt(25);
        }
        bosses.addAll(pendingBosses);
        pendingBosses.clear();

        for (var bullet : bullets) {
            bullet.update(delta);
            if (!bullet.dead() && bullet.bounds().overlaps(player.bounds())) {
                hurt(bullet.damage());
                bullet.kill();
            }
        }
        if (potion != null && potion.bounds().overlaps(player.bounds())) {
            hud.healFull();
            potion = null;
        }

        enemies.removeIf(Enemy::dead);
        bullets.removeIf(Bullet::dead);
        bosses.removeIf(Boss::dead);

        player.setInvulnerable(hud.invulnerable());

        if (hud.dead()) {
            state = State.GAME_OVER;
            bgm.stop();
            failSound.play();
            saveScore();
        }
    }

    private void hurt(int amount) {
        if (player.strafing()) return;
        hud.damage(difficulty.instantKill() ? INSTANT_KILL : amount);
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        player.draw(shapes);
        for (var enemy : enemies) enemy.draw(shapes);
        for (var boss : bosses) boss.draw(shapes);
        for (var bullet : bullets) bullet.draw(shapes);
        if (potion != null) potion.draw(shapes);
        hud.drawBars(shapes);
        shapes.end();

        batch.begin();
        hud.drawText(batch, font);
        if (state != State.PLAYING) drawCentered(state == State.WON
                ? "YOU WON  -  R retry   Esc menu" : "GAME OVER  -  R retry   Esc menu");
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
