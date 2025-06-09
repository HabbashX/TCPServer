package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a command to list the number of online users currently connected to the server.
 * This command can be executed by a user or logged to the console.
 *
 * The command retrieves the current number of active connections from the server
 * and sends this information to the sender of the command. If the command is executed
 * by a user, the message is sent directly to that user. Otherwise, the count is printed
 * to the console.
 *
 * This command is annotated with {@code @Command} to define its name and execution log settings.
 *
 * Dependencies:
 * - A {@code Server} instance is required for retrieving the active connections.
 *
 * Behavior:
 * - Displays the number of online users.
 * - Supports a sender that may be a user or a console command executor.
 */
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
