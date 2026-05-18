package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.user.UserDetails;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;


/**
 * The UserDetailsCommand class is responsible for executing the "find" command,
 * which retrieves user details based on a provided username or user ID.
 * The command can handle both administrative and normal user contexts, displaying
 * different levels of detail depending on the user's role.
 * <p>
 * This class extends CommandExecutor to integrate with the server's command execution framework.
 * It is annotated with the {@code @Command} annotation, specifying command metadata such as name,
 * cooldown time, and execution logging.
 * <p>
 * Key functionalities include:
 * - Resolving user details by username or ID.
 * - Providing a more detailed response for administrators and super administrators.
 * - Managing output formatting and messaging for both command senders and logged output.
 * - Validating command input and handling invalid usages.
 * <p>
 * Constructor:
 * - UserDetailsCommand(Server server): Initializes the command with the given server context.
 * <p>
 * Overridden methods:
 * - execute(CommandContext commandContext): Executes the command using the provided command context,
 * retrieving and sending user details to the command sender.
 * - getCooldownManager(): Retrieves the CooldownManager to apply cooldowns to the command execution.
 * <p>
 * Utility methods:
 * - sendUserDetails(Role commandSenderRole, UserDetails userDetails, CommandSender sender): Formats
 * and sends the appropriate user detail message based on the sender's role and the retrieved user details.
 * - sendMessage(CommandSender commandSender, String message): Sends a message to the command sender,
 * either via direct sender communication or system output.
 * <p>
 * Annotations:
 * - {@code @Command}: Specifies the command metadata including its name ("find"), cooldown time
 * management (10 seconds), and execution logging.
 * <p>
 * This class handles exceptions such as missing arguments or invalid input formats,
 * and provides appropriate usage messages to guide the user.
 */
@Command(
        name = "find",
        cooldownTimeUnit = TimeUnit.SECONDS,
        cooldownTime = 10L,
        executionLog = true
)
public final class UserDetailsCommand extends CommandExecutor {

    private static final String USAGE = "usage: /find <username|id>";

    private static final String ADMIN_MSG = """
            User found!
            userIP: %s
            userID: %s
            userRole: %s
            username: %s
            userEmail: %s
            phoneNumber: %s
            isActiveAccount: %s
            """;

    private static final String USER_MSG = """
            userID: %s
            userRole: %s
            username: %s
            """;

    private static final String NOT_FOUND = RED + "user not found" + RESET;

    private final Server server;

    public UserDetailsCommand(Server server) {
        this.server = server;
    }

    @Override
    public void execute(@NotNull CommandContext ctx) {

        if (ctx.getArgs().isEmpty()) {
            send(ctx.getSender(), USAGE);
            return;
        }

        String target = ctx.getArgs().getFirst();

        if (target.isBlank()) {
            send(ctx.getSender(), "username or id cannot be empty");
            return;
        }

        UserDetails user = resolveUser(target);

        if (user == null) {
            send(ctx.getSender(), NOT_FOUND);
            return;
        }

        Role role = extractRole(ctx.getSender());

        sendUserDetails(role, user, ctx.getSender());
    }


    private UserDetails resolveUser(String target) {

        if (isNumeric(target)) {
            return server.getServerDataManager().getUserById(target);
        }

        return server.getServerDataManager().getUserByUsername(target);
    }

    private boolean isNumeric(String value) {
        for (char c : value.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private Role extractRole(CommandSender sender) {
        if (sender instanceof UserHandler user) {
            return user.getUserDetails().getUserRole();
        }
        return Role.SUPER_ADMINISTRATOR;
    }

    private void sendUserDetails(Role role, UserDetails user, CommandSender sender) {

        if (role == Role.ADMINISTRATOR || role == Role.SUPER_ADMINISTRATOR) {

            send(sender, ADMIN_MSG.formatted(
                    user.getUserIP(),
                    user.getUserID(),
                    user.getUserRole(),
                    user.getUsername(),
                    user.getUserEmail(),
                    user.getPhoneNumber(),
                    user.isActiveAccount()
            ));

        } else {

            send(sender, USER_MSG.formatted(
                    user.getUserID(),
                    user.getUserRole(),
                    user.getUsername()
            ));
        }
    }

    private void send(CommandSender sender, String msg) {
        if (sender instanceof UserHandler user) {
            user.sendTextMessage(msg);
        } else {
            System.out.println(msg);
        }
    }
}