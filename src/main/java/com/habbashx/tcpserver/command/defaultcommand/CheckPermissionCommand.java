package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.security.Permission;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * Represents the "checkpermissions" command, used to verify user permissions within the system.
 * The command checks user permissions, distinguishing between volatile and non-volatile
 * permissions. Additionally, it provides detailed information about user permission containers.
 * <p>
 * This class extends the {@link CommandExecutor} abstract class, implementing the logic for
 * executing the "checkpermissions" command in the defined {@link #execute(CommandContext)} method.
 * The command's behavior and configuration are defined using the {@link Command} annotation.
 * <p>
 * Key Features:
 * - Checks if a user has a specific permission.
 * - Differentiates between volatile and non-volatile permissions.
 * - Outputs comprehensive permission details for associated users.
 * <p>
 * The command expects the following arguments:
 * 1. `username` - The name of the user whose permissions are to be checked.
 * 2. `permission` - The specific permission to check, represented as a numeric string. Use "0"
 * to fetch all permissions associated with the user.
 * 3. `isVolatile` - Boolean flag indicating whether the operation targets volatile permissions.
 * <p>
 * An example usage string is provided in the constant {@code COMMAND_USAGE}.
 * <p>
 * An appropriate error message is displayed if:
 * - The user does not exist on the server.
 * - The required arguments are not provided.
 */
@Command(
        name = "checkpermissions",
        permission = Permission.CHECK_PERMISSIONS_PERMISSION,
        executionLog = true,
        cooldownTime = 10,
        cooldownTimeUnit = TimeUnit.SECONDS
)
public final class CheckPermissionCommand extends CommandExecutor {

    private static final String COMMAND_USAGE = "usage: /checkpermissions <username> <permission> <isVolatile>";
    private static final String USER_NOT_FOUND = RED + "User not found." + RESET;

    private final Server server;

    public CheckPermissionCommand(Server server) {
        this.server = server;
    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().size() < 3) {
            commandContext.getSender().printMessage(COMMAND_USAGE);
            return;
        }

        final var username = commandContext.getArgs().get(0);
        final var permission = commandContext.getArgs().get(1);
        final var isVolatile = Boolean.parseBoolean(commandContext.getArgs().get(2));

        final var userHandler = server.getServerDataManager().getOnlineUserByUsername(username);

        if (userHandler != null) {

            if (permission.equals("0")) {

                if (!isVolatile) {

                    List<Integer> permissions = userHandler.getNonVolatilePermissionContainer().getPermissions();
                    if (permissions != null) {
                        commandContext.getSender().printMessage(userHandler.getNonVolatilePermissionContainer().getPermissions().toString());
                    }
                    return;
                }

                commandContext.getSender().printMessage(userHandler.getHandlerPermissions().toString());
            } else {
                if (!isVolatile) {
                    boolean hasPermission = userHandler.getNonVolatilePermissionContainer()
                            .hasPermission(Integer.parseInt(permission));

                    commandContext.getSender().printMessage("has permission: " + hasPermission);
                    return;
                }
                boolean hasPermission = userHandler.hasVolatilePermission(Integer.parseInt(permission));
                commandContext.getSender().printMessage("has permission: " + hasPermission);
            }
            return;
        }
        commandContext.getSender().printMessage(USER_NOT_FOUND);

    }
}
