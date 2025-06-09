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
import com.habbashx.tcpserver.user.UserDetails;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;
import static com.habbashx.tcpserver.security.Permission.UN_BAN_PERMISSION;


/**
 * Represents a command to unban a user from the server. This command
 * allows authorized users to lift the ban on a previously banned user.
 * The command is equipped with a cooldown mechanism and logs its execution.
 *
 * Features:
 * - Removes a user's ban if their username is provided and they exist.
 * - Provides messages for usage instructions and error handling.
 * - Thread-safe execution with locking to ensure proper state management.
 * - Utilizes a cooldown system to limit rapid command execution.
 *
 * Command annotation details:
 * - name: "unban"
 * - permission: Default constant `UN_BAN_PERMISSION`.
 * - aliases: {"unbanned", "unblock"}.
 * - description: "block user from joining the server".
 * - cooldownTimeUnit: TimeUnit.SECONDS.
 * - cooldownTime: 10 seconds.
 * - executionLog: true.
 *
 * Dependencies:
 * - BanCommandManager: Manages ban and unban processes.
 * - Server: Provides access to user and server data.
 *
 * Exceptions and validations:
 * - If no username is provided, an error message is sent with the usage format.
 * - If the user is not found, a user-not-found message is sent.
 *
 * Threading:
 * - A reentrant lock is used to ensure thread-safe operations during command execution.
 *
 * Override Methods:
 * - `execute(CommandContext commandContext)`: Executes the unban logic.
 * - `getCooldownManager()`: Retrieves auxiliary cooldown manager from the parent class.
 */
@Command(
        name = "unban",
        permission = UN_BAN_PERMISSION,
        aliases = {"unbanned","unblock"},
        description = "block user from joining the server",
        cooldownTimeUnit = TimeUnit.SECONDS,
        cooldownTime = 10L,
        executionLog = true
)
public final class UnBanCommand extends CommandExecutor {

    private static final String COMMAND_USAGE_MESSAGE = "usage: /unban <username>";
    private static final String USER_NOT_FOUND_MESSAGE = RED+"user is not found"+RESET;

    private final BanCommandManager banCommandManager;
    private final Server server;

    public UnBanCommand(Server server, BanCommandManager banCommandManager) {

        this.server = server;
        this.banCommandManager = banCommandManager;
    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {


        if (commandContext.getArgs().isEmpty()) {
            sendMessage(commandContext.getSender(),USER_NOT_FOUND_MESSAGE);
            return;
        }

        @MayBeEmpty
        String targetUsername = commandContext.getArgs().get(0);
        @Nullable
        UserDetails targetUser = server.getServerDataManager().getUserByUsername(targetUsername);

        final ReentrantLock reentrantLock = commandContext.getSender().getReentrantLock();

        reentrantLock.lock();
        try {
            if (targetUser != null) {
                banCommandManager.unBanUser(targetUser, commandContext.getSender());
            } else {
                sendMessage(commandContext.getSender(), COMMAND_USAGE_MESSAGE);
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
