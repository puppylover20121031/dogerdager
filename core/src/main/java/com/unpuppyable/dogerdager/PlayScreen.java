package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.unpuppyable.dogerdager.entity.Boss;
import com.unpuppyable.dogerdager.entity.Enemy;
import com.unpuppyable.dogerdager.entity.Entity;
import com.unpuppyable.dogerdager.entity.Laser;
import com.unpuppyable.dogerdager.entity.Player;
import com.unpuppyable.dogerdager.entity.Potion;

import java.util.ArrayList;
import java.util.List;

public final class PlayScreen implements Screen {

    static final float WORLD_W = 640;
    static final float WORLD_H = 360;
    static final float ARENA_W = 704;
    static final float HUD_H = 72;
    static final float PLAY_TOP = WORLD_H - HUD_H;

    private static final int INSTANT_KILL = 100_000;
    private static final float MAX_STEP = 0.05f;

    private enum State { PLAYING, PAUSED, GAME_OVER, WON }

    private final DogerDager game;
    private final Difficulty difficulty;

    private final Viewport viewport;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Progress progress = new Progress();

    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> pending = new ArrayList<>();
    private final Music bgm;
    private final Sound failSound;

    private Player player;
    private Hud hud;
    private Spawner spawner;
    private State state;
    private boolean muted = true;
    private float shake;
    private boolean cheats;
    private boolean god;

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
        entities.clear();
        pending.clear();
        player = new Player(ARENA_W, PLAY_TOP);
        hud = new Hud(difficulty, progress.bestScore(difficulty), WORLD_W, WORLD_H);
        spawner = new Spawner(difficulty, hud, this);
        state = State.PLAYING;
        shake = 0;
        bgm.stop();
        bgm.play();
    }

    public void add(Entity entity) {
        pending.add(entity);
    }

    public void spawn(Enemy.Kind kind) {
        float x = MathUtils.random(0f, ARENA_W - 24);
        float y = MathUtils.random(0f, PLAY_TOP - 24);
        add(new Enemy(kind, x, y, difficulty.enemySpeed, ARENA_W, PLAY_TOP, player));
    }

    public void spawnBoss(Boss.Kind kind) {
        clearHazards();
        float restY = PLAY_TOP - Boss.SIZE - 8;
        add(new Boss(kind, (ARENA_W - Boss.SIZE) / 2, restY, ARENA_W, this, player));
    }

    public void spawnPotion() {
        add(new Potion(MathUtils.random(0f, ARENA_W - 16), MathUtils.random(0f, PLAY_TOP - 16)));
    }

    // Vertical laser walls with one guaranteed safe slot -- never an impossible config.
    public void spawnLaserWall() {
        int slots = 5;
        float slotW = ARENA_W / slots;
        float laserW = slotW * 0.78f;
        int gap = MathUtils.random(slots - 1);
        for (int i = 0; i < slots; i++) {
            if (i == gap) continue;
            float cx = (i + 0.5f) * slotW;
            add(new Laser(cx - laserW / 2f, laserW, PLAY_TOP));
        }
    }

    public boolean bossActive() {
        for (var e : entities) if (e.isBoss()) return true;
        return false;
    }

    private void clearHazards() {
        entities.clear();
        pending.clear();
    }

    public void win() {
        if (state == State.PLAYING) {
            state = State.WON;
            bgm.stop();
            progress.recordRun(difficulty, hud.highScore(), true);
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.M)) {
            muted = !muted;
            bgm.setVolume(muted ? 0f : 0.5f);
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            if (state == State.PLAYING) {
                state = State.PAUSED;
                bgm.pause();
            } else if (state == State.PAUSED) {
                state = State.PLAYING;
                bgm.play();
            } else {
                toMenu();
                return;
            }
        }
        if (state == State.PLAYING) {
            update(Math.min(delta, MAX_STEP));
        } else if (state == State.PAUSED) {
            if (Gdx.input.isKeyJustPressed(Keys.Q)) {
                toMenu();
                return;
            }
        } else if (Gdx.input.isKeyJustPressed(Keys.R)) {
            reset();
        }
        draw();





    }

    private void toMenu() {
        bgm.stop();
        game.setScreen(new MenuScreen(game));
        dispose();
    }

    private void update(float delta) {
        if (shake > 0) shake -= delta;
        boolean shield = hud.update(delta, Gdx.input.isKeyPressed(Keys.SHIFT_LEFT));
        player.setShielded(shield);
        player.setStamina(hud.staminaFraction());
        player.update(delta);
        spawner.update(delta);

        for (var e : entities) e.update(delta);
        entities.addAll(pending);
        pending.clear();

        for (var e : entities) {
            if (e.dead() || !e.bounds().overlaps(player.bounds())) continue;
            if (e.heals()) {
                hud.healFull();
                e.kill();
            } else if (e.contactDamage() > 0) {
                if (e.knocksBack() && !player.strafing() && !hud.invulnerable()) {
                    player.knockback(ARENA_W, PLAY_TOP);
                }
                hurt(e.contactDamage());
                if (e.diesOnPlayerHit()) e.kill();
            }
        }

        entities.removeIf(Entity::dead);

        player.setInvulnerable(hud.invulnerable());

        if (hud.dead()) {
            state = State.GAME_OVER;
            bgm.stop();
            failSound.play();
            progress.recordRun(difficulty, hud.highScore(), false);
        }

        cheatInput();
    }

    // Чит-меню для тех, кто совершенно ничего не умеет.
    private void cheatInput() {
        if (Gdx.input.isKeyJustPressed(Keys.GRAVE)) cheats = !cheats;
        if (!cheats) return;
        if (Gdx.input.isKeyJustPressed(Keys.NUM_1)) god = !god;
        if (Gdx.input.isKeyJustPressed(Keys.NUM_2)) hud.healFull();
        if (Gdx.input.isKeyJustPressed(Keys.NUM_3)) hud.refillStamina();
        if (Gdx.input.isKeyJustPressed(Keys.NUM_4)) spawnBoss(Boss.Kind.THREE);
        if (Gdx.input.isKeyJustPressed(Keys.NUM_5)) win();
    }

    private void hurt(int amount) {
        if (god || player.strafing()) return;
        if (hud.damage(difficulty.instantKill() ? INSTANT_KILL : amount)) {
            shake = 0.22f;
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        var cam = viewport.getCamera();

        // World pass: the camera follows the player horizontally, clamped to the arena.
        float camX = MathUtils.clamp(player.bounds().x + Player.SIZE / 2f, WORLD_W / 2f, ARENA_W - WORLD_W / 2f);
        float camY = WORLD_H / 2f;
        if (shake > 0) {
            float mag = shake * 45;
            camX += MathUtils.random(-mag, mag);
            camY += MathUtils.random(-mag, mag);
        }
        cam.position.set(camX, camY, 0);
        cam.update();
        shapes.setProjectionMatrix(cam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        player.draw(shapes);
        for (var e : entities) e.draw(shapes);
        shapes.end();

        // HUD pass: fixed screen-space camera.
        cam.position.set(WORLD_W / 2f, WORLD_H / 2f, 0);
        cam.update();
        shapes.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        hud.drawBars(shapes);
        if (state == State.PAUSED) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.setColor(0f, 0f, 0f, 0.6f);
            shapes.rect(0, 0, WORLD_W, WORLD_H);
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
        shapes.end();

        batch.begin();
        hud.drawText(batch, font);
        if (cheats) drawCheats();
        if (state == State.PAUSED) {
            drawCentered("PAUSED   -   Esc resume   Q menu");
        } else if (state == State.WON) {
            drawCentered("YOU WON  -  R retry   Esc menu");
        } else if (state == State.GAME_OVER) {
            drawCentered("GAME OVER  -  R retry   Esc menu");
        }
        batch.end();
    }

    private void drawCentered(String text) {
        font.setColor(Color.WHITE);
        layout.setText(font, text);
        font.draw(batch, text, (WORLD_W - layout.width) / 2f, PLAY_TOP / 2f);
    }

    private void drawCheats() {
        font.setColor(Color.LIME);
        float y = PLAY_TOP - 16;
        font.draw(batch, "CHEATS  ~", 16, y);
        font.draw(batch, "1 god " + (god ? "ON" : "off"), 16, y - 18);
        font.draw(batch, "2 heal   3 stamina", 16, y - 34);
        font.draw(batch, "4 boss3  5 win", 16, y - 50);
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
