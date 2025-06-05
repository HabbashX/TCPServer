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
 * Represents the command used to add a permission to a specified user in the system.
 * This command requires a valid username, a permission identifier, and a flag indicating
 * whether the permission is volatile or non-volatile. Volatile permissions are temporary
 * and are not persisted, while non-volatile permissions are stored permanently.
 * <p>
 * The command enforces a cooldown period before it can be executed again by the same user.
 * It also requires the user executing the command to hold a specific permission level
 * to perform this action.
 * <p>
 * Metadata:
 * - Command Name: addpermission
 * - Permission Required: ADD_NEW_PERMISSIONS_PERMISSION
 * - Execution Log: Enabled
 * - Aliases: addp, addpermits, ap
 * - Cooldown Duration: 10 seconds
 * <p>
 * When executed, the command performs the following actions:
 * 1. Validates the provided arguments to ensure that a username, permission, and volatility flag are supplied.
 * 2. Searches for the user by username using the server's data manager.
 * 3. If the user is found:
 * - If the permission is non-volatile, attempts to add it to the user's permanent permission container.
 * - If the addition operation is successful, a success message is sent to the command sender.
 * - Otherwise, a failure message is sent.
 * - For volatile permissions, adds the permission directly without persistence.
 * 4. If the user is not found, sends an error message to the command sender indicating the user does not exist.
 * <p>
 * Error and Success Responses:
 * - If the command is used incorrectly (less than three arguments), a usage message is sent to the sender.
 * - If the specified user does not exist, an error message is displayed.
 * - If the permission is successfully added (either volatile or non-volatile), a success message is displayed.
 * - If adding the permission fails for non-volatile permissions, a failure message is displayed.
 * <p>
 * Notes:
 * - The permission ID must be a numeric value.
 * - The volatility flag must be a boolean value (true/false).
 */
@Command(
        name = "addpermission",
        permission = Permission.ADD_NEW_PERMISSIONS_PERMISSION,
        executionLog = true,
        aliases = {"addp", "addpermits", "ap"},
        cooldownTime = 10L,
        cooldownTimeUnit = TimeUnit.SECONDS
)
public final class AddPermissionCommand extends CommandExecutor {

    private static final String COMMAND_USAGE = "usage: /addpermission <username> <permission> <isVolatile>";
    private static final String USER_NOT_FOUND_MESSAGE = RED + "User not found." + RESET;
    private static final String PERMISSION_ADDED_SUCCESSFULLY = LIME_GREEN + "permission added successfully" + RESET;
    private static final String FAILED_TO_ADD_PERMISSION = RED + "failed to add permission" + RESET;
    private final Server server;

    public AddPermissionCommand(Server server) {
        this.server = server;
    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().size() < 3) {
            commandContext.getSender().printMessage(RED + COMMAND_USAGE + RESET);
            return;
        }

        final var targetUsername = commandContext.getArgs().get(0);
        final var permission = Integer.parseInt(commandContext.getArgs().get(1));
        final var isVolatilePermission = Boolean.parseBoolean(commandContext.getArgs().get(2));

        final UserHandler targetUserHandler = server.getServerDataManager().getOnlineUserByUsername(targetUsername);

        if (targetUserHandler != null) {
            if (!isVolatilePermission) {
                boolean isAdded = targetUserHandler.getNonVolatilePermissionContainer().addPermission(permission);
                if (isAdded) {
                    commandContext.getSender().printMessage(PERMISSION_ADDED_SUCCESSFULLY);
                    return;
                }
                commandContext.getSender().printMessage(FAILED_TO_ADD_PERMISSION);
                return;
            }
            targetUserHandler.addPermission(permission);
            commandContext.getSender().printMessage(PERMISSION_ADDED_SUCCESSFULLY);
            return;
        }
        commandContext.getSender().printMessage(USER_NOT_FOUND_MESSAGE);
    }
}
