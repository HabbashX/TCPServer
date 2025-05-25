package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.annotation.MayBeEmpty;
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
import static com.habbashx.tcpserver.security.Permission.MUTE_PERMISSION;

/**
 * The MuteCommand class handles the process of muting users in the application.
 * This implementation extends the CommandExecutor class, providing the core
 * functionality for executing the mute command.
 *
 * The command is structured with features such as:
 * - Command annotation to define metadata including name, permission, aliases,
 *   description, cooldown, and execution properties.
 * - Integration with a server implementation to find user information and
 *   manage user states.
 * - Usage of a MuteCommandManager to perform muting-related operations.
 *
 * Features:
 * - Cooldown mechanism to limit the frequency of command usage.
 * - Command execution can be asynchronous to avoid blocking the main thread.
 * - Provides detailed feedback messages for improper usage or if the specified
 *   user cannot be found.
 * - Includes a lock mechanism to ensure thread safety during execution.
 *
 * Note:
 * This mute command is designed to be minimalistic. For additional features,
 * you may need to customize or extend the implementation. The command can also
 * be disabled using the command manager if required.
 */
@Command(
        name = "mute",
        permission = MUTE_PERMISSION,
        aliases ={"curse"},
        description = "mute users",
        cooldownTimeUnit = TimeUnit.SECONDS,
        cooldownTime = 10L,
        isAsync = true,
        executionLog = true,
        note = """
                this mute command made in simple way so it will not support all features and approaches
                if you just want to remove it just use command manager disableCommand method to disable it
                """
)
public final class MuteCommand extends CommandExecutor {

    private static final String COMMAND_USAGE_MESSAGE = "usage: /mute <username>";
    private static final String USER_NOT_FOUND_MESSAGE = RED+"user is not found"+RESET;

    private final Server server;

    /**
     * Manages operations related to the mute command, such as muting and unmuting users,
     * tracking muted users, and maintaining corresponding data persistence.
     * Used to handle the execution logic and functionalities for the "mute" command.
     */
    private final MuteCommandManager muteCommandManager;

    public MuteCommand(Server server ,MuteCommandManager muteCommandManager) {
        this.server = server;
        this.muteCommandManager = muteCommandManager;
    }

    /**
     * Executes the mute command for a given context. This method handles the logic to mute a target user
     * specified in the command arguments. If no arguments are provided, a usage message is sent back to
     * the command sender. It also ensures that the sender cannot mute themselves and locks the command
     * execution to prevent concurrent modifications.
     *
     * @param commandContext the context of the executed command, which contains the sender information and
     *                       command arguments. It is mandatory and cannot be null.
     */
    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().isEmpty()) {
            sendMessage(commandContext.getSender(), COMMAND_USAGE_MESSAGE);
            return;
        }

        final ReentrantLock reentrantLock = commandContext.getSender().getReentrantLock();

        @MayBeEmpty
        final String targetUsername = commandContext.getArgs().get(0);
        @Nullable
        final UserHandler target = server.getServerDataManager().getOnlineUserByUsername(targetUsername);

        reentrantLock.lock();
        try {
            if (target != null) {
                if (commandContext.getSender() instanceof UserHandler userHandler) {
                    String senderUsername = userHandler.getUserDetails().getUsername();


                    if (senderUsername.equals(targetUsername)) {
                        sendMessage(commandContext.getSender(), RED + "you cannot ban yourself." + RESET);
                        return;
                    }
                }
                muteCommandManager.muteUser(target, commandContext.getSender());
            } else {
                sendMessage(commandContext.getSender(), USER_NOT_FOUND_MESSAGE);
            }

        } finally {
            reentrantLock.unlock();
        }
    }

    private void sendMessage(CommandSender commandSender ,String message) {

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
