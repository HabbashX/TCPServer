package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.Priority;
import com.habbashx.tcpserver.event.UserChatEvent;

import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * The DefaultChatHandler class is an event listener for handling UserChatEvent instances.
 * It is responsible for managing user chat messages, enforcing permission rules, and applying
 * cooldown restrictions for chat activities. The class ensures proper message formatting and
 * broadcasts the message to the server if all conditions are met.
 *
 * Core Responsibilities:
 * - Intercepts UserChatEvent and validates the user's chat message.
 * - Enforces permissions for sending messages (e.g., checking if a user has required privileges).
 * - Prevents users from sending messages during cooldown periods unless they have the required permission.
 * - Broadcasts non-empty and valid messages to the server and applies a cooldown for the user.
 * - Responds to special cases such as empty messages or active cooldowns with appropriate error messages.
 *
 * Configuration:
 * - The class is annotated with @EventHandler with a priority level of LOW, indicating its execution
 *   order relative to other event handlers.
 *
 * Constructor:
 * - Accepts a Server instance, which is used to broadcast messages across the server.
 *
 * Methods:
 * - onEvent: The main event handling method that validates and processes user chat messages.
 *   It handles scenarios such as empty messages, permissions checks, and cooldown enforcement.
 * - sendMessage: A helper method to format and broadcast a user's message to the server and
 *   log it to the system console.
 *
 * Usage Notes:
 * - Ensure that this handler is properly registered with the event management system for it to
 *   intercept and process UserChatEvent instances.
 * - The message formatting relies on the user's role prefix and resets format using predefined values.
 */
@EventHandler(priority = Priority.LOW )
public final class DefaultChatHandler implements Listener<UserChatEvent> {

    private final Server server;

    public DefaultChatHandler(Server server) {
        this.server = server;
    }

    @Override
    public void onEvent(@NotNull UserChatEvent userChatEvent) {

        if (!userChatEvent.getMessage().isEmpty()) {
            if (userChatEvent.getUser().hasPermission(0X07)) {
                sendMessage(userChatEvent);
            } else {
                if (!userChatEvent.isOnCooldown()) {
                    sendMessage(userChatEvent);
                } else {
                    final int remainingCoolDown = (int) userChatEvent.getRemainingCooldown();
                    userChatEvent.getUser().sendMessage(RED + "you`re on cooldown for " + remainingCoolDown + " second" + RESET);
                }
            }
        } else {
            userChatEvent.getUser().sendMessage(RED+"you cannot send empty message"+RESET);
        }
    }

    private void sendMessage(@NotNull UserChatEvent userChatEvent) {

        final String prefix = userChatEvent.getUser().getUserDetails().getUserRole().getPrefix();
        final String username = userChatEvent.getUsername();

        server.broadcast(prefix +username + RESET+": "+userChatEvent.getMessage());
        System.out.println(prefix +username + RESET+": "+userChatEvent.getMessage());
        userChatEvent.applyCooldown();

    }
}
