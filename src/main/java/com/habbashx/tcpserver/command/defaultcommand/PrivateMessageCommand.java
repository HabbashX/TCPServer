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
