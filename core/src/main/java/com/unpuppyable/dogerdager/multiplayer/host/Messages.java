package com.unpuppyable.dogerdager.multiplayer.host;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.unpuppyable.dogerdager.Difficulty;
import com.unpuppyable.dogerdager.DogerDager;
import com.unpuppyable.dogerdager.ErrorNotifier;
import com.unpuppyable.dogerdager.MultiplayerScreen;
import com.unpuppyable.dogerdager.entity.*;
import com.unpuppyable.dogerdager.multiplayer.MessageType;
import org.java_websocket.WebSocket;

import java.util.*;

public class Messages {
    private static Websocket wsInstance = Websocket.getInstance();
    public static void reloadWsInstance() { wsInstance = Websocket.getInstance(); }

    private final static HashMap<Player, PlayerValues> previousPlayers = new HashMap<Player, PlayerValues>();
    private final static HashMap<Entity, Object> previousEntities = new HashMap<Entity, Object>();

    // client -> server
    public static void authorize(WebSocket conn, Map<String, Object> msg) {
        HashMap<String, Object> response = new HashMap<String, Object>();
        if (DogerDager.multiplayerGameStarted) return;
        if (wsInstance.getUserName(conn) != null) {
            response.put("success", false);
            response.put("message", "you are already logged in");
            wsInstance.sendWS(conn, MessageType.AUTHORIZE_RESPONSE.code, response);
            return;
        }
        if (!msg.containsKey("user")) return;
        String name = msg.get("user")+"";
        if (wsInstance.isUserLoggedIn(name)) {
            response.put("success", false);
            response.put("message", "name already taken");
            wsInstance.sendWS(conn, MessageType.AUTHORIZE_RESPONSE.code, response);
            return;
        }
        if (!isNameLegal(name)) {
            response.put("success", false);
            response.put("message", "illegal name");
            wsInstance.sendWS(conn, MessageType.AUTHORIZE_RESPONSE.code, response);
            return;
        }
        //broadcast user joined
        response.put("user", name);
        wsInstance.broadcastWS(MessageType.USER_JOINED.code, response);
        wsInstance.markLoggedIn(conn, name);
        MultiplayerScreen.addToUserList(name);
        //responding with success
        response.clear();
        response.put("success", true);
        response.put("message", "you have logged in");
        response.put("users", wsInstance.getUserList());
        wsInstance.sendWS(conn, MessageType.AUTHORIZE_RESPONSE.code, response);

    }

    public static void keyDown(WebSocket conn, Map<String, Object> msg) {
        String name = wsInstance.getUserName(conn);
        if (!msg.containsKey("keys")) return;
        if (!(msg.get("keys") instanceof List<?> keys)) return;
        for (var key : keys) {
            if (!(key instanceof String k)) return;
            Player player = HostPlayScreen.getPlayerByName(name);
            player.keyDown(k);
        }
    }

    public static void keyUp(WebSocket conn, Map<String, Object> msg) {
        String name = wsInstance.getUserName(conn);
        if (!msg.containsKey("keys")) return;
        if (!(msg.get("keys") instanceof List<?> keys)) return;
        for (var key : keys) {
            if (!(key instanceof String k)) return;
            Player player = HostPlayScreen.getPlayerByName(name);
            player.keyUp(k);
        }
    }

    public static void shoot(WebSocket conn, Map<String, Object> msg) {
        String name = wsInstance.getUserName(conn);
        if (!msg.containsKey("x") || !msg.containsKey("y") || !msg.containsKey("pressing")) return;
        if (!(msg.get("x") instanceof Double x)) return;
        if (!(msg.get("y") instanceof Double y)) return;
        if (!(msg.get("pressing") instanceof Boolean pressing)) return;
        Player player = HostPlayScreen.getPlayerByName(name);
        if (player == null) return;
        Gdx.app.postRunnable(() -> player.shoot(x.intValue(), y.intValue(), pressing));
    }

    public static void ping(WebSocket conn) {
        HashMap<String, Object> response = new HashMap<String, Object>();

        wsInstance.pingFromUser(conn);
        wsInstance.sendWS(conn, MessageType.PONG.code, response);
    }

    // server -> client
    public static void renderTick(HashMap<String, Player> players, List<Entity> entities) {
        if (entities.size() != previousEntities.size() || players.size() != previousPlayers.size() || !previousEntities.keySet().containsAll(entities)) {
            initializeEntities(players, entities);
        } else {
            updateEntities(players, entities);
        }
    }

    private static void initializeEntities(HashMap<String, Player> players, List<Entity> entities) {
        HashMap<String, Object> init = new HashMap<String, Object>();
        for (Entity entity : entities) {
            Object infoSet = valuesFor(entity, null);
            init.put(entity.id, infoSet);
            previousEntities.put(entity, infoSet);
        }
        for (Player p : players.values()) {
            Object infoSet = playerValues(p, null);
            init.put(p.id, infoSet);
            previousPlayers.put(p, (PlayerValues) infoSet);
        }
        previousPlayers.keySet().retainAll(players.values());
        previousEntities.keySet().retainAll(entities);

        wsInstance.broadcastWS(MessageType.ENTITIES_INIT.code, init);
        wsInstance.deleteNotVerified();
    }

    private static void updateEntities(HashMap<String, Player> players, List<Entity> entities) {
        HashMap<String, Object> update = new HashMap<String, Object>();
        for (Entity entity : entities) {
            Object infoSet = valuesFor(entity, previousEntities.get(entity));
            update.put(entity.id, infoSet);
            updatePreviousEntity(infoSet, entity);
        }
        for (Player p : players.values()) {
            Object infoSet = playerValues(p, previousPlayers.get(p));
            update.put(p.id, infoSet);
            updatePreviousPlayer(infoSet, p);
        }

        wsInstance.broadcastWS(MessageType.ENTITIES_UPDATE.code, update);
        wsInstance.deleteNotVerified();
    }

    private static Object valuesFor(Entity entity, Object previous) {
        return switch (entity) {
            case Centipede e -> centipedeValues(e, (CentipedeValues) previous);
            case Enemy e -> enemyValues(e, (EnemyValues) previous);
            case Boss e -> bossValues(e, (BossValues) previous);
            case Bullet e -> bulletValues(e, (BulletValues) previous);
            case Laser e -> laserValues(e, (LaserValues) previous);
            case Powerup1 e -> powerUpValues(e, (PowerUpValues) previous);
            case Potion e -> powerUpValues(e, (PowerUpValues) previous);
            default -> entityValues(entity, (EntityValues) previous);
        };
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
        wsInstance.broadcastWS(MessageType.GAME_STARTED.code, response);
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

    private static Object entityValues(Entity entity, EntityValues previous) {
        float x = entity.bounds().x;
        float y = entity.bounds().y;
        if (previous == null) {
            return new EntityValues(
                    entity.name,
                    x,
                    y
            );
        }
        return new EntityValues(
                null,
                x != previous.x ? x : null,
                y != previous.y ? y : null
        );
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

    private static Object centipedeValues(Centipede entity, CentipedeValues previous) {
        float x = entity.bounds().x;
        float y = entity.bounds().y;
        if (previous == null) {
            return new CentipedeValues(
                    entity.name,
                    x,
                    y,
                    entity.seg,
                    entity.heading
            );
        }
        return new CentipedeValues(
                null,
                x != previous.x ? x : null,
                y != previous.y ? y : null,
                entity.seg,
                entity.heading != previous.heading ? entity.heading : null
        );
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

    private static Object enemyValues(Enemy entity, EnemyValues previous) {
        float x = entity.bounds().x;
        float y = entity.bounds().y;
        if (previous == null) {
            return new EnemyValues(
                    entity.name,
                    x,
                    y,
                    entity.kind.toString()
            );
        }
        return new EnemyValues(
                null,
                x != previous.x ? x : null,
                y != previous.y ? y : null,
                null
        );
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

    private static Object bossValues(Boss entity, BossValues previous) {
        float x = entity.bounds().x;
        float y = entity.bounds().y;
        if (previous == null) {
            return new BossValues(
                    entity.name,
                    x,
                    y,
                    entity.kind.toString(),
                    entity.getTarget().bounds().x,
                    entity.getTarget().bounds().y,
                    entity.settled,
                    entity.fireTimer,
                    entity.phase,
                    entity.atkTimer
            );
        }
        return new BossValues(
                null,
                x != previous.x ? x : null,
                y != previous.y ? y : null,
                null,
                entity.getTarget().bounds().x != previous.tx ? entity.getTarget().bounds().x : null,
                entity.getTarget().bounds().y != previous.ty ? entity.getTarget().bounds().y : null,
                entity.settled != previous.s ? entity.settled : null,
                entity.fireTimer != previous.ft ? entity.fireTimer : null,
                entity.phase != previous.ph ? entity.phase : null,
                entity.atkTimer != previous.at ? entity.atkTimer : null
        );
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

    private static Object bulletValues(Bullet entity, BulletValues previous) {
        float x = entity.bounds().x;
        float y = entity.bounds().y;
        if (previous == null) {
            return new BulletValues(
                    entity.name,
                    x,
                    y,
                    entity.kind.toString(),
                    entity.ang
            );
        }
        return new BulletValues(
                null,
                x != previous.x ? x : null,
                y != previous.y ? y : null,
                null,
                entity.ang != previous.ang ? entity.ang : null
        );
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

    private static Object laserValues(Laser entity, LaserValues previous) {
        float x = entity.bounds().x;
        float y = entity.bounds().y;
        if (previous == null) {
            return new LaserValues(
                    entity.name,
                    x,
                    y,
                    entity.telegraph
            );
        }
        return new LaserValues(
                null,
                x != previous.x ? x : null,
                y != previous.y ? y : null,
                entity.telegraph != previous.tele ? entity.telegraph : null
        );
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

    private static Object powerUpValues(Powerup1 entity, PowerUpValues previous) {
        float x = entity.bounds().x;
        float y = entity.bounds().y;
        if (previous == null) {
            return new PowerUpValues(
                    entity.name,
                    x,
                    y,
                    entity.life
            );
        }
        return new PowerUpValues(
                null,
                x != previous.x ? x : null,
                y != previous.y ? y : null,
                entity.life != previous.life ? entity.life : null
        );
    }
    private static Object powerUpValues(Potion entity, PowerUpValues previous) {
        float x = entity.bounds().x;
        float y = entity.bounds().y;
        if (previous == null) {
            return new PowerUpValues(
                    entity.name,
                    x,
                    y,
                    entity.life
            );
        }
        return new PowerUpValues(
                null,
                x != previous.x ? x : null,
                y != previous.y ? y : null,
                entity.life != previous.life ? entity.life : null
        );
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

    private static Object playerValues(Player entity, PlayerValues previous) {
        float x = entity.bounds().x;
        float y = entity.bounds().y;
        if (previous == null) {
            return new PlayerValues(
                    entity.name,
                    x,
                    y,
                    entity.username,
                    entity.dead(),
                    entity.health,
                    entity.staminaFraction(),
                    entity.getShielded(),
                    entity.invulnerable,
                    entity.strafeInvuln,
                    entity.stun
            );
        }
        return new PlayerValues(
                null,
                x != previous.x ? x : null,
                y != previous.y ? y : null,
                null,
                entity.dead() != previous.isdead ? entity.dead() : null,
                entity.health != previous.hp ? entity.health : null,
                entity.staminaFraction() != previous.stam ? entity.staminaFraction() : null,
                entity.getShielded() != previous.shield ? entity.getShielded() : null,
                entity.invulnerable != previous.inv ? entity.invulnerable : null,
                entity.strafeInvuln != previous.sinv ? entity.strafeInvuln : null,
                entity.stun != previous.stun ? entity.stun : null
        );
    }

    public static void dispose() {
        previousEntities.clear();
        previousPlayers.clear();
        wsInstance = null;
    }
}
