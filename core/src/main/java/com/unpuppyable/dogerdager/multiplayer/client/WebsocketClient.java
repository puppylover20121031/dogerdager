package com.unpuppyable.dogerdager.multiplayer.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.unpuppyable.dogerdager.DogerDager;
import com.unpuppyable.dogerdager.ErrorNotifier;
import com.unpuppyable.dogerdager.multiplayer.ErrorLogs;
import com.unpuppyable.dogerdager.multiplayer.Schedulers;
import com.unpuppyable.dogerdager.multiplayer.MessageType;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class WebsocketClient extends WebSocketClient {

    private static WebsocketClient instance;
    private final String playerName;
    private Gson gson = new Gson();
    private boolean verified;
    private Collection<String> players;

    public static WebsocketClient startClient(String URI, String yourName) throws InterruptedException {
        URI uri;
        try {
            uri = new URI("ws://"+URI);
        } catch (URISyntaxException e) {
            return null;
        }
        CountDownLatch latch = new CountDownLatch(1);
        Thread t = new Thread(() ->
        {
            WebSocketClient client = new WebsocketClient(uri, yourName);
            latch.countDown();
            client.run();
        });
        t.setDaemon(true);
        t.start();
        latch.await();
        Schedulers.sendOutPingMessage(5000);
        return getInstance();
    }

    public WebsocketClient(URI uri, String yourName) {
        super(uri);
        this.players = new HashSet<>();
        this.playerName = yourName;
        instance = this;
        Schedulers.reloadClientInstance();
        verified = false;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        ClientMessages.authorize(playerName);
    }

    @Override
    public void onMessage(String message) {
        if (message == null) return;
        Map<String, Object> msg;
        try {
            msg = gson.fromJson(message, Map.class);
        } catch (JsonSyntaxException e) {
            return;
        }
        MessageType type = MessageType.fromCode(msg.get("t").toString());
        if (type == null) return;
        // authorize
        switch (type) {
             case AUTHORIZE_RESPONSE -> ClientMessages.authorizeResponse(msg);
             case ERROR -> {
                 ErrorNotifier.show("Error received from server");
                 ErrorLogs.write("Error received from server: " + msg);
             }
             default -> {}
        }

        if (!verified) return;
        switch (type) {
            case USER_JOINED -> ClientMessages.newUser(msg);
            case USER_LEFT -> ClientMessages.userLoggedOut(msg);
            case GAME_STARTED -> ClientMessages.gameStarted(msg);
            default -> {}
        }

        if (!DogerDager.multiplayerGameStarted) return;

        switch (type) {
            case ENTITIES_INIT -> ClientMessages.newEntityStates(msg);
            case ENTITIES_UPDATE -> ClientMessages.updateEntityStates(msg);
            default -> {}
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        verified = false;
        DogerDager.setMultiplayer(false);
        ErrorNotifier.show("Websocket client closed, error code: "+code);
        Schedulers.stopSchedulers();
    }

    @Override
    public void onError(Exception ex) {
        ErrorNotifier.show("Encountered an error (see multiplayer logs)");
        ErrorLogs.write("Error in ClientWebSocket: " + ex);
    }

    public void sendWS(String type, Map<String, Object> message) {
        Map<String, Object> json = new HashMap<>(message);
        json.put("t", type);
        instance.send(gson.toJson(json));
    }

    public static WebsocketClient getInstance() {
        return instance;
    }

    public boolean isVerified() { return verified; }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void addPlayer(String name) { players.add(name); }
    public void removePlayer(String name) { players.remove(name); }
    public String getClientName() { return playerName; }

    public List<String> getPlayerList() { return new ArrayList<String>(players); }


    public static void closeClient() {
        if (instance == null || instance.isClosed() || instance.isClosing()) return;
        instance.close();
    }

    public static void dispose() {
        closeClient();
        if (instance != null) instance.verified = false;
        instance = null;
        DogerDager.setMultiplayer(false);
        Schedulers.stopSchedulers();
        ClientMessages.dispose();
    }
}
