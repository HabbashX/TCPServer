package com.habbashx.tcpserver.logger;

import com.habbashx.tcpserver.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for logging messages on the server with formatting and color-coded levels.
 * This class supports different levels of logging such as INFO, WARNING, ERROR, and MONITOR,
 * each with its own color-coded output for better visibility in console logs.
 */
public final class ServerLogger {


    public ServerLogger() {
    }


    public void info(String message) {
        Logger logger = LoggerFactory.getLogger(getCallerClass());
        logger.info(message);
    }

    public void warning(String message) {
        Logger logger = LoggerFactory.getLogger(getCallerClass());

        logger.warn(message);

    }

    public void error(Exception e) {
        Logger logger = LoggerFactory.getLogger(getCallerClass());

        logger.error(ExceptionUtils.getStackTrace(e));
    }

    public void error(String message) {
        Logger logger = LoggerFactory.getLogger(getCallerClass());

        logger.error(message);
    }

    public void monitor(String message) {
        Logger logger = LoggerFactory.getLogger(getCallerClass());

        logger.debug(message);
    }

    private Class<?> getCallerClass() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames
                        .filter(frame ->
                                frame.getDeclaringClass() != ServerLogger.class
                        )
                        .findFirst()
                        .map(StackWalker.StackFrame::getDeclaringClass)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Unable to determine caller class"
                                )
                        )
                );
    }
}
