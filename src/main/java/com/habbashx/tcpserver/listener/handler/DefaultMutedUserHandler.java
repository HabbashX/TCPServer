package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.command.manager.MuteCommandManager;
import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.Priority;
import com.habbashx.tcpserver.event.UserChatEvent;

import com.habbashx.tcpserver.listener.Listener;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * Handles muted user interactions during a {@link UserChatEvent}.
 * When a user sends a chat message, this class intercepts the event
 * and determines if the user is muted. If the user is muted, the event
 * is cancelled, and an appropriate message is sent to the user.
 *
 * This handler is annotated with {@code @EventHandler} with a priority
 * level of {@code Priority.HIGHEST}, ensuring it is executed early in
 * the event handling process.
 *
 * Responsibilities:
 * - Cancels the {@link UserChatEvent} for muted users.
 * - Sends a notification to muted users, informing them of their muted status.
 *
 * Constructor:
 * - Accepts an instance of {@link MuteCommandManager}, which is used to
 *   determine if a user is muted.
 *
 * Implements:
 * - {@link Listener<UserChatEvent>} - Specifically listens to the {@link UserChatEvent}.
 */
@EventHandler(priority = Priority.HIGHEST)
public final class DefaultMutedUserHandler implements Listener<UserChatEvent> {

    private final MuteCommandManager muteCommandManager;

    public DefaultMutedUserHandler(MuteCommandManager muteCommandManager) {
        this.muteCommandManager = muteCommandManager;
    }

    @Override
    public void onEvent(@NotNull UserChatEvent userChatEvent) {
        if (muteCommandManager.isUserMuted(userChatEvent.getUsername())) {
            userChatEvent.setCancelled(true);
            userChatEvent.getUser().sendMessage(RED+"you`re muted"+RESET);
        }

    }
}
