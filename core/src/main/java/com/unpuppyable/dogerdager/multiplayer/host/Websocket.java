package com.unpuppyable.dogerdager.multiplayer.host;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.unpuppyable.dogerdager.DogerDager;
import com.unpuppyable.dogerdager.MultiplayerScreen;
import com.unpuppyable.dogerdager.multiplayer.Schedulers;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class Websocket extends WebSocketServer {
    private static Websocket instance;
    public final HashMap<WebSocket, String> users = new HashMap<WebSocket, String>(); //logged-in users map;
    public final HashMap<WebSocket, Long> notVerified = new HashMap<WebSocket, Long>();
    public final HashMap<WebSocket, Long> lastPing = new HashMap<WebSocket, Long>();

    private static Gson gson;
    public Websocket(InetSocketAddress address) {
        super(address); //address ws
        instance = this;
        gson = new Gson();
    }
    public static Websocket startServer(int port) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() ->
        {
            WebSocketServer server = new Websocket(new InetSocketAddress(port));
            latch.countDown();
            server.run();
        }).start();
        latch.await();
        Schedulers.pingStateScheduler(30000);
        Schedulers.notVerifiedScheduler(60000);
        return getInstance();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (DogerDager.multiplayerGameStarted) {
            logOff(conn, 1000, "Game has already started.");
            return;
        }
        notVerified.put(conn, System.currentTimeMillis());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("check out this userlist "+users);
        System.out.println("and thats him: "+conn);
        notVerified.remove(conn);
        //broadcast
        if (!users.containsKey(conn)) return;
        String name = users.get(conn);
        users.remove(conn);
        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("user", name);
        broadcastWS("202", response);
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
        //0 auth
        if (msg.get("t").equals("0")) {
            Messages.authorize(conn, msg);
            return;
        }
        if (!users.containsKey(conn)) return;
        //5 ping
        if (msg.get("t").equals("5")) {
            Messages.ping(conn);
            return;
        }
        if (!DogerDager.multiplayerGameStarted) return;
        if (HostPlayScreen.getPlayerByName(users.get(conn)).dead()) return;
        //1 key being pressed
        if (msg.get("t").equals("1")) {
            Messages.keyDown(conn, msg);
            return;
        }
        //2 key being let go
        if (msg.get("t").equals("2")) {
            Messages.keyUp(conn, msg);
            return;
        }
        //3 shoot arrow
        if (msg.get("t").equals("3")) {
            Messages.shoot(conn, msg);
            return;
        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ServerLogs.write("WS Server Encountered An Error: "+ex);
    }

    @Override
    public void onStart() {
        ServerLogs.write("WS Server Started");
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

    public WebSocket nameToConn(String name) {
        for (WebSocket conn : users.keySet()) {
            if (users.get(conn).equals(name)) return conn;
        }
        return null;
    }

    public void deleteNotVerified() {
        for (WebSocket conn : notVerified.keySet()) {
            logOff(conn, 1000, "Game has been started.");
        }
    }

    public void logOff(WebSocket conn, Integer code, String message) {
        String name = users.get(conn);
        //some actions to delete player off a map

        //deleting connection
        lastPing.remove(conn);
        notVerified.remove(conn);
        users.remove(conn);
        conn.close(code, message);
    }
}