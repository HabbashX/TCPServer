package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;

import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.handler.console.ServerConsoleHandler;
import com.habbashx.tcpserver.socket.Server;

@Command(name = "memoryusage")
public class ServerMemoryUsageCommand extends CommandExecutor {

    private final Server server;

    public ServerMemoryUsageCommand(Server server) {
        this.server = server;
    }

    @Override
    public void execute(CommandContext commandContext) {

        if (commandContext.getSender() instanceof ServerConsoleHandler) {
            final var serverMemoryMonitor = server.getServerMemoryMonitor();
            final long usedMemory = serverMemoryMonitor.getMemoryUsage();
            final long maxMemory = serverMemoryMonitor.getMaxMemory();
            System.out.println(
                    serverMemoryMonitor.formatBytes(usedMemory)+ " / " + serverMemoryMonitor.formatBytes(maxMemory)
            );
        } else if (commandContext.getSender() instanceof final UserHandler userHandler) {
            userHandler.sendMessage("this command not available for users :D");
        }
    }
}
