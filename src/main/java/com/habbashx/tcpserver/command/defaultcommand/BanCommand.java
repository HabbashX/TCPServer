package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.annotation.PossibleEmpty;
import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.command.manager.BanCommandManager;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;
import static com.habbashx.tcpserver.security.Permission.BAN_PERMISSION;

@Command(
        name = "ban",
        permission = BAN_PERMISSION,
        aliases = {"banned","block"},
        description = "block user from joining the server",
        cooldownTimeUnit = TimeUnit.SECONDS,
        cooldownTime = 10L,
        executionLog = true
)
public final class BanCommand extends CommandExecutor {

    private static final String COMMAND_USAGE_MESSAGE = "usage: /ban <username>";
    private static final String USER_NOT_FOUND_MESSAGE = RED+"user not found"+RESET;
    private static final String CANNOT_BAN_SELF_MESSAGE = RED+"you cannot ban yourself :D"+RESET;
    private final Server server;

    private final BanCommandManager banManager;

    public BanCommand(Server server ,BanCommandManager banManager) {
        this.server = server;
        this.banManager = banManager;
    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().isEmpty()) {
            sendMessage(commandContext.getSender(),COMMAND_USAGE_MESSAGE);
            return;
        }

        @PossibleEmpty
        final String targetUsername = commandContext.getArgs().get(0);
        @Nullable
        final UserHandler targetUser = server.getServerDataManager().getOnlineUserByUsername(targetUsername);

        if (targetUser != null) {
            if (commandContext.getSender() instanceof UserHandler userHandler) {
                String username = userHandler.getUserDetails().getUsername();

                if (username.equals(targetUsername)) {
                    sendMessage(commandContext.getSender(), CANNOT_BAN_SELF_MESSAGE);
                    return;
                }
            }
            banManager.banUser(targetUser,commandContext.getSender());
        } else {
            sendMessage(commandContext.getSender(),USER_NOT_FOUND_MESSAGE);
        }

    }

    private void sendMessage(CommandSender commandSender, String message) {

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