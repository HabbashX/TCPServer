package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.ServerConsoleChatEvent;

import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.BRIGHT_RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.GRAY;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

@EventHandler
public final class DefaultServerConsoleChatHandler implements Listener<ServerConsoleChatEvent> {

    private final Server server;

    public DefaultServerConsoleChatHandler(Server server) {
        this.server = server;
    }

    @Override
    public void onEvent(@NotNull ServerConsoleChatEvent event) {
        String message = event.getMessage();
        System.out.printf("%s[Server]%s: %s%s\n", BRIGHT_RED, GRAY, RESET, message);
        server.broadcast("%s[Server]%s: %s%s".formatted(BRIGHT_RED, GRAY, RESET, message));
    }
}
