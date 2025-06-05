package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.security.Permission;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.*;

/**
 * Implements the command to remove a permission for a specified user.
 * The command supports both volatile and non-volatile permissions, where volatile
 * permissions are temporary and non-volatile permissions are persisted.
 * <p>
 * This command leverages the {@link Command} annotation to define its metadata, including
 * names and aliases for invocation, required permissions, logging preferences, and cooldown settings.
 * <p>
 * The command requires at least three arguments passed during execution:
 * - The target user's username.
 * - The permission ID to be removed.
 * - A boolean flag indicating whether the permission is volatile.
 * <p>
 * If the target user is found and the specified permission is successfully removed, a success
 * message is sent to the command executor. Otherwise, appropriate error messages are displayed.
 * <p>
 * Cooldown times are enforced as configured in the {@link Command} metadata. This prevents
 * the command from being executed repeatedly within a short period by the same user.
 * <p>
 * This class is an extension of the {@link CommandExecutor} abstract class, implementing
 * its {@link #execute(CommandContext)} method to define the specific command logic.
 * <p>
 * Constructor Behavior:
 * - Instantiates the command with a reference to the server instance for retrieving user data
 * and invoking necessary server-side operations.
 * <p>
 * Command Annotations:
 * - Name: "removepermission"
 * - Aliases: {"removep", "removepermit", "rp"}
 * - Permission: Requires a specific permission level defined as {@link Permission#REMOVE_PERMISSIONS_PERMISSION}.
 * - Execution Log: Enabled for auditing purposes.
 * - Cooldown: 10 seconds per user.
 */
@Command(
        name = "removepermission",
        permission = Permission.REMOVE_PERMISSIONS_PERMISSION,
        aliases = {"removep", "removepermit", "rp"},
        executionLog = true,
        cooldownTime = 10L,
        cooldownTimeUnit = TimeUnit.SECONDS
)
public final class RemovePermissionCommand extends CommandExecutor {

    private static final String COMMAND_USAGE = "usage: /removepermission <username> <permission> <isVolatile>";
    private static final String PERMISSION_REMOVED = LIME_GREEN + "permission removed successfully." + RESET;
    private static final String PERMISSION_NOT_REMOVED = RED + "permission not removed." + RESET;
    private static final String USER_NOT_FOUND = RED + "User not found." + RESET;
    private final Server server;

    public RemovePermissionCommand(Server server) {
        this.server = server;
    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().size() < 3) {
            commandContext.getSender().printMessage(COMMAND_USAGE);
            return;
        }

        final var username = commandContext.getArgs().get(0);
        final var permission = Integer.parseInt(commandContext.getArgs().get(1));
        final var isVolatile = Boolean.parseBoolean(commandContext.getArgs().get(2));

        UserHandler targetUserHandler = server.getServerDataManager().getOnlineUserByUsername(username);

        if (targetUserHandler != null) {

            if (!isVolatile) {
                boolean isRemoved = targetUserHandler.getNonVolatilePermissionContainer().removePermission(permission);

                if (isRemoved) {
                    commandContext.getSender().printMessage(PERMISSION_REMOVED);
                    return;
                }
                commandContext.getSender().printMessage(PERMISSION_NOT_REMOVED);
                return;
            }
            targetUserHandler.removePermission(permission);
            commandContext.getSender().printMessage(PERMISSION_REMOVED);
            return;
        }
        commandContext.getSender().printMessage(USER_NOT_FOUND);
    }
}
