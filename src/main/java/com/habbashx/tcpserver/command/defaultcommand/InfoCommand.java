package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.user.UserDetails;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

@Command(name = "info")
public class InfoCommand extends CommandExecutor {

    private static final String USER_DETAILS_MESSAGE = """
            userIP: %s
            userID: %s
            userRole: %s
            username: %s
            userEmail: %s
            phoneNumber: %s
            isActiveAccount: %s
            """;

    private static final String CONSOLE_EXECUTE_COMMAND_WARNING_MESSAGE = RED+"this command cannot be executed by console"+RESET;

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getSender() instanceof UserHandler userHandler) {

            UserDetails userDetails = userHandler.getUserDetails();

            userHandler.sendMessage(USER_DETAILS_MESSAGE.formatted(
                    userDetails.getUserIP(),
                    userDetails.getUserID(),
                    userDetails.getUserRole(),
                    userDetails.getUsername(),
                    userDetails.getUserEmail(),
                    userDetails.getPhoneNumber(),
                    userDetails.isActiveAccount()
            ));

        } else {
            System.out.println(CONSOLE_EXECUTE_COMMAND_WARNING_MESSAGE);
        }
    }
}
