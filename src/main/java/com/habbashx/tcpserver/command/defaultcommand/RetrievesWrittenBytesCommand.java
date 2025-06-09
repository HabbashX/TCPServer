package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.security.Permission;
import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.NotNull;

/**
 * The RetrievesWrittenBytesCommand class is a concrete implementation of the CommandExecutor
 * abstract class. Its primary responsibility is to retrieve and display the total number of
 * bytes written by the server. This class is annotated with the {@link Command} annotation,
 * which defines the command's metadata, such as its name and required permissions.
 * <p>
 * This command requires the {@link Permission#RETRIEVES_WRITTEN_BYTES_PERMISSION}
 * permission to execute, ensuring that only authorized users can invoke it.
 */
@Command(name = "bytes", permission = Permission.RETRIEVES_WRITTEN_BYTES_PERMISSION)
public final class RetrievesWrittenBytesCommand extends CommandExecutor {

    private final Server server;

    public RetrievesWrittenBytesCommand(Server server) {
        this.server = server;
    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        final String writtenBytes = String.valueOf(server.getWrittenBytes());
        commandContext.getSender().printMessage(writtenBytes);
    }
}
