package com.habbashx.tcpserver.event;

import com.habbashx.tcpserver.event.manager.EventManager;
import org.jetbrains.annotations.Contract;

import static com.habbashx.tcpserver.logger.ConsoleColor.*;

/**
 * Represents an event triggered when a message is sent via the server console.
 * This event allows developers to capture and process messages entered into
 * the console by the server administrator or other authorized users.
 * <p>
 * This class extends the base {@code Event} class, inheriting the ability
 * to check and set its cancellation status. If cancelled, the event's
 * associated action will not be executed.
 * <p>
 * Key features of this event include:
 * - Capturing the name format specific to server console messages.
 * - Retrieving the actual message input by the user.
 * <p>
 * This event can be triggered via the server's {@link EventManager} and handled
 * by listeners specializing in {@code ServerConsoleChatEvent}.
 */
public class ServerConsoleChatEvent extends Event {

    private static final String NAME = "%s[Server]%s: %s\n".formatted(BRIGHT_RED, GRAY, RESET);
    private final String message;

    public ServerConsoleChatEvent(String message) {
        super("ServerConsoleChatEvent");
        this.message = message;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);
    }

    @Contract(pure = true)
    public static String getName() {
        return NAME;
    }

    public String getMessage() {
        return message;
    }
}
