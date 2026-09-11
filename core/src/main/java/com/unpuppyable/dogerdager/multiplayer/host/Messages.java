package com.unpuppyable.dogerdager.multiplayer.host;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;
import com.unpuppyable.dogerdager.Difficulty;
import com.unpuppyable.dogerdager.DogerDager;
import com.unpuppyable.dogerdager.ErrorNotifier;
import com.unpuppyable.dogerdager.MultiplayerScreen;
import com.unpuppyable.dogerdager.entity.*;
import org.java_websocket.WebSocket;

import java.util.*;

import static com.unpuppyable.dogerdager.multiplayer.host.Websocket.*;

public class Messages {
    private static Websocket wsInstance = getInstance();
    private static HashMap<WebSocket, String> users = wsInstance.users;
    private final static Preferences prefs = Gdx.app.getPreferences("doger-dager");

    private static HashMap<Player, PlayerValues> previousPlayers = new HashMap<Player, PlayerValues>();
    private static HashMap<Entity, Object> previousEntities = new HashMap<Entity, Object>();

    // client -> server
    public static void authorize(WebSocket conn, Map<String, Object> msg) {
        HashMap<String, Object> response = new HashMap<String, Object>();
        if (DogerDager.multiplayerGameStarted) return;
        if (users.containsKey(conn)) {
            response.put("success", false);
            response.put("message", "you are already logged in");
            wsInstance.sendWS(conn, "400", response);
            return;
        }
        if (!msg.containsKey("user")) return;
        String name = msg.get("user")+"";
        if (users.containsValue(name)) {
            response.put("success", false);
            response.put("message", "name already taken");
            wsInstance.sendWS(conn, "400", response);
            return;
        }
        if (!isNameLegal(name)) {
            response.put("success", false);
            response.put("message", "illegal name");
            wsInstance.sendWS(conn, "400", response);
            return;
        }
        //broadcast user joined
        response.put("user", name);
        wsInstance.broadcastWS("201", response);
        wsInstance.notVerified.remove(conn);
        users.put(conn, name);
        MultiplayerScreen.addToUserList(name);
        wsInstance.lastPing.put(conn, System.currentTimeMillis());
        //responding with success
        response.clear();
        response.put("success", true);
        response.put("message", "you have logged in");
        response.put("users", users.values());
        wsInstance.sendWS(conn, "200", response);
    }

    public static void keyDown(WebSocket conn, Map<String, Object> msg) {
        String name = users.get(conn);
        if (!msg.containsKey("keys")) return;
        if (!(msg.get("keys") instanceof List<?> keys)) return;
        for (var key : keys) {
            if (!(key instanceof String k)) return;
            Player player = HostPlayScreen.getPlayerByName(name);
            player.keyDown(k);
        }
    }

    public static void keyUp(WebSocket conn, Map<String, Object> msg) {
        String name = users.get(conn);
        if (!msg.containsKey("keys")) return;
        if (!(msg.get("keys") instanceof List<?> keys)) return;
        for (var key : keys) {
            if (!(key instanceof String k)) return;
            Player player = HostPlayScreen.getPlayerByName(name);
            player.keyUp(k);
        }
    }

    public static void shoot(WebSocket conn, Map<String, Object> msg) {
        String name = users.get(conn);
        if (!msg.containsKey("x") || !msg.containsKey("y") || !msg.containsKey("pressing")) return;
        if (!(msg.get("x") instanceof Double x)) return;
        if (!(msg.get("y") instanceof Double y)) return;
        if (!(msg.get("pressing") instanceof Boolean pressing)) return;
        Player player = HostPlayScreen.getPlayerByName(name);
        player.shoot(x.intValue(), y.intValue(), pressing);
    }

    public static void ping(WebSocket conn) {
        HashMap<String, Object> response = new HashMap<String, Object>();

        wsInstance.lastPing.replace(conn, System.currentTimeMillis());
        wsInstance.sendWS(conn, "205", response);
    }

    // server -> client
    public static void renderTick(HashMap<String, Player> players, List<Entity> entities) {
        if (entities.size() != previousEntities.size() || players.size() != previousPlayers.size()) {
            initializeEntities(players, entities);
        } else {
            updateEntities(players, entities);
        }
    }

    private static void updateEntities(HashMap<String, Player> players, List<Entity> entities) {
        HashMap<String, Object> update = new HashMap<String, Object>();
        for (Entity entity : entities) {
            Object prevVal = previousEntities.get(entity);
            if (prevVal == null) continue;

            String id = entity.id;
            Float x = entity.bounds().x;
            Float y = entity.bounds().y;
            switch (entity) {
                case Centipede e -> {
                    if (!(prevVal instanceof CentipedeValues previousValues)) {
                        ErrorNotifier.show("Something went wrong with parsing entity: Centipede");
                        continue;
                    }
                    CentipedeValues infoSet = new CentipedeValues(
                            null,
                            !x.equals(previousValues.x) ? x : null,
                            !y.equals(previousValues.y) ? y : null,
                            e.seg,
                            e.heading != previousValues.heading ? e.heading : null
                    );
                    update.put(id, infoSet);
                    updatePreviousEntity(infoSet, e);
                }
                case Enemy e -> {
                    if (!(prevVal instanceof EnemyValues previousValues)) {
                        ErrorNotifier.show("Something went wrong with parsing entity: Enemy");
                        continue;
                    }
                    EnemyValues infoSet = new EnemyValues(
                            null,
                            !x.equals(previousValues.x) ? x : null,
                            !y.equals(previousValues.y) ? y : null,
                            null
                    );
                    update.put(id, infoSet);
                    updatePreviousEntity(infoSet, e);
                }
                case Boss e -> {
                    if (!(prevVal instanceof BossValues previousValues)) {
                        ErrorNotifier.show("Something went wrong with parsing entity: Boss");
                        continue;
                    }
                    BossValues infoSet = new BossValues(
                            null,
                            !x.equals(previousValues.x) ? x : null,
                            !y.equals(previousValues.y) ? y : null,
                            null,
                            e.getTarget().bounds().x != previousValues.tx ? e.getTarget().bounds().x : null,
                            e.getTarget().bounds().y != previousValues.ty ? e.getTarget().bounds().y : null,
                            e.settled != previousValues.s ? e.settled : null,
                            e.fireTimer != previousValues.ft ? e.fireTimer : null,
                            e.phase != previousValues.ph ? e.phase : null,
                            e.atkTimer != previousValues.at ? e.atkTimer : null
                    );
                    update.put(id, infoSet);
                    updatePreviousEntity(infoSet, e);
                }
                case Bullet e -> {
                    if (!(prevVal instanceof BulletValues previousValues)) {
                        ErrorNotifier.show("Something went wrong with parsing entity: Bullet");
                        continue;
                    };
                    BulletValues infoSet = new BulletValues(
                            null,
                            !x.equals(previousValues.x) ? x : null,
                            !y.equals(previousValues.y) ? y : null,
                            null,
                            e.ang != previousValues.ang ? e.ang : null
                    );
                    update.put(id, infoSet);
                    updatePreviousEntity(infoSet, e);
                }
                case Laser e -> {
                    if (!(prevVal instanceof LaserValues previousValues)) {
                        ErrorNotifier.show("Something went wrong with parsing entity: Laser");
                        continue;
                    };
                    LaserValues infoSet = new LaserValues(
                            null,
                            !x.equals(previousValues.x) ? x : null,
                            !y.equals(previousValues.y) ? y : null,
                            e.telegraph != previousValues.tele ? e.telegraph : null
                    );
                    update.put(id, infoSet);
                    updatePreviousEntity(infoSet, e);
                }
                case Potion e -> {
                    if (!(prevVal instanceof PowerUpValues previousValues)) {
                        ErrorNotifier.show("Something went wrong with parsing entity: Potion");
                        continue;
                    }
                    PowerUpValues infoSet = new PowerUpValues(
                            null,
                            !x.equals(previousValues.x) ? x : null,
                            !y.equals(previousValues.y) ? y : null,
                            e.life != previousValues.life ? e.life : null
                    );
                    update.put(id, infoSet);
                    updatePreviousEntity(infoSet, e);
                }
                case Powerup1 e -> {
                    if (!(prevVal instanceof PowerUpValues previousValues)) {
                        ErrorNotifier.show("Something went wrong with parsing entity: Potion");
                        continue;
                    }
                    PowerUpValues infoSet = new PowerUpValues(
                            null,
                            !x.equals(previousValues.x) ? x : null,
                            !y.equals(previousValues.y) ? y : null,
                            e.life != previousValues.life ? e.life : null
                    );
                    update.put(id, infoSet);
                    updatePreviousEntity(infoSet, e);
                }
                default -> {
                    if (!(prevVal instanceof EntityValues previousValues)) {
                        ErrorNotifier.show("Something went wrong with parsing entity");
                        continue;
                    }
                    EntityValues infoSet = new EntityValues(
                            null,
                            !x.equals(previousValues.x) ? x : null,
                            !y.equals(previousValues.y) ? y : null
                    );
                    update.put(id, infoSet);
                    updatePreviousEntity(infoSet, entity);
                }
            }
        }
        for (Player p : players.values()) {
            PlayerValues previousValues = previousPlayers.get(p);
            String id = p.id;
            float x = p.bounds().x;
            float y = p.bounds().y;
            PlayerValues infoSet = new PlayerValues(
                    null,
                    x != previousValues.x ? x : null,
                    y != previousValues.y ? y : null,
                    !p.username.equals(previousValues.username) ? p.username : null,
                    p.dead() != previousValues.isdead ? p.dead() : null,
                    p.health != previousValues.hp ? p.health : null,
                    p.staminaFraction() != previousValues.stam ? p.staminaFraction() : null,
                    p.getShielded() != previousValues.shield ? p.getShielded() : null,
                    p.invulnerable != previousValues.inv ? p.invulnerable : null,
                    p.strafeInvuln != previousValues.sinv ? p.strafeInvuln : null,
                    p.stun != previousValues.stun ? p.stun : null
            );
            update.put(id, infoSet);
            updatePreviousPlayer(infoSet, p);
        }

        wsInstance.broadcastWS("251", update);
        wsInstance.deleteNotVerified();

    }

    private static void initializeEntities(HashMap<String, Player> players, List<Entity> entities) {
        HashMap<String, Object> init = new HashMap<String, Object>();
        for (Entity entity : entities) {
            String id = entity.id;
            String type = entity.name;
            float x = entity.bounds().x;
            float y = entity.bounds().y;
            switch (entity) {
                case Centipede e -> {
                    CentipedeValues infoSet = new CentipedeValues(
                            type,
                            x,
                            y,
                            e.seg,
                            e.heading
                    );
                    init.put(id, infoSet);
                    previousEntities.put(e, infoSet);
                }
                case Enemy e -> {
                    EnemyValues infoSet = new EnemyValues(
                            type,
                            x,
                            y,
                            e.kind.toString()
                    );
                    init.put(id, infoSet);
                    previousEntities.put(e, infoSet);
                }
                case Boss e -> {
                    BossValues infoSet = new BossValues(
                            type,
                            x,
                            y,
                            e.kind.toString(),
                            e.getTarget().bounds().x,
                            e.getTarget().bounds().y,
                            e.settled,
                            e.fireTimer,
                            e.phase,
                            e.atkTimer
                    );
                    init.put(id, infoSet);
                    previousEntities.put(e, infoSet);
                }
                case Bullet e -> {
                    BulletValues infoSet = new BulletValues(
                            type,
                            x,
                            y,
                            e.kind.toString(),
                            e.ang
                    );
                    init.put(id, infoSet);
                    previousEntities.put(e, infoSet);
                }
                case Laser e -> {
                    LaserValues infoSet = new LaserValues(
                            type,
                            x,
                            y,
                            e.telegraph
                    );
                    init.put(id, infoSet);
                    previousEntities.put(e, infoSet);
                }
                case Potion e -> {
                    PowerUpValues infoSet = new PowerUpValues(
                            type,
                            x,
                            y,
                            e.life
                    );
                    init.put(id, infoSet);
                    previousEntities.put(e, infoSet);
                }
                case Powerup1 e -> {
                    PowerUpValues infoSet = new PowerUpValues(
                            type,
                            x,
                            y,
                            e.life
                    );
                    init.put(id, infoSet);
                    previousEntities.put(e, infoSet);
                }
                default -> {
                    EntityValues infoSet = new EntityValues(
                            type,
                            x,
                            y
                    );
                    init.put(id, infoSet);
                    previousEntities.put(entity, infoSet);
                }
            }
        }
        for (Player p : players.values()) {
            String id = p.id;
            String type = p.name;
            float x = p.bounds().x;
            float y = p.bounds().y;
            PlayerValues infoSet = new PlayerValues(
                    type,
                    x,
                    y,
                    p.username,
                    p.dead(),
                    p.health,
                    p.staminaFraction(),
                    p.getShielded(),
                    p.invulnerable,
                    p.strafeInvuln,
                    p.stun
            );
            init.put(id, infoSet);
            previousPlayers.put(p, infoSet);
        }

        previousPlayers.keySet().retainAll(players.values());
        previousEntities.keySet().retainAll(entities);

        wsInstance.broadcastWS("250", init);
        wsInstance.deleteNotVerified();
    }

    private static void updatePreviousEntity(Object infoSet, Entity e) {
        if (!(infoSet instanceof Mergeable m)) return;
        previousEntities.put(e, m.merge(previousEntities.get(e)));
    }
    private static void updatePreviousPlayer(Object infoSet, Player p) {
        if (!(infoSet instanceof Mergeable m)) return;
        if (m.merge(previousPlayers.get(p)) instanceof PlayerValues merged)
            previousPlayers.put(p, merged);
    }

    public static void gameStarted(Difficulty difficulty, float tickrate) {
        String diff;
        switch (difficulty) {
            case EASY -> diff = "EASY";
            case NORMAL -> diff = "NORMAL";
            case HARD -> diff = "HARD";
            case HARDCORE -> diff = "HARDCORE";
            case CUSTOM -> diff = "CUSTOM";
            default -> {
                return;
            }
        }
        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("diff", diff);
        response.put("tps", tickrate);
        wsInstance.broadcastWS("210", response);
    }

    // help
    private static Boolean isNameLegal(String name) {
        if (name.chars().anyMatch(Character::isWhitespace)) return false;
        if (name.length()<3) return false;
        if (name.length()>20) return false;
        return true;
    }

    private interface Mergeable<T> {
        T merge(T previous);
    }

    public record EntityValues(
            String type,
            Float x,
            Float y
    ) implements Mergeable<EntityValues> {
        @Override
        public EntityValues merge(EntityValues previous) {
            return new EntityValues(
                    type != null ? type : previous.type,
                    x != null ? x : previous.x,
                    y != null ? y : previous.y
            );
        }
    }

    public record CentipedeValues(
            String type,
            Float x,
            Float y,
            Vector2[] seg,
            Float heading
    ) implements Mergeable<CentipedeValues> {
        @Override
        public CentipedeValues merge(CentipedeValues previous) {
            return new CentipedeValues(
                    type != null ? type : previous.type,
                    x != null ? x : previous.x,
                    y != null ? y : previous.y,
                    seg != null ? seg : previous.seg,
                    heading != null ? heading : previous.heading
            );
        }
    }

    public record EnemyValues(
            String type,
            Float x,
            Float y,
            String kind
    ) implements Mergeable<EnemyValues> {
        @Override
        public EnemyValues merge(EnemyValues previous) {
            return new EnemyValues(
                    type != null ? type : previous.type,
                    x != null ? x : previous.x,
                    y != null ? y : previous.y,
                    kind != null ? kind : previous.kind
            );
        }
    }

    public record BossValues(
            String type,
            Float x,
            Float y,
            String kind,
            Float tx,
            Float ty,
            Boolean s,
            Float ft,
            Integer ph,
            Float at
    ) implements Mergeable<BossValues> {
        @Override
        public BossValues merge(BossValues previous) {
            return new BossValues (
                    type != null ? type : previous.type,
                    x != null ? x : previous.x,
                    y != null ? y : previous.y,
                    kind != null ? kind : previous.kind,
                    tx != null ? tx : previous.tx,
                    ty != null ? ty : previous.ty,
                    s != null ? s : previous.s,
                    ft != null ? ft : previous.ft,
                    ph != null ? ph : previous.ph,
                    at != null ? at : previous.at
            );
        }
    }

    public record BulletValues(
            String type,
            Float x,
            Float y,
            String kind,
            Float ang
    ) implements Mergeable<BulletValues> {
        @Override
        public BulletValues merge(BulletValues previous) {
            return new BulletValues(
                    type != null ? type : previous.type,
                    x != null ? x : previous.x,
                    y != null ? y : previous.y,
                    kind != null ? kind : previous.kind,
                    ang != null ? ang : previous.ang
            );
        }
    }

    public record LaserValues(
            String type,
            Float x,
            Float y,
            Float tele
    ) implements Mergeable<LaserValues> {
        @Override
        public LaserValues merge(LaserValues previous) {
            return new LaserValues(
                    type != null ? type : previous.type,
                    x != null ? x : previous.x,
                    y != null ? y : previous.y,
                    tele != null ? tele : previous.tele
            );
        }
    }

    public record PowerUpValues(
            String type,
            Float x,
            Float y,
            Float life
    ) implements Mergeable<PowerUpValues> {
        @Override
        public PowerUpValues merge(PowerUpValues previous) {
            return new PowerUpValues(
                    type != null ? type : previous.type,
                    x != null ? x : previous.x,
                    y != null ? y : previous.y,
                    life != null ? life : previous.life
            );
        }
    }

    public record PlayerValues(
            String type,
            Float x,
            Float y,
            String username,
            Boolean isdead,
            Float hp,
            Float stam,
            Boolean shield,
            Boolean inv,
            Float sinv,
            Float stun
    ) implements Mergeable<PlayerValues> {
        @Override
        public PlayerValues merge(PlayerValues previous) {
            return new PlayerValues(
                    type != null ? type : previous.type,
                    x != null ? x : previous.x,
                    y != null ? y : previous.y,
                    username != null ? username : previous.username,
                    isdead != null ? isdead : previous.isdead,
                    hp != null ? hp : previous.hp,
                    stam != null ? stam : previous.stam,
                    shield != null ? shield : previous.shield,
                    inv != null ? inv : previous.inv,
                    sinv != null ? sinv : previous.sinv,
                    stun != null ? stun : previous.stun
            );
        }
    }

    public static void dispose() {
        users.clear();
    }
}
