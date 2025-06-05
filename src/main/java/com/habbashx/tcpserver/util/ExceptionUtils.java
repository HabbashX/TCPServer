package com.habbashx.tcpserver.util;

import org.jetbrains.annotations.NotNull;

/**
 * A utility class that provides helper methods related to exception handling.
 */
public final class ExceptionUtils {

    /**
     * Converts the stack trace of the specified exception into a string representation.
     *
     * @param e The exception from which the stack trace will be extracted. Must not be null.
     * @return A string representation of the stack trace of the given exception.
     */
    public static @NotNull String getStackTrace(@NotNull Exception e) {

        StackTraceElement[] stackTrace = e.getStackTrace();

        StringBuilder builder = new StringBuilder();

        for (StackTraceElement stackTraceElement : stackTrace) {
            builder.append(stackTraceElement.toString()).append("\n");
        }

        return builder.toString();
    }
}
