package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.annotation.PossibleEmpty;
import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.configuration.JsonConfiguration;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.handler.UserHandler;

import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.habbashx.tcpserver.logger.ConsoleColor.BRIGHT_RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.GRAY;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;


/**
 * The PrivateMessageCommand class is responsible for implementing the private messaging
 * feature within the server. It extends the CommandExecutor base class and enables
 * users to send private messages to other online users.
 *
 * This command, invoked with the "msg" alias, is configured to enforce a cooldown
 * of 5 seconds between invocations by a given user. The configuration for this command
 * can be managed via the `commands-configuration/privateMessageCommand-configuration.json` file.
 *
 * Features:
 * - Validates that the sender provides both a target username and a message.
 * - Sends messages to a valid, online user identified by the provided username.
 * - Prevents users from messaging themselves.
 * - Informs the sender if the specified target user is not found.
 * - Customizes the private message style based on configuration settings.
 *
 * Key Components:
 * - {@link JsonConfiguration} is used to load the configuration for private messaging styles.
 * - {@link CooldownManager} inherited from the base class is utilized to control command usage frequency.
 *
 * Notes:
 * - Requires the sender to be a valid user for execution.
 * - Any errors, such as missing arguments or invalid usernames, will be relayed back to the sender via messages.
 */
@Command(
        name = "msg",
        cooldownTimeUnit = TimeUnit.SECONDS,
        cooldownTime = 5L,
        configFile = "commands-configuration/privateMessageCommand-configuration.json"
)
public final class PrivateMessageCommand extends CommandExecutor {

    private static final String COMMAND_USAGE_MESSAGE = "usage: /msg <username> <message>";
    private final Server server;

    private final JsonConfiguration config;

    public PrivateMessageCommand(Server server) {
        this.server = server;
        this.config = loadConfiguration(server);
    }

    @Override
    public void execute(@NotNull CommandContext context) {

        if (context.getSender() instanceof UserHandler userHandler) {
            if (context.getArgs().size() < 2) {
                userHandler.sendMessage(COMMAND_USAGE_MESSAGE);
                return;
            }

            @PossibleEmpty
            String targetUsername = context.getArgs().get(0);
            String message = String.join(" ", context.getArgs().subList(1, context.getArgs().size()));

            @Nullable
            UserHandler targetUser = server.getServerDataManager().getOnlineUserByUsername(targetUsername);

            String privateMessageStyle = getPrivateMessageStyle();

            if (targetUser != null) {
                if (!targetUser.getUserDetails().getUsername().equals(context.getSenderName())) {
                    assert privateMessageStyle != null;
                    targetUser.sendMessage(privateMessageStyle
                            .formatted(GRAY, BRIGHT_RED, GRAY, RESET) +
                            context.getSenderName() + ": " + message);
                } else
                    userHandler.sendMessage(BRIGHT_RED + "you cannot message your self :D" + RESET);

            } else userHandler.sendMessage("User " + targetUsername + " not found.");

        }
    }


    private @Nullable String getPrivateMessageStyle() {
        return (String) config.returnValue("private-message-style");
    }

    @Override
    public CooldownManager getCooldownManager() {
        return super.getCooldownManager();
    }
}
