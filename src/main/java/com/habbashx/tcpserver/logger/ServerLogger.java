package com.habbashx.tcpserver.logger;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

public class ServerLogger {

    private final SimpleDateFormat simpleDateFormat;

    public ServerLogger() {
        this.simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }

    private @NotNull String formatMessage(String message , @NotNull Level level) {
        String formatedDate = simpleDateFormat.format(new Date());
        String color = null;
        switch (level) {
            case INFO -> color = ConsoleColor.BRIGHT_GREEN;
            case WARNING -> color = ConsoleColor.BRIGHT_RED;
            case ERROR -> color = ConsoleColor.RED;
            case MONITOR -> color = ConsoleColor.BRIGHT_BLUE;

        }
        String messageColor = null;
        switch (level) {
            case INFO, WARNING -> messageColor = RESET;
            case ERROR -> messageColor = ConsoleColor.RED;
            case MONITOR -> messageColor = ConsoleColor.LIME_GREEN;
        }

        assert color != null;
        assert messageColor != null;
        return String.format("%s[%s%s%s] %s[%s%s%s]%s: %s%s",
                RESET, ConsoleColor.BRIGHT_PURPLE,formatedDate, RESET,
                RESET,color,level, RESET,messageColor,message,RESET
        );
    }

    private void log(String message ,Level level) {
        System.out.println(formatMessage(message,level));;
    }

    public void info(String message) {
        log(message,Level.INFO);
    }

    public void warning(String message) {
        log(message,Level.WARNING);
    }

    public void error(String message) {
        log(message,Level.ERROR);
    }

    public void monitor(String message) {
        log(message,Level.MONITOR);
    }
}
