package com.unpuppyable.dogerdager.multiplayer.host;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;
import com.unpuppyable.dogerdager.Difficulty;
import com.unpuppyable.dogerdager.DogerDager;
import com.unpuppyable.dogerdager.MultiplayerScreen;
import com.unpuppyable.dogerdager.entity.*;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.unpuppyable.dogerdager.multiplayer.host.Websocket.*;

public class Messages {
    private static Websocket wsInstance = getInstance();
    private static HashMap<WebSocket, String> users = wsInstance.users;
    private final static Preferences prefs = Gdx.app.getPreferences("doger-dager");

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
        //some listing bs
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
        HostPlayScreen.getInstance().shootPlayer(player, x.intValue(), y.intValue(), pressing);
    }

    public static void ping(WebSocket conn) {
        HashMap<String, Object> response = new HashMap<String, Object>();

        wsInstance.lastPing.replace(conn, System.currentTimeMillis());
        wsInstance.sendWS(conn, "205", response);
    }

    // server -> client
    public static void stateUpdate(HashMap<String, Player> players, List<Entity> entities) {
        HashMap<String, Object> stateUpdate = new HashMap<String, Object>();
        for (Entity entity : entities) {
            String id = entity.id;
            float x = entity.bounds().x;
            float y = entity.bounds().y;
            if (entity instanceof Centipede e) {
                stateUpdate.put(id, new CentipedeValues(
                        e.name,
                        x,
                        y,
                        e.seg,
                        e.heading
                ));
            } else if (entity instanceof Enemy e) {
                stateUpdate.put(id, new EnemyValues(
                        e.name,
                        x,
                        y,
                        e.kind.toString()
                ));
            } else if (entity instanceof Boss e) {
                stateUpdate.put(id, new BossValues(
                        e.name,
                        x,
                        y,
                        e.kind.toString()
                        //more thingies
                ));
            } else if (entity instanceof Bullet e) {
                stateUpdate.put(id, new BulletValues(
                        e.name,
                        x,
                        y,
                        e.kind.toString(),
                        e.ang
                ));
            } else if (entity instanceof Laser e) {
                stateUpdate.put(id, new LaserValues(
                        e.name,
                        x,
                        y,
                        e.telegraph
                ));
            } else {
                stateUpdate.put(id, new EntityValues(
                        entity.name,
                        x,
                        y
                ));
            }

        }
        for (Player p : players.values()) {
            String id = p.id;
            float x = p.bounds().x;
            float y = p.bounds().y;
            stateUpdate.put(id, new PlayerValues(p.name,
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
            ));
        }
        wsInstance.broadcastWS("250", stateUpdate);
        wsInstance.deleteNotVerified();
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

    public record EntityValues(
            String type,
            float x,
            float y
    ) {}

    public record CentipedeValues(
            String type,
            float x,
            float y,
            Vector2[] seg,
            Float heading
    ) {}

    public record EnemyValues(
            String type,
            float x,
            float y,
            String kind
    ) {}

    public record BossValues(
            String type,
            float x,
            float y,
            String kind
            // more thingies
    ) {}

    public record BulletValues(
            String type,
            float x,
            float y,
            String kind,
            Float ang
    ) {}

    public record LaserValues(
            String type,
            float x,
            float y,
            Float tele
    ) {}

    public record PlayerValues(
            String type,
            float x,
            float y,
            String username,
            boolean isdead,
            float hp,
            float stam,
            boolean shield,
            boolean inv,
            float sinv,
            float stun
    ) {}
}
