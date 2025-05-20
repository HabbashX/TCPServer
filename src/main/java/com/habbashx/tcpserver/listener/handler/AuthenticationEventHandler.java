package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.AuthenticationEvent;
import com.habbashx.tcpserver.event.UserJoinEvent;
import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.listener.Listener;

import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.LIME_GREEN;
import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * The AuthenticationEventHandler is responsible for handling {@link AuthenticationEvent} instances.
 * This final class implements the {@link Listener} interface and provides behavior associated with
 * user authentication events, including registration and login actions.
 *
 * Key Responsibilities:
 * - Processes user authentication events triggered during login or registration operations.
 * - Sends appropriate feedback messages to users based on the authentication outcomes.
 * - Triggers a {@link UserJoinEvent} upon successful user authentication, enabling further
 *   event processing within the system.
 *
 * Behavior Details:
 * - During a registration operation:
 *   - If authentication succeeds, a positive confirmation message is sent to the user, and a
 *     {@link UserJoinEvent} is triggered.
 *   - If authentication fails (e.g., duplicate username), an error message is sent, and the user's
 *     connection is terminated.
 * - During a login operation:
 *   - If authentication succeeds, a success message is sent, and a {@link UserJoinEvent} is triggered.
 *   - If authentication fails, an error message is sent, and the user's connection is terminated.
 *
 * This class ensures proper handling of system events by validating authentication states and dispatching
 * secondary events as necessary.
 *
 * Thread Safety:
 * - This event handler assumes that it will be executed in the appropriate thread context managed by the
 *   event system and does not manage its own synchronization.
 */
@EventHandler
public final class AuthenticationEventHandler implements Listener<AuthenticationEvent> {

    @Override
    public void onEvent(@NotNull AuthenticationEvent event) {

        final String username = event.getUserHandler().getUserDetails().getUsername();
        final UserJoinEvent userJoinEvent = new UserJoinEvent(username,event.getUserHandler());
        if (event.isRegisterOperation()) {
            if (event.isAuthenticated()) {
                event.getUserHandler().sendMessage(LIME_GREEN + "Register Successfully" + RESET);
                event.getUserHandler().getServer().getEventManager().triggerEvent(userJoinEvent);
            } else {
                event.getUserHandler().sendMessage(RED+"this username already registered in system");
                event.getUserHandler().shutdown();
            }
        } else if (event.isAuthenticated())  {
            event.getUserHandler().sendMessage(LIME_GREEN+"login successfully"+RESET);
            event.getUserHandler().getServer().getEventManager().triggerEvent(userJoinEvent);
        } else {
            event.getUserHandler().sendMessage(RED+"wrong username or password"+RESET);
            event.getUserHandler().shutdown();
        }
    }

}
