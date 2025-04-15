package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

@Command(name = "online" , executionLog = true)
public final class ListUserCommand extends CommandExecutor {

    private final Server server;

    public ListUserCommand(Server server) {
        this.server =server;
    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {
        int onlineUsers = server.getConnections().size();

        if (commandContext.getSender() instanceof UserHandler user) {
            user.sendMessage("online users: "+onlineUsers);
        } else {
            System.out.println("online users: "+onlineUsers);
        }
    }
}
