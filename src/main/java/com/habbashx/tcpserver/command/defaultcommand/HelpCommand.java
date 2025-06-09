package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.socket.server.Server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * Represents a command that displays the help board content to the command sender.
 *
 * The HelpCommand reads from a predefined file and sends the content to the command
 * executor, either as a message for a user or prints directly to the console if executed
 * by non-user entities.
 *
 * It ensures the help file exists and, if missing, creates a new empty help board file
 * during execution.
 */
@Command(name = "help")
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
                Scanner scanner = new Scanner(new FileReader(HELP_BOARD_PATH));

                StringBuilder stringBuilder = new StringBuilder();

                while (scanner.hasNextLine()) {
                    stringBuilder.append(scanner.nextLine()).append("\n");
                }

                if (commandContext.getSender() instanceof UserHandler user) {
                    user.sendMessage(stringBuilder.toString());
                } else {
                    System.out.println(stringBuilder);
                }
                scanner.close();
            } else {
                HELP_BOARD_PATH.createNewFile();
            }
        } catch (IOException e) {
            server.getServerLogger().error(e);
        }
    }
}
