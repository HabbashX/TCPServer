package com.habbashx.tcpserver.connection.packet.factory;

import com.habbashx.tcpserver.connection.packet.Packet;
import com.habbashx.tcpserver.connection.packet.TextPacket;
import com.habbashx.tcpserver.connection.packet.registry.PacketRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketFactory {


    public static @NotNull TextPacket createText(final String message) {
        return new TextPacket(message);
    }

    public static Packet readPacket(DataInputStream input) throws IOException {
        int type = input.readInt();
        return PacketRegistry.decode(type, input);
    }


    public static void writePacket(DataOutputStream out, Packet packet) throws IOException {
        PacketRegistry.encode(packet, out);
        out.flush();
    }
}
