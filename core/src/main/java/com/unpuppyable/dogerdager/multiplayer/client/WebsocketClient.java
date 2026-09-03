package com.unpuppyable.dogerdager.multiplayer.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.unpuppyable.dogerdager.DogerDager;
import com.unpuppyable.dogerdager.multiplayer.Schedulers;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class WebsocketClient extends WebSocketClient {
    private static WebsocketClient instance;
    public String playerName;
    private Gson gson = new Gson();
    public boolean verified;
    public Collection<String> players;

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
        return getClientInstance();
    }

    public WebsocketClient(URI uri, String yourName) {
        super(uri);
        this.players = new HashSet<>();
        this.playerName = yourName;
        instance = this;
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
        if (!msg.containsKey("t")) return;
        // authorize
        if (msg.get("t").equals("200")) {
            ClientMessages.authorizeResponse(msg);
            return;
        }

        if (!verified) return;
        // new user joined
        if (msg.get("t").equals("201")) {
            ClientMessages.newUser(msg);
            return;
        }
        //user logged out
        if (msg.get("t").equals("202")) {
            ClientMessages.userLoggedOut(msg);
            return;
        }
        //game started
        if (msg.get("t").equals("210")) {
            ClientMessages.gameStarted(msg);
            return;
        }
        if (!DogerDager.multiplayerGameStarted) return;
        //entity states
        if (msg.get("t").equals("250")) {
            ClientMessages.newEntityStates(msg);
            return;
        }
        if (msg.get("t").equals("251")) {
            ClientMessages.updateEntityStates(msg);
            return;
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }
    public void sendWS(String type, Map<String, Object> message) {
        Map<String, Object> json = new HashMap<>(message);
        json.put("t", type);
        instance.send(gson.toJson(json));
    }
    public static WebsocketClient getClientInstance() {
        return instance;
    }

    public static void closeClient() {
        if (instance == null || instance.isClosed() || instance.isClosing()) return;
        instance.close();
    }

    public static void dispose() {
        closeClient();
        if (instance != null) instance.verified = false;
        DogerDager.setMultiplayer(false);
        Schedulers.stopSchedulers();
    }
}
