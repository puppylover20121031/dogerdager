package com.unpuppyable.dogerdager.multiplayer.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.unpuppyable.dogerdager.*;
import com.unpuppyable.dogerdager.entity.*;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;


public class ClientPlayScreen implements Screen {

    private static final float FLOOR_TIME = 30f;
    protected static final float WORLD_W = 640 * 3;
    protected static final float WORLD_H = 360 * 3;
    static final float ARENA_W = WORLD_W;
    static final float HUD_H = 72 * 3;
    static final float PLAY_TOP = WORLD_H - HUD_H;

    private static ClientPlayScreen instance;

    private DogerDager game = DogerDager.getGameInstance();
    private final KeyBind keyBind = new KeyBind();
    private final Difficulty difficulty;
    protected final Progress progress = new Progress();

    protected Viewport viewport;
    protected final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final ReceiverHud hud;

    protected float shake;
    protected float camX = ARENA_W;

    private boolean waitingForExitConfirm = false;
    private float anim = 0;
    private float timer = 0;
    private float floorTimer = 0;
    private long lastTick;
    private long secondLastTick;
    private float interpTimer;

    private final HashSet<String> keysDown = new HashSet<String>();
    private HashSet<String> previousKeysDown = new HashSet<String>();

    private ConcurrentHashMap<String, EntityState> previousEntities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, EntityState> entities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, EntityState> entitiesToRender = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, EntityState> players = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, EntityState> playersToRender = new ConcurrentHashMap<>();
    private final String yourName;
    private EntityState player;

    public ClientPlayScreen(DogerDager game, PostProcessor post, Difficulty difficulty, String name) {
        this.yourName = name;
        this.viewport = new FitViewport(WORLD_W, WORLD_H);
        this.difficulty = difficulty;
        hud = new ReceiverHud(difficulty, progress.bestScore(difficulty), WORLD_W, WORLD_H);
        instance = this;
    }

    @Override
    public void render(float delta) {
        anim+=delta;
        handleKeys(delta);
        draw(delta);

        update(delta);
        hud.update(delta);
    }

    public void update(float delta) {
        floorTimer += delta;
        hud.setFloorProgress(floorTimer / FLOOR_TIME);
        if (floorTimer >= FLOOR_TIME) {
            floorTimer = 0;
            hud.advanceFloor();
        }
        interpTimer += delta;
        interpolateEntities();
    }

    private void handleKeys(float delta) {
        if (waitingForExitConfirm && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Pad.justA())) {
            game.setScreen(new MenuScreen(game, game.post));
            dispose();
        }

        if (keyBind.isJustPressed(KeyBind.Action.PAUSE) || Pad.justStart()) {
            if (waitingForExitConfirm) {
                waitingForExitConfirm = false;
                return;
            }
            waitingForExitConfirm = true;
        }

        if (!WebsocketClient.getClientInstance().verified) {
            batch.begin();
            drawCentered("Encountered a connection issue, redirecting to menu...");
            batch.end();
            timer += delta;
            if (timer >= 3) {
                game.setScreen(new MenuScreen(game, game.post));
                dispose();
            }
            return;
        }

        if (keyBind.isPressed(KeyBind.Action.MOVE_UP) || Pad.justUp()) {
            keysDown.add("W");
        }

        if (keyBind.isPressed(KeyBind.Action.MOVE_DOWN) || Pad.justDown()) {
            keysDown.add("S");
        }

        if (keyBind.isPressed(KeyBind.Action.MOVE_LEFT) || Pad.justLeft()) {
            keysDown.add("A");
        }

        if (keyBind.isPressed(KeyBind.Action.MOVE_RIGHT) || Pad.justDown()) {
            keysDown.add("D");
        }

        if (keyBind.isPressed(KeyBind.Action.STRAFE) || Pad.justA()) {
            keysDown.add("TAB");
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)
            || Pad.shield()) {
            keysDown.add("SHIFT_LEFT");
        }

        if (keyBind.isJustPressed(KeyBind.Action.SHOOT) || Pad.justB()) {
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
        Vector3 aim = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(aim);
        boolean isPressed = Gdx.input.isTouched() || Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        ClientMessages.shoot((int)aim.x, (int)aim.y, isPressed);
    }

    public void newEntityStates(HashSet<Object> entitySet) {
        interpTimer = 0;

        previousEntities = new ConcurrentHashMap<>(entities);
        for (EntityState oldEnt : previousEntities.values()) {
            if (entityDied(oldEnt.id, entitySet)) entities.remove(oldEnt.id);
        }

        for (var newEntity : entitySet) {
            if (!(newEntity instanceof EntityState newEnt)) continue;
            entities.put(newEnt.id, newEnt);
            if (newEnt.name == null) continue;
            players.put(newEnt.name, newEnt);
        }
        secondLastTick = lastTick;
        lastTick = System.currentTimeMillis();
    }

    public void updateEntityStates(HashSet<Object> entitySet) {
        interpTimer = 0;
        previousEntities = new ConcurrentHashMap<>(entities);

        for (var newEntity : entitySet) {
            if (!(newEntity instanceof EntityState newEnt)) continue;
            EntityState old = previousEntities.get(newEnt.id);
            if (old == null) continue;

            EntityState e = new EntityState(
                    newEnt.id,
                    newEnt.type == null ? old.type : newEnt.type,
                    newEnt.x == null ? old.x : newEnt.x,
                    newEnt.y == null ? old.y : newEnt.y,
                    newEnt.name == null ? old.name : newEnt.name,
                    newEnt.dead == null ? old.dead : newEnt.dead,
                    newEnt.stamina == null ? old.stamina : newEnt.stamina,
                    newEnt.shielded == null ? old.shielded : newEnt.shielded,
                    newEnt.invulnerable == null ? old.invulnerable : newEnt.invulnerable,
                    newEnt.strafeinvuln == null ? old.strafeinvuln : newEnt.strafeinvuln,
                    newEnt.stun == null ? old.stun : newEnt.stun,
                    newEnt.hp == null ? old.hp : newEnt.hp,
                    newEnt.seg == null ? old.seg : newEnt.seg,
                    newEnt.heading == null ? old.heading : newEnt.heading,
                    newEnt.kind == null ? old.kind : newEnt.kind,
                    newEnt.ang == null ? old.ang : newEnt.ang,
                    newEnt.telegraph == null ? old.telegraph : newEnt.telegraph,
                    newEnt.targetX == null ? old.targetX : newEnt.targetX,
                    newEnt.targetY == null ? old.targetY : newEnt.targetY,
                    newEnt.settled == null ? old.settled : newEnt.settled,
                    newEnt.fireTimer == null ? old.fireTimer : newEnt.fireTimer,
                    newEnt.phase == null ? old.phase : newEnt.phase,
                    newEnt.atkTimer == null ? old.atkTimer : newEnt.atkTimer,
                    newEnt.life == null ? old.life : newEnt.life
            );
            entities.put(e.id, e);
            if (e.name == null) continue;
            players.put(e.name, e);
        }
        secondLastTick = lastTick;
        lastTick = System.currentTimeMillis();
    }

    private boolean entityDied(String id, HashSet<Object> entitySet) {
        for (var entity : entitySet) {
            if (!(entity instanceof EntityState ent)) continue;
            if (id.equals(ent.id)) return false;
        }
        return true;
    }

    private void interpolateEntities() {
        if (entities.isEmpty() || previousEntities.isEmpty()) return;
        if (lastTick == 0 || secondLastTick == 0) return;
        entitiesToRender.clear();
        playersToRender.clear();

        long tickDiff = lastTick - secondLastTick;
        for (String entityId : previousEntities.keySet()) {
            if (!entities.containsKey(entityId)) {
                entitiesToRender.put(entityId, previousEntities.get(entityId));
                continue;
            }
            EntityState oldEnt = previousEntities.get(entityId);
            EntityState newEnt = entities.get(entityId);
            float t = MathUtils.clamp(interpTimer / (tickDiff / 1000f), 0f, 1f);

            EntityState interpEntity = new EntityState(oldEnt);
            interpEntity.x = MathUtils.lerp(oldEnt.x, newEnt.x, t);
            interpEntity.y = MathUtils.lerp(oldEnt.y, newEnt.y, t);

            if (interpEntity.type.equals("Centipede") && interpEntity.seg != null) {
                for (int i = 0; i < oldEnt.seg.length; i++) {
                    if (oldEnt.seg[i] == null || newEnt.seg[i] == null) continue;

                    interpEntity.seg[i].x = MathUtils.lerp(oldEnt.seg[i].x, newEnt.seg[i].x, t);
                    interpEntity.seg[i].y = MathUtils.lerp(oldEnt.seg[i].y, newEnt.seg[i].y, t);
                }
            }

            entitiesToRender.put(entityId, interpEntity);
            if (interpEntity.name == null) continue;
            playersToRender.put(interpEntity.name, interpEntity);
        }
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
        batch.setProjectionMatrix(cam.combined);
        shapes.setProjectionMatrix(cam.combined);

        if (entitiesToRender.isEmpty()) {
            ErrorNotifier.show("skipped wrong values");
            return;
        }
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawBackground(shapes);
        for (EntityState e : entitiesToRender.values()) {
            drawEntity(e, shapes);
        }

        if (waitingForExitConfirm) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.setColor(0f, 0f, 0f, 0.6f);
            shapes.rect(0, 0, WORLD_W, WORLD_H);
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
        hud.drawBars(shapes);
        shapes.end();

        batch.begin();
        hud.drawText(batch, font);
        if (waitingForExitConfirm) {
            drawCentered("Are you sure you want to leave? (press enter or A on pad)");
        }
        batch.end();
    }

    private void drawEntity(EntityState entity, ShapeRenderer shapes) {
        if (entity.type == null) return;

        switch (entity.type) {
            case "Player" -> {
                if (entity.invulnerable == null || entity.strafeinvuln == null || entity.stun == null || entity.shielded == null || entity.stamina == null) return;

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
                shapes.setColor(switch (entity.kind) {
                    case "NORMAL" -> Color.RED;
                    case "FAST" -> Color.GRAY;
                    case "SMART" -> Color.GREEN;
                    default -> Color.RED;
                });
                shapes.rect(entity.x, entity.y, Enemy.SIZE, Enemy.SIZE);
            }
            case "Boss" -> {
                if (entity.kind.equals("THREE") && entity.settled && entity.fireTimer < 0.3f) {
                    float intensity = 1f - entity.fireTimer / 0.3f;
                    shapes.setColor(1f, 0.25f * intensity, 0.1f, 1f);
                    shapes.rectLine(entity.x + Boss.SIZE / 2f, entity.y, entity.targetX + 8f, entity.targetY + 8f, 1.5f);
                }
                Color body = switch (entity.kind) {
                    case "ARM" -> Color.MAROON;
                    case "ONE" -> Boss.ONE_COL;
                    case "TWO" -> Boss.TWO_COL;
                    case "THREE" -> entity.phase <= 1 ? Color.FIREBRICK : entity.phase == 2 ? Color.ORANGE : entity.phase == 3 ? Color.SCARLET : Color.VIOLET;
                    case "CENTIPEDE" -> Boss.CENTIPEDE_COL;
                    default -> Color.MAGENTA;
                };
                if ((entity.kind.equals("ONE") || entity.kind.equals("TWO") || entity.kind.equals("CENTIPEDE")) && entity.settled && entity.atkTimer < 0.25f) {
                    body = body.cpy().lerp(Color.WHITE, 1f - entity.atkTimer / 0.25f);
                }
                shapes.setColor(body);
                shapes.rect(entity.x, entity.y, Boss.SIZE, Boss.SIZE);

                if (entity.kind.equals("CENTIPEDE")) {
                    shapes.setColor(Color.GOLD);
                    for (int i = 0; i < 4; i++) {
                        shapes.rect(entity.x + 12 + i * 18, entity.y + Boss.SIZE / 3f, 10, Boss.SIZE / 3f);
                    }
                }
            }
            case "Bullet" -> {
                int size = entity.kind.equals("FALLING") ? 32 : entity.kind.equals("ROCKET") ? 18 : entity.kind.equals("SHARD") ? 12 : 16;
                if (entity.kind.equals("ROCKET")) {
                    float cx = entity.x + size / 2f;
                    float cy = entity.y + size / 2f;
                    float w = 6, h = 20;
                    shapes.setColor(Color.GOLD);
                    shapes.rect(cx - w / 2f, cy - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, entity.ang);
                    return;
                }
                shapes.setColor(entity.kind.equals("HOMING") ? Color.ROYAL : entity.kind.equals("SHARD") ? Color.ORANGE : Color.RED);
                shapes.rect(entity.x, entity.y, size, size);
            }
            case "Laser" -> {
                int slots = 5;
                float slotW = ARENA_W / slots;
                float laserW = slotW * 0.78f;
                if (entity.telegraph > 0) {
                    shapes.setColor(0.6f, 0.05f, 0.05f, 1f);
                    float cx = entity.x + laserW / 2f;
                    shapes.rect(cx - 1.5f, entity.y, 3, laserW);
                } else {
                    shapes.setColor(1f, 0.25f, 0.2f, 1f);
                    shapes.rect(entity.x, entity.y, laserW, laserW);
                }
            }
            case "PlayerArrow" -> {
                shapes.setColor(Color.GOLD);
                shapes.rect(entity.x, entity.y, PlayerArrow.SIZE, PlayerArrow.SIZE);
            }
            case "Potion" -> {
                if (entity.life < 2f && (int) (anim * 8) % 2 == 0) return;
                shapes.setColor(Color.CYAN);
                shapes.rect(entity.x, entity.y, Potion.SIZE, Potion.SIZE);
            }
            case "Powerup1" -> {
                if (entity.life < 2f && (int) (anim * 8) % 2 == 0) return;
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

    protected void drawCentered(String text) {
        font.setColor(Color.WHITE);
        layout.setText(font, text);
        font.draw(batch, text, (WORLD_W - layout.width) / 2f, PLAY_TOP / 2f);
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
        WebsocketClient.closeClient();
    }

    public static ClientPlayScreen getInstance() {
        return instance;
    }

    public EntityState getPlayer() {
        return player;
    }

    public static final class EntityState {
        public final String id;
        public final String type;
        public Float x;
        public Float y;
        public final String name;
        //player
        public final Boolean dead;
        public final Float stamina;
        public final Boolean shielded;
        public final Boolean invulnerable;
        public final Float strafeinvuln;
        public final Float stun;
        public final Float hp;
        //centipede
        public final Vector2[] seg;
        public final Float heading;
        //boss, bullet, enemy
        public final String kind;
        //bullet special
        public final Float ang;
        //laser
        public final Float telegraph;
        //boss
        public final Float targetX;
        public final Float targetY;
        public final Boolean settled;
        public final Float fireTimer;
        public final Integer phase;
        public final Float atkTimer;
        public final Float life;

        public EntityState(
                String id,
                String type,
                Float x,
                Float y,
                String name,
                //player
                Boolean dead,
                Float stamina,
                Boolean shielded,
                Boolean invulnerable,
                Float strafeinvuln,
                Float stun,
                Float hp,
                //centipede
                Vector2[] seg,
                Float heading,
                //boss, bullet, enemy
                String kind,
                //bullet special
                Float ang,
                //laser
                Float telegraph,
                //boss
                Float targetX,
                Float targetY,
                Boolean settled,
                Float fireTimer,
                Integer phase,
                Float atkTimer,
                //powerup and potion
                Float life
        ) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
            this.name = name;
            this.dead = dead;
            this.stamina = stamina;
            this.shielded = shielded;
            this.invulnerable = invulnerable;
            this.strafeinvuln = strafeinvuln;
            this.stun = stun;
            this.hp = hp;
            this.seg = seg;
            this.heading = heading;
            this.kind = kind;
            this.ang = ang;
            this.telegraph = telegraph;
            this.targetX = targetX;
            this.targetY = targetY;
            this.settled = settled;
            this.fireTimer = fireTimer;
            this.phase = phase;
            this.atkTimer = atkTimer;
            this.life = life;
        }

        public EntityState(EntityState other) {
            this(other.id, other.type, other.x, other.y, other.name,
                    other.dead, other.stamina, other.shielded, other.invulnerable,
                    other.strafeinvuln, other.stun, other.hp, other.seg, other.heading,
                    other.kind, other.ang, other.telegraph, other.targetX, other.targetY,
                    other.settled, other.fireTimer, other.phase, other.atkTimer, other.life);
        }
    }
}
