package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.user.UserDetails;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * The InfoCommand class is a command handler responsible for displaying user account information
 * when executed. It extends the CommandExecutor class and implements the required behavior for
 * handling the "info" command.
 *
 * The command displays details such as the user's IP address, ID, role, username, email, phone number,
 * and account activation status. The command can only be executed by a user, not through the console.
 */
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

    /**
     * Executes a command in the given command context. If the sender is a user, displays
     * user details through a message. If the sender is from the console, displays a warning message.
     *
     * @param commandContext the context in which the command is being executed, including the sender
     *                       and any additional arguments provided.
     */
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
