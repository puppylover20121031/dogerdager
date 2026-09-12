package com.unpuppyable.dogerdager.multiplayer.client;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.unpuppyable.dogerdager.Difficulty;
import com.unpuppyable.dogerdager.DogerDager;
import com.unpuppyable.dogerdager.ErrorNotifier;
import com.unpuppyable.dogerdager.multiplayer.ErrorLogs;
import com.unpuppyable.dogerdager.MultiplayerScreen;

import java.util.*;

public class ClientMessages {
    private static WebsocketClient instance = WebsocketClient.getClientInstance();
    private static ClientPlayScreen screenInstance;
    // Server -> client
    public static void authorizeResponse(Map<String, Object> response) {
        //getters
        if (!response.containsKey("success")) return;
        if (!(response.get("success") instanceof Boolean success)) return;
        if (!response.containsKey("message")) return;
        if (!(response.get("message") instanceof String message)) return;
        if (!response.containsKey("users")) return;
        if (!(response.get("users") instanceof List<?> users)) return;
        //logic
        if (!success) {
            ErrorNotifier.show(message);
            return;
        }
        instance.verified = true;
        for (var user : users) {
            if (!(user instanceof String p)) continue;
            instance.players.add(p);
        }
        MultiplayerScreen.onClientVerified();
    }

    public static void newUser(Map<String, Object> message) {
        if (!message.containsKey("user")) return;
        if (!(message.get("user") instanceof String newUser)) return;
        instance.players.add(newUser);
        MultiplayerScreen.addToUserList(newUser);
    }

    public static void userLoggedOut(Map<String, Object> message) {
        if (!message.containsKey("user")) return;
        if (!(message.get("user") instanceof String loggedOut)) return;
        instance.players.remove(loggedOut);
        MultiplayerScreen.deleteFromUserList(loggedOut);
    }

    public static void gameStarted(Map<String, Object> message) {
        if (!(message.get("diff") instanceof String diff)) return;
        Difficulty difficulty;
        switch (diff) {
            case "EASY" -> {
                difficulty = Difficulty.EASY;
            }
            case "NORMAL" -> {
                difficulty = Difficulty.NORMAL;
            }
            case "HARD" -> {
                difficulty = Difficulty.HARD;
            }
            case "HARDCORE" -> {
                difficulty = Difficulty.HARDCORE;
            }
            case "CUSTOM" -> {
                difficulty = Difficulty.CUSTOM;
            }
            default -> {
                return;
            }
        }
        Gdx.app.postRunnable(() -> {
            DogerDager game = DogerDager.getGameInstance();
            DogerDager.multiplayerGameStarted = true;
            DogerDager.instance.setScreen(new ClientPlayScreen(game, game.post, difficulty, instance.playerName));
            screenInstance = ClientPlayScreen.getInstance();
        });

    }

    public static void newEntityStates(Map<String, Object> message) {
        HashSet<Object> entities = validateServerTick(message);
        if (entities == null) return;

        if (screenInstance == null) {
            screenInstance = ClientPlayScreen.getInstance();
            return;
        }
        Gdx.app.postRunnable(() -> screenInstance.newEntityStates(entities));
    }

    public static void updateEntityStates(Map<String, Object> message) {
        HashSet<Object> entities = validateServerTick(message);
        if (entities == null) return;

        if (screenInstance == null) {
            screenInstance = ClientPlayScreen.getInstance();
            return;
        }

        Gdx.app.postRunnable(() -> {
            try {
                screenInstance.updateEntityStates(entities);
            } catch (Exception ex) {
                ErrorNotifier.show("unexpected error, more info in logs");
                ErrorLogs.write("Unexpected exception: " + ex);
            }
        });
    }

    private static HashSet<Object> validateServerTick(Map<String, Object> message) {
        try {
            HashSet<Object> entities = new HashSet<Object>();
            for (String key : message.keySet()) {
                if (key.equals("t")) continue;
                Object value = message.get(key);
                if (!(value instanceof Map<?, ?> entityMap)) continue;
                Float x = entityMap.get("x") instanceof Double d ? d.floatValue() : null;
                Float y = entityMap.get("y") instanceof Double d ? d.floatValue() : null;
                String type = entityMap.get("type") instanceof String u ? u : null;
                //player
                String username = entityMap.get("username") instanceof String u ? u : null;
                Boolean isDead = entityMap.get("isdead") instanceof Boolean b ? b : false;
                Float health = entityMap.get("hp") instanceof Double d ? d.floatValue() : null;
                Float stamina = entityMap.get("stam") instanceof Double d ? d.floatValue() : null;
                Boolean shielded = entityMap.get("shield") instanceof Boolean b ? b : null;
                Boolean invulnerable = entityMap.get("inv") instanceof Boolean b ? b : null;
                Float strafeInvuln = entityMap.get("sinv") instanceof Double d ? d.floatValue() : null;
                Float stun = entityMap.get("stun") instanceof Double d ? d.floatValue() : null;
                //centipede
                Vector2[] seg;
                if (entityMap.get("seg") == null) {
                    seg = null;
                } else {
                    List<Map<String, Double>>segs = (List<Map<String, Double>>) entityMap.get("seg");
                    seg = new Vector2[segs.size()];
                    for (int i = 0; i < segs.size(); i++) {
                        seg[i] = new Vector2(segs.get(i).get("x").floatValue(), segs.get(i).get("y").floatValue());
                    }
                }
                Float heading = entityMap.get("heading") instanceof Double d ? d.floatValue() : null;
                String kind = entityMap.get("kind") instanceof String u ? u : null;
                Float ang = entityMap.get("ang") instanceof Double d ? d.floatValue() : null;
                Float telegraph = entityMap.get("tele") instanceof Double d ? d.floatValue() : null;
                Float targetX = entityMap.get("tx") instanceof Double d ? d.floatValue() : null;
                Float targetY = entityMap.get("ty") instanceof Double d ? d.floatValue() : null;
                Boolean settled = entityMap.get("s") instanceof Boolean b ? b : false;
                Float fireTimer = entityMap.get("ft") instanceof Double d ? d.floatValue() : null;
                Integer phase = entityMap.get("ph") instanceof Double d ? d.intValue() : null;
                Float atkTimer = entityMap.get("at") instanceof Double d ? d.floatValue() : null;
                Float life = entityMap.get("life") instanceof Double d ? d.floatValue() : null;

                entities.add(new ClientPlayScreen.EntityState(
                        key,
                        type,
                        x,
                        y,
                        username,
                        isDead,
                        stamina,
                        shielded,
                        invulnerable,
                        strafeInvuln,
                        stun,
                        health,
                        seg,
                        heading,
                        kind,
                        ang,
                        telegraph,
                        targetX,
                        targetY,
                        settled,
                        fireTimer,
                        phase,
                        atkTimer,
                        life
                ));
            }
            return entities;
        } catch (ClassCastException | NullPointerException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Client -> server

    //type 0
    public static void authorize(String name) {
        if (instance.verified || !instance.isOpen()) return;
        HashMap<String, Object> message = new HashMap<String, Object>();
        message.put("user", name);
        instance.sendWS("0", message);
    }

    //type 1
    public static void keysDown(HashSet<String> keys) {
        if (!instance.verified || !instance.isOpen()) return;
        if (!DogerDager.multiplayerGameStarted) return;
        HashMap<String, Object> message = new HashMap<String, Object>();
        message.put("keys", keys);
        instance.sendWS("1", message);
    }

    //type 2
    public static void keysUp(HashSet<String> keys) {
        if (!instance.verified || !instance.isOpen()) return;
        if (!DogerDager.multiplayerGameStarted) return;
        HashMap<String, Object> message = new HashMap<String, Object>();
        message.put("keys", keys);
        instance.sendWS("2", message);
    }

    //type 3
    public static void shoot(int x, int y, boolean isPressed) {
        if (!instance.verified || !instance.isOpen()) return;
        HashMap<String, Object> message = new HashMap<String, Object>();
        message.put("x", x);
        message.put("y", y);
        message.put("pressing", isPressed);
        instance.sendWS("3", message);
    }

    //type 5
    public static void sendPing() {
        if (!instance.verified || !instance.isOpen()) return;
        HashMap<String, Object> message = new HashMap<String, Object>();
        instance.sendWS("5", message);
    }
}
