package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.annotation.MayBeEmpty;
import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.command.manager.BanCommandManager;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;
import static com.habbashx.tcpserver.security.Permission.BAN_PERMISSION;

/**
 * The BanCommand class represents a command to ban a user from joining the server.
 * This class extends the CommandExecutor abstract class and is annotated with the {@code @Command}
 * annotation to provide metadata about its name, aliases, permission, cooldown, and execution logging.
 *
 * Responsibilities:
 * - Executes the user banning functionality when invoked, validating input arguments and user status.
 * - Prevents users from banning themselves.
 * - Sends appropriate feedback messages to the command sender.
 * - Handles concurrency using reentrant locks to ensure thread safety during execution.
 * - Integrates with the {@code BanCommandManager} to process and enforce bans.
 *
 * Annotations:
 * - {@code @Command}: Specifies the command name as "ban," provides alternative aliases, defines the required
 *   permission level, sets the description, cooldown time, and ensures execution logging.
 *
 * Thread Safety:
 * - Implements thread safety by acquiring a {@link ReentrantLock} from the command sender during execution.
 *
 * Key Components:
 * - {@code execute(CommandContext commandContext)}: Processes the command invocation, validates user input,
 *   and applies the ban to the specified target user if all conditions are met.
 * - {@code sendMessage(CommandSender commandSender, String message)}: Utility function to send messages
 *   to the command sender, supporting both user handlers and console output.
 * - {@code CooldownManager getCooldownManager()}: Retrieves the associated cooldown manager for this command.
 *
 * Usage Conditions:
 * - The command expects exactly one argument representing the target username to ban.
 * - The sender must not be attempting to ban themselves.
 * - The target user must exist or be online on the server.
 *
 * Dependencies:
 * - {@code Server}: Facilitates access to server-related services and online user retrieval.
 * - {@code BanCommandManager}: Handles the logic for banning users.
 * - {@code CommandContext}: Encapsulates information about the command invocation, including arguments
 *   and the sender.
 */
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

    /**
     * Manages the banning operations for the {@link BanCommand} execution.
     * Responsible for handling user bans, unbans, and the management of banned user records.
     * Provides utility methods for checking if a user is banned and retrieving the list of banned users.
     * Typically used to enforce banning functionality within the server's command execution system.
     */
    private final BanCommandManager banManager;

    public BanCommand(Server server ,BanCommandManager banManager) {
        this.server = server;
        this.banManager = banManager;
    }

    /**
     * Executes the "ban" command to ban a specified user from the server.
     * Validates the provided arguments and ensures that the command is
     * executed in a thread-safe manner. If no arguments are provided, it
     * sends a usage message to the command sender. Attempts to ban the
     * target user if the user is online and not the same as the sender.
     * Sends appropriate messages depending on the outcome.
     *
     * @param commandContext the context of the command execution, containing
     *                       the sender, arguments, and additional metadata
     */
    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().isEmpty()) {
            sendMessage(commandContext.getSender(),COMMAND_USAGE_MESSAGE);
            return;
        }

        @MayBeEmpty
        final String targetUsername = commandContext.getArgs().get(0);
        @Nullable
        final UserHandler targetUser = server.getServerDataManager().getOnlineUserByUsername(targetUsername);

        final ReentrantLock reentrantLock = commandContext.getSender().getReentrantLock();

        reentrantLock.lock();

        try {
            if (targetUser != null) {
                if (commandContext.getSender() instanceof UserHandler userHandler) {
                    String username = userHandler.getUserDetails().getUsername();

                    if (username.equals(targetUsername)) {
                        sendMessage(commandContext.getSender(), CANNOT_BAN_SELF_MESSAGE);
                        return;
                    }
                }
                banManager.banUser(targetUser, commandContext.getSender());
            } else {
                sendMessage(commandContext.getSender(), USER_NOT_FOUND_MESSAGE);
            }

        } finally {
            reentrantLock.unlock();
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