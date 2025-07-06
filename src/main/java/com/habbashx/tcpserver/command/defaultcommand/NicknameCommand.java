package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.annotation.MayBeEmpty;
import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.util.UserUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;
import static com.habbashx.tcpserver.security.Permission.NICKNAME_PERMISSION;

/**
 * Represents a command to update a user's nickname in the system. This command
 * can only be executed by a user, and not from the console. It validates the
 * provided nickname to ensure it is free of invalid characters, such as symbols
 * or spaces, and applies a cooldown period after execution.
 *
 * The class extends CommandExecutor and overrides the execute method
 * to handle the execution logic specific to the "nickname" command. The cooldown
 * mechanism, command usage validation, and response messages are managed within
 * this class.
 *
 * Attributes:
 * - This command requires the {@code NICKNAME_PERMISSION} to be executed.
 * - It enforces a cooldown of 60 seconds between executions.
 * - The command runs asynchronously.
 * - Execution logs are recorded for this command.
 *
 * Usage:
 * The command expects a single argument representing the desired nickname. If the
 * argument is missing or invalid, an appropriate usage or error message is sent to
 * the user.
 */
@Command(
        name = "nickname",
        permission = NICKNAME_PERMISSION,
        cooldownTimeUnit = TimeUnit.SECONDS,
        cooldownTime = 60L,
        isAsync = true,
        executionLog = true
)
public class NicknameCommand extends CommandExecutor {

    private static final String COMMAND_USAGE = "usage: /nickname <nickname>";
    private static final String INVALID_USERNAME_MESSAGE = RED + "invalid username please try to select name without any symbols and spaces" + RESET;
    private static final String CONSOLE_EXECUTED_COMMAND_WARNING_MESSAGE = RED+"this command cannot be executed by console"+RESET;

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getSender() instanceof final UserHandler userHandler) {

            final ReentrantLock reentrantLock = userHandler.getReentrantLock();

            reentrantLock.lock();

            try {
                if (commandContext.getArgs().isEmpty()) {
                    userHandler.sendMessage(COMMAND_USAGE);
                    return;
                }

                @MayBeEmpty final String nickname = commandContext.getArgs().get(0);

                if (!UserUtil.isValidUsername(nickname)) {
                    userHandler.getUserDetails().setUsername(nickname);
                } else {
                    userHandler.sendMessage(INVALID_USERNAME_MESSAGE);
                }

            } finally {
                reentrantLock.unlock();
            }
            } else {
                System.out.println(CONSOLE_EXECUTED_COMMAND_WARNING_MESSAGE);
            }

    }
    @Override
    public CooldownManager getCooldownManager() {
        return super.getCooldownManager();
    }
}
