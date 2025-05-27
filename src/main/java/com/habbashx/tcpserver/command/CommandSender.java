package com.habbashx.tcpserver.command;

import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.handler.console.ServerConsoleHandler;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents an abstract class for entities that can send commands.
 *
 * This class provides basic functionality for handling command execution contexts, ensuring
 * thread-safe operations through the use of a {@link ReentrantLock}. It also handles the
 * transmission of messages to specific sender types, whether console-based or user-based.
 *
 * The class is sealed, permitting only specific subclasses to extend from it. These subclasses
 * define the specific behavior for console command sending and user-based command sending.
 */
public sealed abstract class CommandSender permits UserHandler, ServerConsoleHandler {

    /**
     * A {@code ReentrantLock} instance used to ensure thread-safe operations within
     * the {@link CommandSender} class and its subclasses. This lock is primarily utilized
     * to synchronize actions for maintaining data integrity when accessed by multiple threads.
     */
    private final ReentrantLock reentrantLock = new ReentrantLock();

    /**
     * Prints a message to the appropriate output destination depending on the type of CommandSender.
     * If the current instance is of type UserHandler, the message will be sent to the user's client.
     * If the current instance is of type ServerConsoleHandler, the message will be printed to the server console.
     *
     * @param message The message to be printed or sent. Cannot be null and should represent textual data
     *                intended for the user or console output.
     */
    public void printMessage(String message) {

        if (this instanceof UserHandler userHandler) {
            userHandler.sendMessage(message);
        } else if (this instanceof ServerConsoleHandler) {
            System.out.println(message);
        }
    }

    public ReentrantLock getReentrantLock() {
        return reentrantLock;
    }
}
