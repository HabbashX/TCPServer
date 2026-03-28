package com.habbashx.tcpserver.connection.packet.factory;

import com.habbashx.tcpserver.connection.packet.FilePacket;
import com.habbashx.tcpserver.connection.packet.Packet;
import com.habbashx.tcpserver.connection.packet.TextPacket;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PacketFactory {


    public static @NotNull TextPacket createText(final String message) {
        return new TextPacket(message);
    }

    public static Packet readPacket(java.io.DataInputStream input) throws IOException {
        int type = input.readInt();
        switch (type) {
            case 1 -> { // TEXT
                int length = input.readInt();
                byte[] data = new byte[length];
                input.readFully(data);
                return new TextPacket(new String(data));
            }
            case 2 -> { // FILE
                int nameLength = input.readInt();
                byte[] nameBytes = new byte[nameLength];
                input.readFully(nameBytes);
                String fileName = new String(nameBytes);

                long size = input.readLong();
                InputStream fileData = input; // stream of the rest
                return new FilePacket(fileName, size, fileData);
            }
            default -> throw new IOException("Unknown packet type: " + type);
        }
    }

 
    public static void writePacket(DataOutputStream output, Packet packet) throws IOException {
        switch (packet.getType()) {
            case 1 -> {
                TextPacket text = (TextPacket) packet;
                byte[] data = text.message().getBytes();
                output.writeInt(1);
                output.writeInt(data.length);
                output.write(data);
            }
            case 2 -> {
                FilePacket file = (FilePacket) packet;
                byte[] nameBytes = file.fileName().getBytes();
                output.writeInt(2);
                output.writeInt(nameBytes.length);
                output.write(nameBytes);
                output.writeLong(file.fileSize());

                InputStream fileData = file.data();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = fileData.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }
            default -> throw new IOException("Unknown packet type: " + packet.getType());
        }
        output.flush();
    }
}
