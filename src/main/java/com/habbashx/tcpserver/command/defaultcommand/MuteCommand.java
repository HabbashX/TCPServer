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
import static com.habbashx.tcpserver.security.Permission.MUTE_PERMISSION;

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

    private final MuteCommandManager muteCommandManager;

    public MuteCommand(Server server ,MuteCommandManager muteCommandManager) {
        this.server = server;
        this.muteCommandManager = muteCommandManager;
    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().isEmpty()) {
            sendMessage(commandContext.getSender(), COMMAND_USAGE_MESSAGE);
            return;
        }

        final ReentrantLock reentrantLock = commandContext.getSender().getReentrantLock();

        @PossibleEmpty
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
