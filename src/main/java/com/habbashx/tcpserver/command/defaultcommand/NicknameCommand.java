package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.annotation.PossibleEmpty;
import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.util.UserUtil;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;
import static com.habbashx.tcpserver.security.Permission.NICKNAME_PERMISSION;

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

        if (commandContext.getSender() instanceof UserHandler userHandler) {

            if (commandContext.getArgs().isEmpty()) {
                userHandler.sendMessage(COMMAND_USAGE);
                return;
            }

            @PossibleEmpty
            final String nickname = commandContext.getArgs().get(0);

            if (!UserUtil.isValidUsername(nickname)) {
                userHandler.getUserDetails().setUsername(nickname);
            } else {
                userHandler.sendMessage(INVALID_USERNAME_MESSAGE);
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
