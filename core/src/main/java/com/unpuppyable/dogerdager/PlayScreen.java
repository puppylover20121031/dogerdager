package com.unpuppyable.dogerdager;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
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
import com.unpuppyable.dogerdager.entity.PlayerArrow;
import com.unpuppyable.dogerdager.entity.Potion;
import com.unpuppyable.dogerdager.entity.Powerup0;
import com.unpuppyable.dogerdager.entity.Powerup1;
import com.unpuppyable.dogerdager.multiplayer.host.Messages;

public class PlayScreen implements Screen {

    protected static final float WORLD_W = 640 * 3;
    protected static final float WORLD_H = 360 * 3;
    protected static final float ARENA_W = WORLD_W;
    protected static final float HUD_H = 72 * 3;
    protected static final float PLAY_TOP = WORLD_H - HUD_H;

    protected static final int INSTANT_KILL = 100_000;
    protected static final float MAX_STEP = 0.05f;
    private static final float HAUNTED_DEMO_SPEED = 4f;
    private static final float HAUNTED_DEMO_FLOOR_TIME = 30f;

    protected enum State {
        PLAYING, PAUSED, GAME_OVER, WON, PLAYER_WON, YOU_DIED
    }

    protected DogerDager game;
    protected Difficulty difficulty;
    public Difficulty curDifficulty;

    protected Viewport viewport;
    public Viewport viewport() {return this.viewport;}
    protected final ShapeRenderer shapes = new ShapeRenderer();
    protected final SpriteBatch batch = new SpriteBatch();
    protected final BitmapFont font = new BitmapFont();
    protected final GlyphLayout layout = new GlyphLayout();
    protected final Progress progress = new Progress();
    protected final String endingText = "THE END\n\nYOU WON\n\n\nCredits\nUnpuppyable\nOwner / Developer\nThe Doger Dager team\n\nR retry   Esc menu";
    protected float endingScroll;

    protected final List<Entity> entities = new ArrayList<>();
    protected final List<Entity> pending = new ArrayList<>();

    private final Preferences prefs = Gdx.app.getPreferences("doger-dager");
    private final KeyBind keyBind = new KeyBind();

    protected Player player;
    protected Hud hud;
    protected Spawner spawner;
    protected State state;
    protected boolean bingo = false;
    protected float shake;
    //private float camX = ARENA_W / 2f;
    protected float camX = ARENA_W;
    protected Music bgm;
    protected boolean playedMusic = false;
    private boolean hauntedDemo;
    private float hauntedDemoElapsed;
    private float hauntedFloorTimer;
    private float hauntedSpawnTimer;
    private Texture hauntedBackground;
    private boolean spookyTerminalPending;

    int fc = 0;

    protected PostProcessor post;

    public PlayScreen(DogerDager game, Difficulty difficulty, float delta, PostProcessor post) {
        this(game, difficulty, delta, post, false);
    }

    public PlayScreen(DogerDager game, Difficulty difficulty, float delta, PostProcessor post, boolean hauntedDemo) {
        this.game = game;
        this.difficulty = difficulty;
        this.post = post;
        this.hauntedDemo = hauntedDemo;
        curDifficulty = difficulty;
        this.viewport = new FitViewport(WORLD_W, WORLD_H);
        hud = new Hud(difficulty, progress.bestScore(difficulty), WORLD_W, WORLD_H, player);
        player = new Player(ARENA_W, PLAY_TOP, post, progress, prefs.getString("user.name"), curDifficulty, true, hud);
        player.setAutomated(hauntedDemo);
        if (hauntedDemo) {
            player.setInvulnerable(true);
            hauntedBackground = new Texture(Gdx.files.internal("glitch-background.jpg"));
        }
        spawner = new Spawner(difficulty, hud, this);
        update(delta, post);
        bingo = Settings.bingo();
        if (prefs.getBoolean("set.music", true)) {
            if (bingo) {
                this.bgm = Gdx.audio.newMusic(Gdx.files.internal("combine.mp3"));
            } else {
                this.bgm = Gdx.audio.newMusic(Gdx.files.internal("combine.mp3"));
            }
            this.bgm.setLooping(true);
            this.bgm.setVolume(1f);
            this.bgm.play();
            prefs.putBoolean("set.music", false);
        }
        reset();
        if (hauntedDemo) shake = 30f;
    }

    protected void reset() {
        playedMusic = true;
        entities.clear();
        pending.clear();
        player.reset();
        hud.resetCheatDetector();
        state = State.PLAYING;
        shake = 0;
        //camX = ARENA_W / 2f;
        camX = ARENA_W;
    }

    public void add(Entity entity) {
        pending.add(entity);
    }

    public void spawn(Enemy.Kind kind) {
        float x = MathUtils.random(0f, ARENA_W - 24);
        float y = MathUtils.random(0f, PLAY_TOP - 24);
        add(new Enemy(kind, x, y, difficulty.enemySpeed, ARENA_W, PLAY_TOP, getTarget()));
    }

    public void spawnBoss(Boss.Kind kind) {
        clearHazards();
        float restY = PLAY_TOP - Boss.SIZE - 8;
        add(new Boss(kind, (ARENA_W - Boss.SIZE) / 2, restY, ARENA_W, this, getTarget()));
    }

    public void spawnPotion() {
        add(new Potion(MathUtils.random(0f, ARENA_W - 16), MathUtils.random(0f, PLAY_TOP - 16)));
    }

    public void spawnPowerup0() {
        add(new Powerup0(MathUtils.random(0f, ARENA_W - 16), MathUtils.random(0f, PLAY_TOP - 16)));
    }
    public void spawnPowerup1() {
        add(new Powerup1(MathUtils.random(0f, ARENA_W - 16), MathUtils.random(0f, PLAY_TOP - 16)));
    }

    public void spawnCentipede() {
        float x = MathUtils.random(60f, ARENA_W - 60);
        float y = MathUtils.random(60f, PLAY_TOP - 60);
        add(new Centipede(x, y, ARENA_W, PLAY_TOP, getTarget()));
    }

    // Vertical laser walls with one guaranteed safe slot -- never an impossible
    // config.
    public void spawnLaserWall() {
        int slots = 5;
        float slotW = ARENA_W / slots;
        float laserW = slotW * 0.78f;
        int gap = MathUtils.random(slots - 1);
        for (int i = 0; i < slots; i++) {
            if (i == gap)
                continue;
            float cx = (i + 0.5f) * slotW;
            add(new Laser(cx - laserW / 2f, laserW, PLAY_TOP));
        }
    }

    public boolean bossActive() {
        for (var e : entities)
            if (e.isBoss())
                return true;
        return false;
    }

    protected void clearHazards() {
        entities.clear();
        pending.clear();
    }

    protected Player getTarget() {
        return this.player;
    }

    // Floor transition: heal, wipe the arena, then either win or stage the next
    // floor.
    public void nextFloor() {
        int floor = hud.advanceFloor();
        if (floor >= 5)
            progress.unlock(Achievement.FLOOR_5);
        if (floor >= 10)
            progress.unlock(Achievement.FLOOR_10);
        clearHazards();
        player.healFull();
        if (floor % 2 == 0) fc++;
        if (fc >= 3) {
            shake = 30f;
            game.unfullscreenWithShrink();
            fc = 0;
        }
        if (floor >= difficulty.winFloor) {
            win();
            return;
        }
        if (floor == difficulty.centipedeFloor) {
            //spawnBoss(Boss.Kind.CENTIPEDE);
        } else if (floor % 4 == 0) {
            Boss.Kind k = floor >= 12 ? Boss.Kind.THREE : floor >= 8 ? Boss.Kind.TWO : Boss.Kind.ONE;
            spawnBoss(k);
        }
    }



    public void win() {
        if (state == State.PLAYING) {
            state = State.WON;
            progress.recordRun(difficulty, hud.highScore(), true);
            switch (difficulty) {
                case EASY -> prefs.putBoolean("Easy_unlock", true);
                case NORMAL -> progress.unlock(Achievement.CLEAR_NORMAL);
                case HARD -> progress.unlock(Achievement.CLEAR_HARD);
                case HARDCORE -> progress.unlock(Achievement.CLEAR_HARDCORE);
                default -> {
                }
            }
            if (progress.achieved(Achievement.CLEAR_HARD) && progress.achieved(Achievement.CLEAR_HARDCORE)
                    && progress.achieved(Achievement.CLEAR_HARD) && prefs.getBoolean("Easy_unlock", false)) {
                progress.unlock(Achievement.CLEAR_ALL);
            }
            if (!DogerDager.getMultiplayer() && !prefs.getBoolean("hauntedWinSeen", false)) {
                spookyTerminalPending = true;
            }
        }
    }

    @Override
    public void render(float delta) {
        if (!hauntedDemo && (keyBind.isJustPressed(KeyBind.Action.PAUSE) || Pad.justStart())) {
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
            if (hauntedDemo) {
                float remaining = Math.min(delta, 0.1f) * HAUNTED_DEMO_SPEED;
                while (remaining > 0 && state == State.PLAYING && !spookyTerminalPending) {
                    float step = Math.min(remaining, MAX_STEP);
                    update(step, post);
                    remaining -= step;
                }
            } else {
                update(Math.min(delta, MAX_STEP), post);
            }
        } else if (state == State.PAUSED) {
            if (Gdx.input.isKeyJustPressed(Keys.Q) || Pad.justStart()) {
                toMenu();
                return;
            }
        } else if (Gdx.input.isKeyJustPressed(Keys.R)) {
            reset();
        }
        if (spookyTerminalPending) {
            game.setScreen(new SpookyTerminalScreen(prefs));
            dispose();
            return;
        }
        if (hauntedDemo && state == State.WON) {
            game.setScreen(new HauntedWinScreen(game));
            dispose();
            return;
        }
        draw(delta);
    }

    protected void toMenu() {
        reset();
        game.setScreen(new MenuScreen(game, post));
        dispose();
    }

    protected void update(float delta, PostProcessor post) {
        if (shake > 0)
            shake -= delta;
        player.update(delta);
        if (hauntedDemo) updateHauntedBot(delta);
        hud.update(delta, player);
        if (hauntedDemo) {
            updateHauntedDemo(delta);
        } else {
            spawner.update(delta);
        }


        for (var e : entities)
            e.update(delta);
        entities.addAll(pending);
        pending.clear();

        for (var e : entities) {
            if (e instanceof PlayerArrow arrow) {
                for (var target : entities) {
                    if (target == arrow || target.dead())
                        continue;
                    if ((target instanceof Enemy || target instanceof Centipede) && arrow.hits(target.bounds())) {
                        arrow.kill();
                        target.kill();
                        progress.unlock(Achievement.TAKE_THAT);
                        break;
                    }
                }
            }
        }

        for (var e : entities) {
            if (e.dead() || !e.hits(player.bounds()))
                continue;
            if (e.heals()) {
                player.heal(2);
                progress.unlock(Achievement.POTIONER);
                e.kill();
            } else if (e.contactDamage() > 0) {
                if (hauntedDemo) continue;
                if (e.knocksBack() && !player.strafing() && !player.invulnerable) {
                    player.knockback(ARENA_W, PLAY_TOP);
                }
                hurt(this, e.contactDamage());
                if (e.diesOnPlayerHit())
                    e.kill();
            }
            if (e.card1()) {
                hud.setCardActive(0, true);
                e.kill();
            } if (e.card2()) {
                hud.setCardActive(1, true);
                e.kill();
            } if (e.card3()) {
                hud.setCardActive(2, true);
                e.kill();
            } if (e.card4()) {
                hud.setCardActive(3, true);
                e.kill();
            } if (e.card5()) {
                hud.setCardActive(4, true);
                e.kill();
            } if (e.card6()) {
                hud.setCardActive(5, true);
                e.kill();
            } if (e.card7()) {
                hud.setCardActive(6, true);
                e.kill();
            } if (e.card8()) {
                hud.setCardActive(7, true);
                e.kill();
            } if (e.card9()) {
                hud.setCardActive(8, true);
                e.kill();
            } if (e.card10()) {
                hud.setCardActive(9, true);
                e.kill();
            } 
        }

        entities.removeIf(Entity::dead);

        if (player.dead()) {
            progress.unlock(Achievement.FIRST_DEATH);
            state = State.GAME_OVER;
            progress.recordRun(difficulty, hud.highScore(), false);
        }


        if (Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT) && Gdx.input.isKeyPressed(Keys.NUM_1) && Gdx.input.isKeyPressed(Keys.S)) {
            hud.markCheatUsed();
            spawnPotion();
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(1000); // 1 second delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                spawnPotion();
            });
            thread.start();
        }

        // if (Pad.justL() || Gdx.input.isKeyJustPressed(Keys.M)) {
        // if (mute) {
        // bgm.pause();
        // mute = false;
        // } else {
        // bgm.play();
        // mute = true;
        // }
        // } 
        final Settings settings = new Settings();

        settings.setGlitch(true);


        if (Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT) && Gdx.input.isKeyPressed(Keys.ALT_RIGHT)
                && Gdx.input.isKeyPressed(Keys.W)) {
            hud.markCheatUsed();
            win();
        }


        



        if (Gdx.input.isKeyPressed(Keys.NUM_1) && Gdx.input.isKeyPressed(Keys.ALT_RIGHT) && Gdx.input.isKeyPressed(Keys.C)) {
            hud.markCheatUsed();
            hud.setCardActive(0, true);
        }

        if (Gdx.input.isKeyPressed(Keys.NUM_2) && Gdx.input.isKeyPressed(Keys.ALT_RIGHT) && Gdx.input.isKeyPressed(Keys.C)) {
            hud.markCheatUsed();
            hud.setCardActive(1, true);
        }

        if (Gdx.input.isKeyPressed(Keys.NUM_3) && Gdx.input.isKeyPressed(Keys.ALT_RIGHT) && Gdx.input.isKeyPressed(Keys.C)) {
            hud.markCheatUsed();
            hud.setCardActive(2, true);
        }

        if (Gdx.input.isKeyPressed(Keys.NUM_4) && Gdx.input.isKeyPressed(Keys.ALT_RIGHT) && Gdx.input.isKeyPressed(Keys.C)) {
            hud.markCheatUsed();
            hud.setCardActive(3, true);
        }

        if (Gdx.input.isKeyPressed(Keys.NUM_5) && Gdx.input.isKeyPressed(Keys.ALT_RIGHT) && Gdx.input.isKeyPressed(Keys.C)) {
            hud.markCheatUsed();
            hud.setCardActive(4, true);
        }



        Boolean mute = true;

        if (Gdx.input.isKeyPressed(Keys.M)) {
            mute = !mute;
            if (mute) {
                bgm.pause();
            } else {
                bgm.play();
            }
        }



        if(player.strafing() && this.post.getGlitch()) {
            progress.unlock(Achievement.USE_GLITCH);
        }
        
        
        if (DogerDager.getMultiplayer()) {
            Messages.playerEffects(player, true, null);
        }

    }

    private void updateHauntedDemo(float delta) {
        hauntedDemoElapsed += delta;
        hauntedFloorTimer += delta;
        hauntedSpawnTimer -= delta;
        hud.setFloorProgress(hauntedFloorTimer / HAUNTED_DEMO_FLOOR_TIME);

        if (hauntedSpawnTimer <= 0 && !bossActive()) {
            spawn(Enemy.Kind.NORMAL);
            hauntedSpawnTimer = 1.4f;
        }

        if (hauntedFloorTimer >= HAUNTED_DEMO_FLOOR_TIME) {
            hauntedFloorTimer -= HAUNTED_DEMO_FLOOR_TIME;
            nextFloor();
            if (state == State.WON) {
                hauntedDemo = false;
                player.setAutomated(false);
                player.setInvulnerable(false);
                if (hauntedBackground != null) {
                    hauntedBackground.dispose();
                    hauntedBackground = null;
                }
            }
        }
    }

    private void updateHauntedBot(float delta) {
        float targetX = player.bounds().x + Player.SIZE + 100f;
        float targetY = player.bounds().y + Player.SIZE / 2f;
        float nearestDistance = Float.MAX_VALUE;
        for (Entity entity : entities) {
            if (entity.dead() || entity.contactDamage() <= 0) continue;
            float dx = entity.bounds().x + entity.bounds().width / 2f - (player.bounds().x + Player.SIZE / 2f);
            float dy = entity.bounds().y + entity.bounds().height / 2f - (player.bounds().y + Player.SIZE / 2f);
            float distance = dx * dx + dy * dy;
            if (distance < nearestDistance) {
                nearestDistance = distance;
                targetX = entity.bounds().x + entity.bounds().width / 2f;
                targetY = entity.bounds().y + entity.bounds().height / 2f;
            }
        }

        float angle = hauntedDemoElapsed * 1.7f;
        player.autoPlay(delta, MathUtils.cos(angle), MathUtils.sin(angle * 0.73f), targetX, targetY);
    }

    protected static void hurt(PlayScreen playScreen, int amount) {
        if (playScreen.player.strafing()) {
            return;
        }
        int dmg = playScreen.difficulty.instantKill() ? INSTANT_KILL : Math.max(1, amount + playScreen.difficulty.hitBonus);
        if (playScreen.player.damage(dmg)) {
            playScreen.shake = 0.22f;
        }
    }

    protected void draw(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        var cam = viewport.getCamera();

        // World pass: the camera eases toward the player horizontally, clamped to the
        // arena.
        float targetX = MathUtils.clamp(player.bounds().x + Player.SIZE / 2f, WORLD_W / 2f, ARENA_W - WORLD_W / 2f);
        camX = MathUtils.lerp(camX, targetX, Math.min(1f, 9f * delta));
        float drawX = camX;
        float drawY = WORLD_H / 2f;
        if (shake > 0) {
            float mag = shake > 1f ? 5f : shake * 10f;
            drawX += MathUtils.random(-mag, mag);
            drawY += MathUtils.random(-mag, mag);
        }
        cam.position.set(drawX, drawY, 0);
        cam.update();
        shapes.setProjectionMatrix(cam.combined);
        if (hauntedBackground != null) {
            batch.setProjectionMatrix(cam.combined);
            batch.begin();
            batch.draw(hauntedBackground, 0, 0, ARENA_W, WORLD_H);
            batch.end();
        }
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (hauntedBackground == null) drawBackground(shapes);
        player.draw(shapes);
        for (var e : entities)
            e.draw(shapes);
        shapes.end();

        // HUD pass: fixed screen-space camera.
        cam.position.set(WORLD_W / 2f, WORLD_H / 2f, 0);
        cam.update();
        shapes.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        hud.drawBars(shapes, player);
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
            drawMovieEnding(endingText.replace("YOU WON", "YOU WON"), delta);
        } else if (state == State.GAME_OVER) {
            drawMovieEnding(endingText.replace("YOU WON", "GAME OVER"), delta);
        }
        batch.end();
    }

    // World-space backdrop -- gives the panning camera something to scroll over so
    // motion reads.
    protected void drawBackground(ShapeRenderer shapes) {
        shapes.setColor(0.05f, 0.05f, 0.08f, 1f);
        shapes.rect(0, 0, ARENA_W, PLAY_TOP);
        shapes.setColor(0.11f, 0.11f, 0.16f, 1f);
        for (float x = 0; x <= ARENA_W; x += 32f)
            shapes.rect(x, 0, 1f, PLAY_TOP);
        for (float y = 0; y <= PLAY_TOP; y += 32f)
            shapes.rect(0, y, ARENA_W, 1f);
        shapes.setColor(0.28f, 0.30f, 0.42f, 1f);
        shapes.rect(0, 0, ARENA_W, 2f);
        shapes.rect(0, PLAY_TOP - 2f, ARENA_W, 2f);
        shapes.rect(0, 0, 2f, PLAY_TOP);
        shapes.rect(ARENA_W - 2f, 0, 2f, PLAY_TOP);
    }

    protected void drawCentered(String text) {
        font.setColor(Color.WHITE);
        layout.setText(font, text);
        font.draw(batch, text, (WORLD_W - layout.width) / 2f, PLAY_TOP / 2f);
    }

    protected void drawMovieEnding(String text, float delta) {
        if (state != State.PAUSED) {
            endingScroll += delta * 50f;
        }
        font.setColor(Color.WHITE);
        font.getData().setScale(1.4f);
        layout.setText(font, "THE END");
        float titleY = PLAY_TOP / 2f + 90f;
        font.draw(batch, "THE END", (WORLD_W - layout.width) / 2f, titleY);
        font.getData().setScale(1f);
        String[] lines = text.split("\\n");
        float startY = PLAY_TOP / 2f - 20f + endingScroll;
        float lineHeight = 24f;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) continue;
            layout.setText(font, line);
            float y = startY - i * lineHeight;
            if (y + 20f < -40f) {
                endingScroll = 0f;
                break;
            }
            font.draw(batch, line, (WORLD_W - layout.width) / 2f, y);
        }
        font.getData().setScale(1f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (bgm != null) {
            bgm.stop();
            bgm.dispose();
            bgm = null;
        }
        shapes.dispose();
        batch.dispose();
        font.dispose();
        if (hauntedBackground != null) {
            hauntedBackground.dispose();
            hauntedBackground = null;
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}
