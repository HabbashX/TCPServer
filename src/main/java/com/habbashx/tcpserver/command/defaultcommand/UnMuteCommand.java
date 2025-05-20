package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.annotation.PossibleEmpty;
import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.command.manager.MuteCommandManager;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;
import static com.habbashx.tcpserver.security.Permission.UN_MUTE_PERMISSION;

/**
 * Represents the command "unmute", allowing a previously muted user to be unblocked in chat.
 * This command can also be referred to using the alias "uncurse".
 *
 * The command performs the following:
 * - Expects a username as an argument to identify the user to unmute.
 * - If the username is not specified or the user is not found, appropriate messages are sent to the command sender.
 * - If the user is found, the mute is removed using the MuteCommandManager.
 *
 * Features of the command include:
 * - Runs asynchronously.
 * - Logs the execution for better tracking.
 * - Enforces a cooldown of 10 seconds between executions to prevent spam.
 *
 * Permissions:
 * - Requires a specific permission to execute, as defined by the constant UN_MUTE_PERMISSION.
 *
 * Concurrency:
 * - Uses a ReentrantLock to ensure command execution and user state modifications are thread-safe.
 *
 * Configuration:
 * - Provides usage message feedback for invalid or missing arguments.
 */
@Command(
        name = "unmute",
        aliases = "uncurse",
        permission = UN_MUTE_PERMISSION,
        description = "block user from chat",
        cooldownTimeUnit = TimeUnit.SECONDS,
        cooldownTime = 10L,
        isAsync = true,
        executionLog = true
)
public final class UnMuteCommand extends CommandExecutor {

    private static final String COMMAND_USAGE_MESSAGE = "usage: /unmute <username>";
    private static final String USER_NOT_FOUND_MESSAGE = RED+"cannot find user with this name"+RESET;

    private final Server server;
    private final MuteCommandManager muteCommandManager;

    public UnMuteCommand(Server server ,MuteCommandManager muteCommandManager) {
        this.server = server;
        this.muteCommandManager = muteCommandManager;

    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().isEmpty()) {
            sendMessage(commandContext.getSender(),COMMAND_USAGE_MESSAGE);
            return;
        }

        @PossibleEmpty
        String username = commandContext.getArgs().get(0);
        @Nullable
        UserHandler targetUser = server.getServerDataManager().getOnlineUserByUsername(username);

        ReentrantLock reentrantLock = commandContext.getSender().getReentrantLock();
        reentrantLock.lock();

        try {
            if (targetUser != null) {
                muteCommandManager.unMuteUser(targetUser, commandContext.getSender());
            } else {
                sendMessage(commandContext.getSender(), USER_NOT_FOUND_MESSAGE);
            }
        } finally {
            reentrantLock.unlock();
        }

    }

    private void sendMessage(CommandSender commandSender ,String message) {

        if (commandSender instanceof UserHandler userHandler){
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







