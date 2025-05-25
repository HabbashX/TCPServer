package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.annotation.MayBeEmpty;
import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.socket.Server;
import com.habbashx.tcpserver.user.UserDetails;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Year;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;


/**
 * The UserDetailsCommand class is responsible for executing the "find" command,
 * which retrieves user details based on a provided username or user ID.
 * The command can handle both administrative and normal user contexts, displaying
 * different levels of detail depending on the user's role.
 *
 * This class extends CommandExecutor to integrate with the server's command execution framework.
 * It is annotated with the {@code @Command} annotation, specifying command metadata such as name,
 * cooldown time, and execution logging.
 *
 * Key functionalities include:
 * - Resolving user details by username or ID.
 * - Providing a more detailed response for administrators and super administrators.
 * - Managing output formatting and messaging for both command senders and logged output.
 * - Validating command input and handling invalid usages.
 *
 * Constructor:
 * - UserDetailsCommand(Server server): Initializes the command with the given server context.
 *
 * Overridden methods:
 * - execute(CommandContext commandContext): Executes the command using the provided command context,
 *   retrieving and sending user details to the command sender.
 * - getCooldownManager(): Retrieves the CooldownManager to apply cooldowns to the command execution.
 *
 * Utility methods:
 * - sendUserDetails(Role commandSenderRole, UserDetails userDetails, CommandSender sender): Formats
 *   and sends the appropriate user detail message based on the sender's role and the retrieved user details.
 * - sendMessage(CommandSender commandSender, String message): Sends a message to the command sender,
 *   either via direct sender communication or system output.
 *
 * Annotations:
 * - {@code @Command}: Specifies the command metadata including its name ("find"), cooldown time
 *   management (10 seconds), and execution logging.
 *
 * This class handles exceptions such as missing arguments or invalid input formats,
 * and provides appropriate usage messages to guide the user.
 */
@Command(
        name = "find",
        cooldownTimeUnit = TimeUnit.SECONDS,
        cooldownTime = 10L,
        executionLog = true
)
public class UserDetailsCommand extends CommandExecutor {

    private static final String COMMAND_USAGE_MESSAGE = "usage: /find <username|id>";

    /**
     * A template message used to provide comprehensive details of an administrator user.
     * This message includes placeholders for user-specific information such as IP address,
     * ID, role, username, email, phone number, and account status.
     * The placeholders, denoted by `%s`, are dynamically replaced with actual user information
     * when the message is formatted.
     */
    private static final String ADMINISTRATOR_USER_DETAILS_MESSAGE = """
            User found !
            userIP: %s
            userID: %s
            userRole: %s
            username: %s
            userEmail: %s
            phoneNumber: %s
            isActiveAccount: %s
            """;

    /**
     * A constant string used as a template to represent user details messages.
     * The message includes placeholders for user-specific information such as
     * userID, userRole, and username.
     *
     * The format includes:
     * - userID: The unique identifier for the user.
     * - userRole: The role assigned to the user.
     * - username: The name associated with the user.
     *
     * This string is expected to be formatted with actual user details
     * before being sent or displayed.
     */
    private static final String USER_DETAILS_MESSAGE = """
            userID: %s
            userRole: %s
            username: %s
            """;

    private static final String USER_NOT_FOUND_MESSAGE = RED+"user not found"+RESET;

    private final Server server;

    public UserDetailsCommand(Server server) {
        this.server = server;
    }

    /**
     * Executes the given command based on the provided context. This method processes
     * the command arguments to fetch user details either by username or user ID and
     * sends the appropriate response to the command sender.
     *
     * If no arguments are provided or an error occurs during processing, a usage
     * message is sent to the sender.
     *
     * @param commandContext the context of the command execution, containing
     *                       the sender, command arguments, and other metadata; cannot be null
     */
    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().isEmpty()) {
            sendMessage(commandContext.getSender() , COMMAND_USAGE_MESSAGE);
        }

        try {
            @MayBeEmpty
            final String target = commandContext.getArgs().get(0);

            @Nullable
            UserDetails userDetails;

            final boolean targetID = target.startsWith(String.valueOf(Year.now()));
            if (!target.isEmpty()) {
                if (!targetID) {
                    userDetails = server.getServerDataManager().getUserByUsername(target);
                } else {
                    userDetails = server.getServerDataManager().getUserById(target);
                }
                if (commandContext.getSender() instanceof UserHandler userHandler) {
                    Role userRole = userHandler.getUserDetails().getUserRole();
                    sendUserDetails(userRole, userDetails, userHandler);
                } else {
                    sendUserDetails(Role.SUPER_ADMINISTRATOR, userDetails, commandContext.getSender());
                }
            } else {
                sendMessage(commandContext.getSender(), "username or id field may not be empty");
            }
        } catch (IndexOutOfBoundsException ignore) {
            sendMessage(commandContext.getSender(),COMMAND_USAGE_MESSAGE);
        }
    }

    /**
     * Sends user details to the specified command sender based on their role.
     *
     * @param commandSenderRole the role of the command sender, which determines the level of detail provided
     * @param userDetails the details of the user to be sent; may include properties such as IP, ID, role, username, email, etc.
     * @param sender the recipient of the message, which can be a user or console command sender
     */
    private void sendUserDetails(Role commandSenderRole ,UserDetails userDetails , CommandSender sender) {

        if (userDetails != null) {
            if (commandSenderRole.equals(Role.SUPER_ADMINISTRATOR) || commandSenderRole.equals(Role.ADMINISTRATOR)) {
                sendMessage(sender,ADMINISTRATOR_USER_DETAILS_MESSAGE.formatted(
                        userDetails.getUserIP(),
                        userDetails.getUserID(),
                        userDetails.getUserRole().toString(),
                        userDetails.getUsername(),
                        userDetails.getUserEmail(),
                        userDetails.getPhoneNumber(),
                        userDetails.isActiveAccount()
                ));
            } else {
                sendMessage(sender,USER_DETAILS_MESSAGE.formatted(
                        userDetails.getUserID(),
                        userDetails.getUserRole().toString(),
                        userDetails.getUsername()
                ));
            }
        } else {
            sendMessage(sender, USER_NOT_FOUND_MESSAGE);
        }
    }
    private void sendMessage(CommandSender commandSender , String message) {

        if (commandSender instanceof UserHandler userHandler) {
            userHandler.sendMessage(message);
        } else {
            System.out.println(message);
        }
    }

    @Override
    public CooldownManager getCooldownManager() {
        return super.getCooldownManager();
    }
}
