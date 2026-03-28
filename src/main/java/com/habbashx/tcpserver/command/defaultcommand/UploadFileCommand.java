package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.connection.packet.FilePacket;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Command to initiate a file upload sequence.
 * This command validates the request and prepares the server to receive
 * binary data through the PacketFactory system.
 */
@Command(
        name = "upload",
        cooldownTime = 30,
        cooldownTimeUnit = TimeUnit.SECONDS,
        executionLog = true
)
public class UploadFileCommand extends CommandExecutor {

    @Override
    public void execute(@NotNull final CommandContext context) {

        if (context.getSender() instanceof final UserHandler userHandler) {

            if (context.getArgs().isEmpty()) {
                userHandler.sendTextMessage("please provide the full file name");
            }
            final String fileName = context.getArgs().getFirst();
            final File file = new File("server_storage/" + fileName);

            if (!file.exists()) {
                userHandler.sendTextMessage("Error: File not found.");
                return;
            }

            try {
                FileInputStream fis = new FileInputStream(file);
                FilePacket packet = new FilePacket(file.getName(), file.length(), fis);

                userHandler.sendPacket(packet);

                userHandler.sendTextMessage("Starting download of " + file.getName());
            } catch (FileNotFoundException ignored) {
                userHandler.sendTextMessage("cannot find file with this name , make sure to enter the right file name with his format");
            }
        }
    }

    private void sendMessage(CommandSender commandSender, String message) {

        if (commandSender instanceof final UserHandler userHandler) {
            userHandler.sendTextMessage(message);
        } else {
            System.out.println(message);
        }
    }
}