package com.unpuppyable.dogerdager.multiplayer.host;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.unpuppyable.dogerdager.*;
import com.unpuppyable.dogerdager.entity.*;
import org.java_websocket.WebSocket;

import java.util.HashMap;


public class HostPlayScreen extends PlayScreen {

    private final KeyBind keyBind = new KeyBind();
    static final float ARENA_W = WORLD_W;
    static final float HUD_H = 72 * 3;
    static final float PLAY_TOP = WORLD_H - HUD_H;
    private final Progress progress = new Progress();
    private float tickrate = 30f;
    private float netTimer;
    private float timeDelta;
    private static HostPlayScreen instance;
    private Websocket wsInstance = Websocket.getInstance();
    private final Preferences prefs = Gdx.app.getPreferences("doger-dager");
    private static HashMap<String, Player> players = new HashMap<String, Player>();
    private Player host;

    protected State state;

    protected enum State {
        PLAYING, PAUSED, DEAD, GAME_OVER, WON
    }

    public HostPlayScreen(DogerDager game, Difficulty difficulty, float delta, PostProcessor post) {
        super(game, difficulty, delta, post);
        instance = this;
        curDifficulty = difficulty;
        this.viewport = new FitViewport(WORLD_W, WORLD_H);
        for (WebSocket conn : wsInstance.users.keySet()) {
            String playerName = wsInstance.users.get(conn);
            if (conn == null) {
                host = player;
                players.put(playerName, host);
                continue;
            }
            players.put(playerName, new Player(ARENA_W, PLAY_TOP, post, progress, playerName, curDifficulty, false));
        }
        if (host == null) return;
        hud = new Hud(difficulty, progress.bestScore(difficulty), WORLD_W, WORLD_H, host);
        spawner = new Spawner(difficulty, hud, this);
        update(delta, post);
        bingo = Settings.bingo();
        if (prefs.getBoolean("set.music", true)) {
            if (bingo) this.bgm = Gdx.audio.newMusic(Gdx.files.internal("bingo.mp3"));
            this.bgm.setLooping(true);
            this.bgm.setVolume(1f);
            this.bgm.play();
        }
        reset();
        if (progress.achieved(Achievement.CLEAR_NORMAL) || prefs.getBoolean("Easy_unlock", false))
            playerShootingEnabled = true;
        Messages.gameStarted(curDifficulty, tickrate);
        DogerDager.multiplayerGameStarted = true;
        state = State.PLAYING;
    }

    @Override public void render(float delta) {
        int playersDead = 0;
        for (Player p : players.values()) {
            if (p.dead()) playersDead++;
        }
        if (playersDead>=players.size()) state = State.GAME_OVER;

        if (keyBind.isJustPressed(KeyBind.Action.PAUSE) || Pad.justStart()) {
            if (state == State.PLAYING) {
                state = State.PAUSED;
            } else if (state == State.PAUSED) {
                state = State.PLAYING;
            } else {
                toMenu();
                return;
            }
        }
        if (state == State.PLAYING || state == State.DEAD) {
            update(Math.min(delta, MAX_STEP), post);
        } else if (state == State.PAUSED) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                DogerDager.multiplayerGameStarted = false;
                toMenu();
                return;
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reset();
        }
        draw(delta);
        //networking
        netTimer += delta;
        if (netTimer < 1f/tickrate) return;
        netTimer = 0;
        Messages.stateUpdate(players, entities);
    }

    protected void update(float delta, PostProcessor post) {
        timeDelta = delta;
        if (host==null) return;
        if (shake > 0)
            shake -= delta;
        hud.update(delta);
        for (Player player : players.values()) {
            player.update(delta);
        }
        spawner.update(delta);
        if (shootCooldown > 0)
            shootCooldown -= delta;
        if (playerShootingEnabled && shootCooldown <= 0
                && (keyBind.isJustPressed(KeyBind.Action.SHOOT) || Pad.justB())) {
            shootPlayer();
            shootCooldown = PLAYER_SHOOT_COOLDOWN;
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
        //loop for players
        for (Player p : players.values()) {
            if (p.dead()) continue;
            for (var e : entities) {
                if (e.dead() || !e.hits(p.bounds()))
                    continue;
                if (e.heals()) {
                    p.heal(2);
                    e.kill();
                } else if (e.contactDamage() > 0) {
                    if (e.knocksBack() && !p.strafing() && !p.invulnerable) {
                        p.knockback(ARENA_W, PLAY_TOP);
                    }
                    hurt(p, e.contactDamage());
                    if (e.diesOnPlayerHit())
                        e.kill();
                }
            }
        }

        //loop for enemies then host
        for (var e : entities) {
            if (e.dead() || !e.hits(host.bounds()))
                continue;
            if (e.heals()) {
                host.heal(2);
                progress.unlock(Achievement.POTIONER);
                e.kill();
            } else if (e.contactDamage() > 0) {
                if (e.knocksBack() && !host.strafing() && !host.invulnerable) {
                    host.knockback(ARENA_W, PLAY_TOP);
                }
                hurt(e.contactDamage());
                if (e.diesOnPlayerHit())
                    e.kill();
            } if (e.glitches()) {
                post.setGlitch(true);
                e.kill();
            }
        }

        entities.removeIf(Entity::dead);

        if (host.dead()) {
            progress.unlock(Achievement.FIRST_DEATH);
            state = State.DEAD;
            progress.recordRun(difficulty, hud.highScore(), false);
        }
    }

    protected void draw(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        var cam = viewport.getCamera();

        // World pass: the camera eases toward the player horizontally, clamped to the
        // arena.
        float targetX = MathUtils.clamp(host.bounds().x + Player.SIZE / 2f, WORLD_W / 2f, ARENA_W - WORLD_W / 2f);
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
        for (Player p : players.values()) {
            p.draw(shapes);
        }
        for (var e : entities)
            e.draw(shapes);
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
        } else if (state == State.DEAD){
            drawCentered("You Died!"); //respawn
        } else if (state == State.WON) {
            drawMovieEnding(endingText.replace("YOU WON", "YOU WON"), delta);
        } else if (state == State.GAME_OVER) {
            drawMovieEnding(endingText.replace("YOU WON", "GAME OVER"), delta);
        }
        batch.end();
    }

    @Override
    protected void hurt(int amount) {
        if (host.strafing())
            return;
        int dmg = difficulty.instantKill() ? INSTANT_KILL : Math.max(1, amount + difficulty.hitBonus);
        if (host.damage(dmg)) {
            host.health -= dmg;
            shake = 0.22f;
        }
    }
    protected void hurt(Player player, int amount) {
        if (player.strafing())
            return;
        int dmg = difficulty.instantKill() ? INSTANT_KILL : Math.max(1, amount + difficulty.hitBonus);
        player.damage(dmg);
    }

    @Override
    protected void shootPlayer() {
        if (host.dead()) return;
        Vector3 aim = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(aim);
        float px = host.bounds().x + Player.SIZE / 2f;
        float py = host.bounds().y + Player.SIZE / 2f;
        float dx = 0f;
        float dy = 0f;
        if (Gdx.input.isTouched() || Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            dx = aim.x - px;
            dy = aim.y - py;
        }
        if (Math.abs(dx) < 0.1f && Math.abs(dy) < 0.1f) {
            dx = host.aimX();
            dy = host.aimY();
        }
        if (Math.abs(dx) < 0.1f && Math.abs(dy) < 0.1f) {
            dx = 1f;
            dy = 0f;
        }
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        float vx = dx / len * PLAYER_SHOOT_SPEED;
        float vy = dy / len * PLAYER_SHOOT_SPEED;
        add(new PlayerArrow(px - PlayerArrow.SIZE / 2f, py - PlayerArrow.SIZE / 2f, vx, vy, ARENA_W, PLAY_TOP, host));
    }

    protected void shootPlayer(Player player, int worldX, int worldY, boolean pressed) {
        if (player.dead()) return;
        if (shootCooldown > 0)
            shootCooldown -= timeDelta;
        if (!playerShootingEnabled || shootCooldown > 0) return;

        Vector3 aim = new Vector3(worldX, worldY, 0);
        float px = player.bounds().x + Player.SIZE / 2f;
        float py = player.bounds().y + Player.SIZE / 2f;
        float dx = 0f;
        float dy = 0f;
        if (pressed) {
            dx = aim.x - px;
            dy = aim.y - py;
        }
        if (Math.abs(dx) < 0.1f && Math.abs(dy) < 0.1f) {
            dx = player.aimX();
            dy = player.aimY();
        }
        if (Math.abs(dx) < 0.1f && Math.abs(dy) < 0.1f) {
            dx = 1f;
            dy = 0f;
        }
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        float vx = dx / len * PLAYER_SHOOT_SPEED;
        float vy = dy / len * PLAYER_SHOOT_SPEED;
        add(new PlayerArrow(px - PlayerArrow.SIZE / 2f, py - PlayerArrow.SIZE / 2f, vx, vy, ARENA_W, PLAY_TOP, player));
        shootCooldown = PLAYER_SHOOT_COOLDOWN;
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
        for (Player p : players.values()) {
            p.healFull();
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

    public static Player getPlayerByName(String name) {
        return players.get(name);
    }

    public static HostPlayScreen getInstance() {
        return instance;
    }

}
