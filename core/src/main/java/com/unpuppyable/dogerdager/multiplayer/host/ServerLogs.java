package com.unpuppyable.dogerdager.multiplayer.host;
import java.io.PrintWriter;
import java.io.FileWriter;
//time
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
//exceptions
import java.io.IOException;
public class ServerLogs {
    private static final PrintWriter logger;

    static {
        try {
            logger = new PrintWriter(new FileWriter("../core/src/main/java/com/unpuppyable/dogerdager/multiplayer/host/wsServer.log", true));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't open logs", e);
        }
    }
    public static void write(String message) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        logger.println("["+timestamp+"] "+ message);
        logger.flush();
    }
}