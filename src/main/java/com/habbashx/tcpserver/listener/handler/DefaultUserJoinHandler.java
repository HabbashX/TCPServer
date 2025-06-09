package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.Priority;
import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.UserJoinEvent;
import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * The DefaultUserJoinHandler class is an implementation of the Listener interface
 * that handles `UserJoinEvent`. It is responsible for processing user join events
 * on the server.
 *
 * This class checks if a user attempting to join the server is banned. If the user
 * is not banned, a join message is broadcasted to the chat, and the connection is
 * logged. If the user is banned, the join event is canceled, the user is notified,
 * and the connection attempt is logged and terminated.
 */
@EventHandler(priority = Priority.HIGHEST)
public final class DefaultUserJoinHandler implements Listener<UserJoinEvent> {

    private final Server server;

    public DefaultUserJoinHandler(Server server) {
        this.server = server;
    }

    @Override
    public void onEvent(@NotNull UserJoinEvent userJoinEvent) {

        if (!server.getBanCommandManager().isUserBanned(userJoinEvent.getUsername())) {
            server.broadcast(userJoinEvent.getUsername() +" join the chat");
            server.getServerLogger().info(userJoinEvent.getUsername() + " connected to server");
            return;
        }

        userJoinEvent.setCancelled(true);
        userJoinEvent.getUser().sendMessage(RED+"you are banned from server"+RESET);
        userJoinEvent.getUser().getUserDetails().setUsername(null);
        server.getServerLogger().monitor("the user: "+userJoinEvent.getUsername() + " trying to join to server");
        userJoinEvent.getUser().shutdown();

    }
}
