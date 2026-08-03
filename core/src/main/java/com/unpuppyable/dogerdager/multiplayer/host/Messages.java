package com.unpuppyable.dogerdager.multiplayer.host;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;
import com.unpuppyable.dogerdager.DogerDager;
import com.unpuppyable.dogerdager.MultiplayerScreen;
import com.unpuppyable.dogerdager.entity.Centipede;
import com.unpuppyable.dogerdager.entity.Entity;
import com.unpuppyable.dogerdager.entity.Player;
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
        if (!(msg.get("x") instanceof Integer x)) return;
        if (!(msg.get("y") instanceof Integer y)) return;
        if (!(msg.get("pressing") instanceof Boolean pressing)) return;
        Player player = HostPlayScreen.getPlayerByName(name);
        HostPlayScreen.getInstance().shootPlayer(player, x, y, pressing);
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
                stateUpdate.put(id, new EntityValues(
                        e.name,
                        x,
                        y,
                        e.seg,
                        e.heading
                ));
            } else {
                stateUpdate.put(id, new EntityValues(
                        entity.name,
                        x,
                        y,
                        null,
                        null
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
                    p.stamina,
                    p.getShielded(),
                    p.invulnerable,
                    p.strafeInvuln,
                    p.stun
            ));
        }
        wsInstance.broadcastWS("250", stateUpdate);
        wsInstance.deleteNotVerified();
    }

    public static void gameStarted() {
        HashMap<String, Object> response = new HashMap<String, Object>();
        wsInstance.broadcastWS("210", response);
    }

    public static void playerHurt(String name, float amount) {
        HashMap<String, Object> response = new HashMap<String, Object>();
        WebSocket conn = wsInstance.nameToConn(name);
        response.put("dmg", amount);
        wsInstance.sendWS(conn, "251", response);
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
            float y,
            Vector2[] seg,
            Float heading
    ) {}

    public record PlayerValues(
            String type,
            float x,
            float y,
            String username,
            boolean isdead,
            float stam,
            boolean shield,
            boolean inv,
            float sinv,
            float stun
    ) {}
}
