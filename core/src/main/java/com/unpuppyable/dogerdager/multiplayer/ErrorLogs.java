package com.unpuppyable.dogerdager.multiplayer;
import com.unpuppyable.dogerdager.ErrorNotifier;

import java.io.PrintWriter;
import java.io.FileWriter;
//time
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
//exceptions
import java.io.IOException;
public class ErrorLogs {
    private static final PrintWriter logger;

    static {
        PrintWriter temporary;
        try {
            temporary = new PrintWriter(new FileWriter("multiplayer.logs", true));
        } catch (IOException e) {
            ErrorNotifier.show("Encountered an issue with logs file");
            temporary = null;
        }
        logger = temporary;
    }
    public static void write(String message) {
        if (logger == null) return;
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        logger.println("["+timestamp+"] "+ message);
        logger.flush();
    }
}