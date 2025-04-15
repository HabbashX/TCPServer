package com.habbashx.tcpserver.event;

import static com.habbashx.tcpserver.logger.ConsoleColor.BRIGHT_RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.GRAY;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

public class ServerConsoleChatEvent extends Event {

    private final String name = "%s[Server]%s: %s\n".formatted(BRIGHT_RED, GRAY, RESET);
    private final String message;

    public ServerConsoleChatEvent(String message) {
        super("ServerConsoleChatEvent");
        this.message = message;
    }

    @Override
    public void setCancelled(boolean cancelled) {
       super.setCancelled(cancelled);
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean isCancelled() {
        return super.isCancelled();
    }
}
