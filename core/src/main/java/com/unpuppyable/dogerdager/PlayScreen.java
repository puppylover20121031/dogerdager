package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
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
import com.unpuppyable.dogerdager.entity.Centipede;
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

    private Player player;
    private Hud hud;
    private Spawner spawner;
    private State state;
    private float shake;
    private float camX = ARENA_W / 2f;

    public PlayScreen(DogerDager game, Difficulty difficulty) {
        this.game = game;
        this.difficulty = difficulty;
        this.viewport = new FitViewport(WORLD_W, WORLD_H);
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
        camX = ARENA_W / 2f;
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

    public void spawnCentipede() {
        float x = MathUtils.random(60f, ARENA_W - 60);
        float y = MathUtils.random(60f, PLAY_TOP - 60);
        add(new Centipede(x, y, ARENA_W, PLAY_TOP, player));
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

    // Floor transition: heal, wipe the arena, then either win or stage the next floor.
    public void nextFloor() {
        int floor = hud.advanceFloor();
        if (floor >= 5) progress.unlock(Achievement.FLOOR_5);
        if (floor >= 10) progress.unlock(Achievement.FLOOR_10);
        clearHazards();
        hud.healFull();
        if (floor >= difficulty.winFloor) {
            win();
            return;
        }
        if (floor % 4 == 0) {
            Boss.Kind k = floor >= 12 ? Boss.Kind.THREE : floor >= 8 ? Boss.Kind.TWO : Boss.Kind.ONE;
            spawnBoss(k);
        }
    }

    public void win() {
        if (state == State.PLAYING) {
            state = State.WON;
            progress.recordRun(difficulty, hud.highScore(), true);
            switch (difficulty) {
                case NORMAL -> progress.unlock(Achievement.CLEAR_NORMAL);
                case HARD -> progress.unlock(Achievement.CLEAR_HARD);
                case HARDCORE -> progress.unlock(Achievement.CLEAR_HARDCORE);
                default -> { }
            }
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Pad.justStart()) {
            if (state == State.PLAYING) {
                state = State.PAUSED;
            } else if (state == State.PAUSED) {
                state = State.PLAYING;
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
        draw(delta);
    }

    private void toMenu() {
        game.setScreen(new MenuScreen(game));
        dispose();
    }

    private void update(float delta) {
        if (shake > 0) shake -= delta;
        boolean shield = hud.update(delta, Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Pad.shield());
        player.setShielded(shield);
        player.setStamina(hud.staminaFraction());
        player.update(delta);
        spawner.update(delta);

        for (var e : entities) e.update(delta);
        entities.addAll(pending);
        pending.clear();

        for (var e : entities) {
            if (e.dead() || !e.hits(player.bounds())) continue;
            if (e.heals()) {
                hud.heal(2);
                progress.unlock(Achievement.POTIONER);
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
            progress.recordRun(difficulty, hud.highScore(), false);
        }
    }

    private void hurt(int amount) {
        if (player.strafing()) return;
        int dmg = difficulty.instantKill() ? INSTANT_KILL : Math.max(1, amount + difficulty.hitBonus);
        if (hud.damage(dmg)) {
            shake = 0.22f;
        }
    }

    private void draw(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        var cam = viewport.getCamera();

        // World pass: the camera eases toward the player horizontally, clamped to the arena.
        float targetX = MathUtils.clamp(player.bounds().x + Player.SIZE / 2f, WORLD_W / 2f, ARENA_W - WORLD_W / 2f);
        camX = MathUtils.lerp(camX, targetX, Math.min(1f, 9f * delta));
        float drawX = camX;
        float drawY = WORLD_H / 2f;
        if (shake > 0) {
            float mag = shake * 45;
            drawX += MathUtils.random(-mag, mag);
            drawY += MathUtils.random(-mag, mag);
        }
        cam.position.set(drawX, drawY, 0);
        cam.update();
        shapes.setProjectionMatrix(cam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawBackground(shapes);
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
        if (state == State.PAUSED) {
            drawCentered("PAUSED   -   Esc resume   Q menu");
        } else if (state == State.WON) {
            drawCentered("YOU WON  -  R retry   Esc menu");
        } else if (state == State.GAME_OVER) {
            drawCentered("GAME OVER  -  R retry   Esc menu");
        }
        batch.end();
    }

    // World-space backdrop -- gives the panning camera something to scroll over so motion reads.
    private void drawBackground(ShapeRenderer shapes) {
        shapes.setColor(0.05f, 0.05f, 0.08f, 1f);
        shapes.rect(0, 0, ARENA_W, PLAY_TOP);
        shapes.setColor(0.11f, 0.11f, 0.16f, 1f);
        for (float x = 0; x <= ARENA_W; x += 32f) shapes.rect(x, 0, 1f, PLAY_TOP);
        for (float y = 0; y <= PLAY_TOP; y += 32f) shapes.rect(0, y, ARENA_W, 1f);
        shapes.setColor(0.28f, 0.30f, 0.42f, 1f);
        shapes.rect(0, 0, ARENA_W, 2f);
        shapes.rect(0, PLAY_TOP - 2f, ARENA_W, 2f);
        shapes.rect(0, 0, 2f, PLAY_TOP);
        shapes.rect(ARENA_W - 2f, 0, 2f, PLAY_TOP);
    }

    private void drawCentered(String text) {
        font.setColor(Color.WHITE);
        layout.setText(font, text);
        font.draw(batch, text, (WORLD_W - layout.width) / 2f, PLAY_TOP / 2f);
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
    }

    @Override public void show() { }
    @Override public void hide() { }
    @Override public void pause() { }
    @Override public void resume() { }
}
