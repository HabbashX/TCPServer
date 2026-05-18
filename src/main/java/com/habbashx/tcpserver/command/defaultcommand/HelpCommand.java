package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.terminal.HelpBoard;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents a command that displays the help board content to the command sender.
 * <p>
 * The HelpCommand reads from a predefined file and sends the content to the command
 * executor, either as a message for a user or prints directly to the console if executed
 * by non-user entities.
 * <p>
 * It ensures the help file exists and, if missing, creates a new empty help board file
 * during execution.
 */
@Command(name = "help", aliases = "?")
public final class HelpCommand extends CommandExecutor {

    private static final Path HELP_BOARD_PATH =
            Paths.get("server", "data", "helpBoard.txt");

    private final Server server;

    public HelpCommand(Server server) {
        this.server = server;
    }

    @Override
    public void execute(CommandContext context) {

        try {
            ensureFileExists();

            context.getSender().printMessage(HelpBoard.getHelpBoard());

        } catch (IOException e) {
            server.getServerLogger().error(e);
        }
    }

    private void ensureFileExists() throws IOException {
        Path parent = HELP_BOARD_PATH.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try {
            Files.createFile(HELP_BOARD_PATH);
        } catch (FileAlreadyExistsException ignored) {
        }
    }
}