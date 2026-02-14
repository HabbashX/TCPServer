package com.habbashx.tcpserver.util;

import org.jetbrains.annotations.NotNull;

public class ExceptionUtils {

    public static @NotNull String getStackTrace(@NotNull Exception e) {

        final StackTraceElement[] stackTraceElements = e.getStackTrace();

        final StringBuilder builder = new StringBuilder();
        for (final StackTraceElement element : stackTraceElements) {
            builder.append(element);
        }
        return builder.toString();
    }
}
