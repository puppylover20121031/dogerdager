package com.unpuppyable.dogerdager.multiplayer.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.unpuppyable.dogerdager.*;
import com.unpuppyable.dogerdager.entity.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.badlogic.gdx.graphics.g3d.particles.ParticleShader.AlignMode.Screen;


public class ClientPlayScreen implements Screen {
    protected static final float WORLD_W = 640 * 3;
    protected static final float WORLD_H = 360 * 3;
    static final float ARENA_W = WORLD_W;
    static final float HUD_H = 72 * 3;
    static final float PLAY_TOP = WORLD_H - HUD_H;

    protected static final int INSTANT_KILL = 100_000;
    protected static final float MAX_STEP = 0.05f;
    public static boolean playerShootingEnabled = false;
    protected static final float PLAYER_SHOOT_SPEED = 380f;
    protected static final float PLAYER_SHOOT_COOLDOWN = 0.18f;
    private final KeyBind keyBind = new KeyBind();

    protected float shake;
    //private float camX = ARENA_W / 2f;
    protected float camX = ARENA_W;
    protected Viewport viewport;
    protected final ShapeRenderer shapes = new ShapeRenderer();

    private HashSet<String> keysDown = new HashSet<String>();
    private HashSet<String> previousKeysDown = new HashSet<String>();
    private HashMap<String, EntityState> entities = new HashMap<String, EntityState>();
    private HashMap<String, EntityState> previousEntities = new HashMap<String, EntityState>();
    private HashMap<String, EntityState> players = new HashMap<String, EntityState>();
    private static ClientPlayScreen instance;
    private String yourName;
    private EntityState player;
    private float anim = 0;


    public ClientPlayScreen(DogerDager game, PostProcessor post, String name) {
        this.yourName = name;
        this.viewport = new FitViewport(WORLD_W, WORLD_H);
        instance = this;
    }

    @Override
    public void render(float delta) {
        anim+=delta;
        handleKeys();
        draw(delta);
    }

    private void handleKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            //are you sure you want to leave popup
        }

        if (!WebsocketClient.getClientInstance().verified) {
            //error and in 5 seconds redirect to menu
            return;
        }

        if (keyBind.isPressed(KeyBind.Action.MOVE_UP)) {
            keysDown.add("W");
        }

        if (keyBind.isPressed(KeyBind.Action.MOVE_DOWN)) {
            keysDown.add("S");
        }

        if (keyBind.isPressed(KeyBind.Action.MOVE_LEFT)) {
            keysDown.add("A");
        }

        if (keyBind.isPressed(KeyBind.Action.MOVE_RIGHT)) {
            keysDown.add("D");
        }

        if (keyBind.isPressed(KeyBind.Action.STRAFE) || Pad.justA()) {
            keysDown.add("TAB");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            shoot();
        }

        if (keysDown == previousKeysDown) return;
        ClientMessages.keysDown(keysDown);
        HashSet<String> keysUp = new HashSet<String>();
        for (String key : previousKeysDown) {
            if (!keysDown.contains(key)) {
                keysUp.add(key);
            }
        }
        if (previousKeysDown==keysDown) {
            keysDown.clear();
            return;
        }
        ClientMessages.keysUp(keysUp);
        previousKeysDown = new HashSet<>(keysDown);
        keysDown.clear();
    }

    protected void shoot() {
        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        boolean isPressed = Gdx.input.isTouched() || Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        ClientMessages.shoot(x, y, isPressed);
    }

    public void updateStates(HashSet<Object> entitySet) {
        previousEntities = new HashMap<>(entities);
        for (EntityState oldEnt : previousEntities.values()) {
            if (entityDied(oldEnt.id, entitySet)) entities.remove(oldEnt.id);
        }

        for (var newEntity : entitySet) {
            if (!(newEntity instanceof EntityState newEnt)) continue;
            entities.put(newEnt.id, newEnt);
            if (newEnt.name == null) continue;
            players.put(newEnt.name, newEnt);
        }
    }

    private boolean entityDied(String id, HashSet<Object> entitySet) {
        for (var entity : entitySet) {
            if (!(entity instanceof EntityState ent)) continue;
            if (id.equals(ent.id)) return false;
        }
        return true;
    }

    protected void draw(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        var cam = viewport.getCamera();
        if (!players.containsKey(yourName)) {
            cam.position.set(ARENA_W / 2f, WORLD_H / 2f, 0);
            cam.update();
            shapes.setProjectionMatrix(cam.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            drawBackground(shapes);
            shapes.end();
            return;
        }
        player = players.get(yourName);
        float targetX = MathUtils.clamp(player.x + Player.SIZE / 2f, WORLD_W / 2f, ARENA_W - WORLD_W / 2f);
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
        for (EntityState e : entities.values()) {
            drawEntity(e, shapes);
        }
        shapes.end();
    }

    private void drawEntity(EntityState entity, ShapeRenderer shapes) {
        switch (entity.type()) {
            case "Player" -> {
                if (entity.strafeinvuln > 0) {
                    EntityState prevPlayer = previousEntities.get(entity.id);
                    shapes.setColor(0.4f, 0.7f, 1f, 1f);
                    shapes.rect(prevPlayer.x, prevPlayer.y, Player.SIZE, Player.SIZE);
                }
                if (entity.invulnerable && (int) (anim * 10) % 2 == 0) return;
                Color body = entity.stun > 0 ? Color.GRAY : entity.shielded ? Color.SKY : Color.CYAN;
                shapes.setColor(body);
                shapes.rect(entity.x, entity.y, Player.SIZE, Player.SIZE);
                shapes.setColor(Color.BLACK);
                shapes.rect(entity.x + 2, entity.y + 2, Player.SIZE - 4, Player.SIZE - 4);
                shapes.setColor(body);
                shapes.rect(entity.x + 2, entity.y + 2, Player.SIZE - 4, (Player.SIZE - 4) * entity.stamina);
            }
            case "Centipede" -> {
                // legs: a wiggling pair per body segment, perpendicular to the spine
                shapes.setColor(Centipede.LEG);
                for (int i = 1; i < Centipede.SEGMENTS; i++) {
                    Vector2 a = entity.seg[i - 1];
                    Vector2 b = entity.seg[i];
                    float ang = MathUtils.atan2(b.y - a.y, b.x - a.x);
                    float nx = -MathUtils.sin(ang);
                    float ny = MathUtils.cos(ang);
                    float legLen = radius(i) + 4f + MathUtils.sin(anim * 14 + i) * 3f;
                    shapes.rectLine(b.x, b.y, b.x + nx * legLen, b.y + ny * legLen, 1.6f);
                    shapes.rectLine(b.x, b.y, b.x - nx * legLen, b.y - ny * legLen, 1.6f);
                }

                // body: drawn tail-first so the head sits on top
                for (int i = Centipede.SEGMENTS - 1; i >= 0; i--) {
                    shapes.setColor(i == 0 ? Centipede.HEAD : (i % 2 == 0 ? Centipede.BAND_A : Centipede.BAND_B));
                    shapes.circle(entity.seg[i].x, entity.seg[i].y, radius(i));
                }

                // head: forward antennae + a pair of eyes
                Vector2 h = entity.seg[0];
                float fx = MathUtils.cos(entity.heading);
                float fy = MathUtils.sin(entity.heading);
                float sx = -fy;
                float sy = fx;
                shapes.setColor(Centipede.HEAD);
                shapes.rectLine(h.x, h.y, h.x + (fx + sx * 0.5f) * 13f, h.y + (fy + sy * 0.5f) * 13f, 1.4f);
                shapes.rectLine(h.x, h.y, h.x + (fx - sx * 0.5f) * 13f, h.y + (fy - sy * 0.5f) * 13f, 1.4f);
                shapes.setColor(Color.BLACK);
                shapes.circle(h.x + fx * 3f + sx * 3f, h.y + fy * 3f + sy * 3f, 1.8f);
                shapes.circle(h.x + fx * 3f - sx * 3f, h.y + fy * 3f - sy * 3f, 1.8f);
            }
            case "Enemy" -> {
                /*shapes.setColor(switch (entity.kind) {
                    case "NORMAL" -> Color.RED;
                    case "FAST" -> Color.GRAY;
                    case "SMART" -> Color.GREEN;
                });
                shapes.rect(entity.x, entity.y, entity.width, entity.height);*/
                shapes.setColor(Color.ORANGE);
                shapes.rect(entity.x, entity.y, Player.SIZE, Player.SIZE);
            }
            case "Boss" -> {
                /*if (entity.kind == "THREE" && entity.settled && entity.fireTimer < 0.3f) {
                    float intensity = 1f - entity.fireTimer / 0.3f;
                    shapes.setColor(1f, 0.25f * intensity, 0.1f, 1f);
                    shapes.rectLine(entity.x + Boss.SIZE / 2f, entity.y, target.bounds().x + 8f, target.bounds().y + 8f, 1.5f);
                }
                Color body = switch (kind) {
                    case ARM -> Color.MAROON;
                    case ONE -> ONE_COL;
                    case TWO -> TWO_COL;
                    case THREE -> phase <= 1 ? Color.FIREBRICK : phase == 2 ? Color.ORANGE : phase == 3 ? Color.SCARLET : Color.VIOLET;
                    case CENTIPEDE -> CENTIPEDE_COL;
                };
                if ((kind == Boss.Kind.ONE || kind == Boss.Kind.TWO || kind == Boss.Kind.CENTIPEDE) && settled && atkTimer < 0.25f) {
                    body = body.cpy().lerp(Color.WHITE, 1f - atkTimer / 0.25f);
                }
                shapes.setColor(body);
                shapes.rect(bounds.x, bounds.y, SIZE, SIZE);

                if (kind == Boss.Kind.CENTIPEDE) {
                    shapes.setColor(Color.GOLD);
                    for (int i = 0; i < 4; i++) {
                        shapes.rect(bounds.x + 12 + i * 18, bounds.y + SIZE / 3f, 10, SIZE / 3f);
                    }
                }*/
                shapes.setColor(Color.ORANGE);
                shapes.rect(entity.x, entity.y, Boss.SIZE, Boss.SIZE);
            }
            case "Bullet" -> {
                /*if (entity.kind == "ROCKET") {
                    for (int i = 0; i < entity.filled; i++) {
                        int idx = (entity.head - 1 - i + 2 * Bullet.TRAIL) % Bullet.TRAIL;
                        float t = 1f - (float) i / Bullet.TRAIL;
                        float s = 7f * t;
                        shapes.setColor(0.9f * t, 0.7f * t, 0.1f * t, 1f);
                    }
                    float cx = entity.x + entity.width / 2f;
                    float cy = entity.y + entity.height / 2f;
                    float w = 6, h = 20;
                    shapes.setColor(Color.GOLD);
                    shapes.rect(cx - w / 2f, cy - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, ang);
                    return;
                }
                shapes.setColor(kind == Bullet.Kind.HOMING ? Color.ROYAL : kind == Bullet.Kind.SHARD ? Color.ORANGE : Color.RED);
                shapes.rect(entity.x, entity.y, entity.width, bounds.height);*/
                shapes.setColor(Color.ORANGE);
                //int size = entity.kind.equals("FALLING") ? 32 : entity.kind.equals("ROCKET") ? 18 : entity.kind.equals("SHARD") ? 12 : 16;
                shapes.rect(entity.x, entity.y, 16, 16);
            }
            case "Laser" -> {
                int slots = 5;
                float slotW = ARENA_W / slots;
                float laserW = slotW * 0.78f;
                /*if (entity.telegraph > 0) {
                    shapes.setColor(0.6f, 0.05f, 0.05f, 1f);
                    float cx = entity.x + laserW / 2f;
                    shapes.rect(cx - 1.5f, entity.y, 3, laserW);
                } else {
                    shapes.setColor(1f, 0.25f, 0.2f, 1f);
                    shapes.rect(entity.x, entity.y, laserW, laserW);
                }*/
                shapes.setColor(Color.RED);
                shapes.rect(entity.x, entity.y, laserW, laserW);
            }
            case "PlayerArrow" -> {
                shapes.setColor(Color.GOLD);
                shapes.rect(entity.x, entity.y, PlayerArrow.SIZE, PlayerArrow.SIZE);
            }
            case "Potion" -> {
                shapes.setColor(Color.CYAN);
                shapes.rect(entity.x, entity.y, Potion.SIZE, Potion.SIZE);
            }
            case "Powerup1" -> {
                shapes.setColor(Color.WHITE);
                shapes.rect(entity.x, entity.y, Powerup1.SIZE, Powerup1.SIZE);
            }
            default -> {}
        }
    }

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
    //centipede
    private static float radius(int i) {
    return MathUtils.lerp(Centipede.HEAD_R, Centipede.TAIL_R, i / (float) (Centipede.SEGMENTS - 1));
}

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public static ClientPlayScreen getInstance() {
        return instance;
    }

    public record EntityState(
            String id,
            String type,
            float x,
            float y,
            String name,
            //player
            boolean dead,
            Float stamina,
            boolean shielded,
            boolean invulnerable,
            Float strafeinvuln,
            Float stun,
            //centipede
            Vector2[] seg,
            Float heading
    ) {}
}
