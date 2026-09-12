package com.unpuppyable.dogerdager.multiplayer.host;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.unpuppyable.dogerdager.DogerDager;
import com.unpuppyable.dogerdager.ErrorNotifier;
import com.unpuppyable.dogerdager.MultiplayerScreen;
import com.unpuppyable.dogerdager.multiplayer.ErrorLogs;
import com.unpuppyable.dogerdager.multiplayer.MessageType;
import com.unpuppyable.dogerdager.multiplayer.Schedulers;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CountDownLatch;


public class Websocket extends WebSocketServer {
    private static Websocket instance;
    private static Gson gson;

    private final HashMap<WebSocket, String> users = new HashMap<WebSocket, String>(); //logged-in users map;
    private final HashMap<WebSocket, Long> notVerified = new HashMap<WebSocket, Long>();
    private final HashMap<WebSocket, Long> lastPing = new HashMap<WebSocket, Long>();

    public Websocket(InetSocketAddress address) {
        super(address); //address ws
        instance = this;
        Messages.reloadWsInstance();
        Schedulers.reloadWsInstance();
        gson = new Gson();
        onAppStop();
    }
    public static Websocket startServer(int port) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Thread t = new Thread(() ->
        {
            WebSocketServer server = new Websocket(new InetSocketAddress(port));
            latch.countDown();
            server.run();
        });
        t.setDaemon(true);
        t.start();
        latch.await();
        Schedulers.pingStateScheduler(30000);
        Schedulers.notVerifiedScheduler(60000);
        return getInstance();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (DogerDager.multiplayerGameStarted) {
            logOffUser(conn, 1000, "Game has already started.");
            return;
        }
        notVerified.put(conn, System.currentTimeMillis());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        notVerified.remove(conn);
        //broadcast
        if (!users.containsKey(conn)) return;
        String name = users.get(conn);
        users.remove(conn);
        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("user", name);
        broadcastWS(MessageType.USER_LEFT.code, response);
        //output into userlist if game didn't start yet
        if (!DogerDager.multiplayerGameStarted) MultiplayerScreen.deleteFromUserList(name);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (message == null) return;
        Map<String, Object> msg;
        try {
            msg = gson.fromJson(message, Map.class);
        } catch (JsonSyntaxException e) {
            return;
        }
        MessageType type = MessageType.fromCode(msg.get("t").toString());
        if (type == null) return;

        if (type == MessageType.AUTHORIZE) {
            Messages.authorize(conn, msg);
            return;
        }
        if (!users.containsKey(conn)) return;
        if (type == MessageType.PING) {
            Messages.ping(conn);
            return;
        }
        if (!DogerDager.multiplayerGameStarted) return;
        if (HostPlayScreen.getPlayerByName(users.get(conn)).dead()) return;
        switch (type) {
            case KEYS_DOWN -> Messages.keyDown(conn, msg);
            case KEYS_UP -> Messages.keyUp(conn, msg);
            case SHOOT -> Messages.shoot(conn, msg);
            default -> {}
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ErrorLogs.write("WebSocket Server Encountered An Error: "+ex);
        ErrorNotifier.show("server error (check multiplayer.logs)");
    }

    @Override
    public void onStart() {
        ErrorLogs.write("WS Server Started");
    }

    public void onAppStop() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            dispose();
        }));
    }

    public void sendWS(WebSocket conn, String type, Map<String, Object> data) {
        Map<String, Object> json = new HashMap<>(data);
        json.put("t", type);
        if (conn == null) return;
        if (conn.isClosed()) return;
        conn.send(gson.toJson(json));
    }
    public void broadcastWS(String type, Map<String, Object> data) {
        Map<String, Object> json = new HashMap<>(data);
        json.put("t", type);
        for (WebSocket conn : users.keySet()) {
            if (conn == null) continue;
            if (users.get(conn)==null) continue;
            if (conn.isClosed()) continue;
            conn.send(gson.toJson(json));
        }
    }

    public static Websocket getInstance() {
        return instance;
    }

    public static void stopServer() {
        DogerDager.setMultiplayer(false);
        DogerDager.multiplayerGameStarted = false;

        if (instance == null) return;
        try {
            instance.stop();
        } catch (InterruptedException ex) {
            ErrorLogs.write("Error while stopping WebSocket server: "+ex);
        }
    }

    public List<String> getUserList() {
        return new ArrayList<String>(users.values());
    }

    public List<WebSocket> getConnectionsList() {
        return new ArrayList<WebSocket>(users.keySet());
    }

    public HashMap<WebSocket, Long> getPingMap() {
        return new HashMap<WebSocket, Long>(lastPing);
    }

    public HashMap<WebSocket, Long> getNotVerifiedMap() {
        return new HashMap<WebSocket, Long>(notVerified);
    }

    public void addHost(String name) {
        users.put(null, name);
    }

    public void markLoggedIn(WebSocket conn, String name) {
        notVerified.remove(conn);
        users.put(conn, name);
        lastPing.put(conn, System.currentTimeMillis());
    }

    public boolean isUserLoggedIn(String name) {
        return users.containsValue(name);
    }

    public String getUserName(WebSocket conn) {
        return users.get(conn);
    }

    public void pingFromUser(WebSocket conn) {
        lastPing.put(conn, System.currentTimeMillis());
    }

    public void deleteNotVerified() {
        for (WebSocket conn : notVerified.keySet()) {
            logOffUser(conn, 1000, "Game has been started.");
        }
    }

    public void logOffUser(WebSocket conn, Integer code, String message) {
        if (conn == null) return;
        String name = users.get(conn);
        //deleting connection
        lastPing.remove(conn);
        notVerified.remove(conn);
        users.remove(conn);
        conn.close(code, message);
    }
    public static void dispose() {
        stopServer();
    }
}