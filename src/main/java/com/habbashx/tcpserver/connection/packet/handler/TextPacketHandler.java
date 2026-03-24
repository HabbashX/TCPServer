package com.habbashx.tcpserver.connection.packet.handler;

import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.connection.packet.TextPacket;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TextPacketHandler implements PacketHandler<TextPacket> {


    @Override
    public void handle(@NotNull final TextPacket textPacket, @NotNull final UserHandler userHandler) throws IOException {

        final String message = textPacket.message();

        if (message.startsWith("/")) {
            userHandler.getServer().getCommandManager().executeCommand(
                    userHandler.getUserDetails().getUsername(),
                    message,
                    userHandler
            );
        }
    }
}
