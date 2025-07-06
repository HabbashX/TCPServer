package com.habbashx.tcpserver.command;

import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.connection.console.ServerConsoleHandler;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents an abstract class for entities that can send commands.
 * <p>
 * This class provides basic functionality for handling command execution contexts, ensuring
 * thread-safe operations through the use of a {@link ReentrantLock}. It also handles the
 * transmission of messages to specific sender types, whether console-based or user-based.
 * <p>
 * The class is sealed, permitting only specific subclasses to extend from it. These subclasses
 * define the specific behavior for console command sending and user-based command sending.
 */
public interface CommandSender {

    /**
     * Prints a message to the appropriate output destination depending on the type of CommandSender.
     * If the current instance is of type UserHandler, the message will be sent to the user's client.
     * If the current instance is of type ServerConsoleHandler, the message will be printed to the server console.
     *
     * @param message The message to be printed or sent. Cannot be null and should represent textual data
     *                intended for the user or console output.
     */
    default void printMessage(String message) {

        if (this instanceof UserHandler userHandler) {
            userHandler.sendMessage(message);
        } else if (this instanceof ServerConsoleHandler) {
            System.out.println(message);
        }
    }

    /**
     *
     */
    default ReentrantLock getReentrantLock() {
        return new ReentrantLock();
    }
}
