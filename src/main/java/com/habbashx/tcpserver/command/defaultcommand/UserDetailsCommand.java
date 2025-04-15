package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.annotation.PossibleEmpty;
import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.socket.Server;
import com.habbashx.tcpserver.user.UserDetails;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Year;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;


@Command(
        name = "find",
        cooldownTimeUnit = TimeUnit.SECONDS,
        cooldownTime = 10L,
        executionLog = true
)
public class UserDetailsCommand extends CommandExecutor {

    private static final String COMMAND_USAGE_MESSAGE = "usage: /find <username|id>";

    private static final String ADMINISTRATOR_USER_DETAILS_MESSAGE = """
            User found !
            userIP: %s
            userID: %s
            userRole: %s
            username: %s
            userEmail: %s
            phoneNumber: %s
            isActiveAccount: %s
            """;

    private static final String USER_DETAILS_MESSAGE = """
            userID: %s
            userRole: %s
            username: %s
            """;

    private static final String USER_NOT_FOUND_MESSAGE = RED+"user not found"+RESET;

    private final Server server;

    public UserDetailsCommand(Server server) {
        this.server = server;
    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().isEmpty()) {
            sendMessage(commandContext.getSender() , COMMAND_USAGE_MESSAGE);
        }

        try {
            @PossibleEmpty
            final String target = commandContext.getArgs().get(0);

            @Nullable
            UserDetails userDetails;

            final boolean targetID = target.startsWith(String.valueOf(Year.now()));
            if (!target.isEmpty()) {
                if (!targetID) {
                    userDetails = server.getServerDataManager().getUserByUsername(target);
                } else {
                    userDetails = server.getServerDataManager().getUserById(target);
                }
                if (commandContext.getSender() instanceof UserHandler userHandler) {
                    Role userRole = userHandler.getUserDetails().getUserRole();
                    sendUserDetails(userRole, userDetails, userHandler);
                } else {
                    sendUserDetails(Role.SUPER_ADMINISTRATOR, userDetails, commandContext.getSender());
                }
            } else {
                sendMessage(commandContext.getSender(), "username or id field may not be empty");
            }
        } catch (IndexOutOfBoundsException ignore) {
            sendMessage(commandContext.getSender(),COMMAND_USAGE_MESSAGE);
        }
    }

    private void sendUserDetails(Role commandSenderRole ,UserDetails userDetails , CommandSender sender) {

        if (userDetails != null) {
            if (commandSenderRole.equals(Role.SUPER_ADMINISTRATOR) || commandSenderRole.equals(Role.ADMINISTRATOR)) {
                sendMessage(sender,ADMINISTRATOR_USER_DETAILS_MESSAGE.formatted(
                        userDetails.getUserIP(),
                        userDetails.getUserID(),
                        userDetails.getUserRole().toString(),
                        userDetails.getUsername(),
                        userDetails.getUserEmail(),
                        userDetails.getPhoneNumber(),
                        userDetails.isActiveAccount()
                ));
            } else {
                sendMessage(sender,USER_DETAILS_MESSAGE.formatted(
                        userDetails.getUserID(),
                        userDetails.getUserRole().toString(),
                        userDetails.getUsername()
                ));
            }
        } else {
            sendMessage(sender, USER_NOT_FOUND_MESSAGE);
        }
    }
    private void sendMessage(CommandSender commandSender , String message) {

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
