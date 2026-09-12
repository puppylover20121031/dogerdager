package com.unpuppyable.dogerdager.multiplayer;
import com.unpuppyable.dogerdager.multiplayer.client.ClientMessages;
import com.unpuppyable.dogerdager.multiplayer.client.WebsocketClient;
import com.unpuppyable.dogerdager.multiplayer.host.Websocket;
import org.java_websocket.WebSocket;


import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Schedulers {
    private static Websocket wsInstance = Websocket.getInstance();
    public static void reloadWsInstance() { wsInstance = Websocket.getInstance(); }

    private static WebsocketClient clientInstance = WebsocketClient.getInstance();
    public static void reloadClientInstance() { clientInstance = WebsocketClient.getInstance(); }

    private final static Set<ScheduledExecutorService> schedulers = ConcurrentHashMap.newKeySet();

    public static void stopSchedulers() {
        for (ScheduledExecutorService s : schedulers) {
            s.close();
        }
        schedulers.clear();
    }

    public static void pingStateScheduler(int timeToPing) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            HashMap<WebSocket, Long> lastPing = wsInstance.getPingMap();
            if (lastPing.isEmpty()) return;
            for (WebSocket conn : lastPing.keySet()) {
                if (System.currentTimeMillis() - lastPing.get(conn) > timeToPing) {
                    wsInstance.logOffUser(conn, 1006, "timed out");
                }
            }
        }, 0,15, TimeUnit.SECONDS );
        schedulers.add(scheduler);
    }
    public static void notVerifiedScheduler(int timeToVerify) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            HashMap<WebSocket, Long> notVerified = wsInstance.getNotVerifiedMap();
            if (notVerified.isEmpty()) return;
            for (WebSocket conn : notVerified.keySet()) {
                if (System.currentTimeMillis() - notVerified.get(conn) > timeToVerify) {
                    wsInstance.logOffUser(conn, 1006, "didnt register in time");
                }
            }
        }, 0,5, TimeUnit.SECONDS );
        schedulers.add(scheduler);
    }


    //client
    public static void sendOutPingMessage(int period) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            if (!clientInstance.isVerified()) return;
            ClientMessages.sendPing();
            }, 0,5, TimeUnit.SECONDS );
        schedulers.add(scheduler);
    }
}
