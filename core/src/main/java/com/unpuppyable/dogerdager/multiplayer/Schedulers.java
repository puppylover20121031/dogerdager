package com.unpuppyable.dogerdager.multiplayer;
import com.unpuppyable.dogerdager.multiplayer.client.ClientMessages;
import com.unpuppyable.dogerdager.multiplayer.client.WebsocketClient;
import com.unpuppyable.dogerdager.multiplayer.host.Websocket;
import org.java_websocket.WebSocket;


import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Schedulers {
    private static Websocket wsInstance = Websocket.getInstance();
    private static WebsocketClient clientInstance = WebsocketClient.getClientInstance();

    private static HashSet<ScheduledExecutorService> schedulers = new HashSet<ScheduledExecutorService>();

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
            if (wsInstance.lastPing.isEmpty()) return;
            for (WebSocket conn : wsInstance.lastPing.keySet()) {
                if (System.currentTimeMillis() - wsInstance.lastPing.get(conn) > timeToPing) {
                    wsInstance.logOff(conn, 1006, "timed out");
                }
            }
        }, 0,15, TimeUnit.SECONDS );
    }
    public static void notVerifiedScheduler(int timeToVerify) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            if (wsInstance.notVerified.isEmpty()) return;
            for (WebSocket conn : wsInstance.notVerified.keySet()) {
                if (System.currentTimeMillis() - wsInstance.notVerified.get(conn) > timeToVerify) {
                    wsInstance.logOff(conn, 1006, "didnt register in time");
                }
            }
        }, 0,5, TimeUnit.SECONDS );
    }


    //client
    public static void sendOutPingMessage(int period) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            if (!clientInstance.verified) return;
            ClientMessages.sendPing();
            }, 0,5, TimeUnit.SECONDS );
    }
}
