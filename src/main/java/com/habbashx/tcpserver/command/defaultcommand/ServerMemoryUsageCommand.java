package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.connection.console.ServerConsoleHandler;
import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a command to display the current memory usage of the server.
 * This command is tailored for execution within the server console environment
 * and provides an output of the server's used memory and total allocated memory.
 * <p>
 * If the command is executed by a user (e.g., through a user interface or
 * player interaction), a message is sent to inform that the command is
 * unavailable for such contexts.
 * <p>
 * This class is part of a command execution framework and extends
 * {@link CommandExecutor}, leveraging its capabilities for execution
 * within a specified context.
 */
@Command(name = "memoryusage")
public final class ServerMemoryUsageCommand extends CommandExecutor {

    private final Server server;

    public ServerMemoryUsageCommand(Server server) {
        this.server = server;
    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getSender() instanceof ServerConsoleHandler) {
            final var serverMemoryMonitor = server.getServerMemoryMonitor();
            final long usedMemory = serverMemoryMonitor.getMemoryUsage();
            final long maxMemory = serverMemoryMonitor.getMaxMemory();
            System.out.println(
                    serverMemoryMonitor.formatBytes(usedMemory) + " / " + serverMemoryMonitor.formatBytes(maxMemory)
            );
        } else if (commandContext.getSender() instanceof final UserHandler userHandler) {
            userHandler.sendMessage("this command not available for users :D");
        }
    }
}
