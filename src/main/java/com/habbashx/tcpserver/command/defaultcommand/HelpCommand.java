package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.terminal.HelpBoard;

import java.io.File;
import java.io.IOException;

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

    private static final File HELP_BOARD_PATH = new File("data/helpBoard.txt");
    public final Server server;

    public HelpCommand(Server server) {
        this.server = server;
    }

    @Override
    public void execute(CommandContext commandContext) {

        try {
            if (HELP_BOARD_PATH.exists()) {

                commandContext.getSender().printMessage(HelpBoard.getHelpBoard());
            } else {
                HELP_BOARD_PATH.createNewFile();
            }
        } catch (IOException e) {
            server.getServerLogger().error(e);
        }
    }
}
