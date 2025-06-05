package com.habbashx.tcpserver.logger;

import com.habbashx.tcpserver.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * A utility class for logging messages on the server with formatting and color-coded levels.
 * This class supports different levels of logging such as INFO, WARNING, ERROR, and MONITOR,
 * each with its own color-coded output for better visibility in console logs.
 */
public final class ServerLogger {

    private final SimpleDateFormat simpleDateFormat;

    public ServerLogger() {
        this.simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }

    private @NotNull String formatMessage(String message, @NotNull Level level) {
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
                RESET, ConsoleColor.BRIGHT_PURPLE, formatedDate, RESET,
                RESET, color, level, RESET, messageColor, message, RESET
        );
    }

    private void log(String message, Level level) {
        System.out.println(formatMessage(message, level));
    }

    public void info(String message) {
        log(message, Level.INFO);
    }

    public void warning(String message) {
        log(message, Level.WARNING);
    }

    public void error(Exception e) {
        log(ExceptionUtils.getStackTrace(e), Level.ERROR);
    }

    public void monitor(String message) {
        log(message, Level.MONITOR);
    }
}
