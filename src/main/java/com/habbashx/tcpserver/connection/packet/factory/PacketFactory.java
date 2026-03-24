package com.habbashx.tcpserver.connection.packet.factory;

import com.habbashx.tcpserver.connection.packet.FilePacket;
import com.habbashx.tcpserver.connection.packet.Packet;
import com.habbashx.tcpserver.connection.packet.TextPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PacketFactory {


    public static TextPacket createText(String message) {
        return new TextPacket(message);
    }

    public static FilePacket createFile(String name, long size, InputStream data) {
        return new FilePacket(name, size, data);
    }

    public static Packet readPacket(DataInputStream input) throws IOException {
        int type = input.readInt();
        return switch (type) {
            case 1 -> {
                int len = input.readInt();
                byte[] data = input.readNBytes(len);
                yield new TextPacket(new String(data));
            }
            case 2 -> {
                int nameLen = input.readInt();
                byte[] nameBytes = input.readNBytes(nameLen);
                String name = new String(nameBytes);
                long fileSize = input.readLong();
                yield new FilePacket(name, fileSize, input);
            }
            default -> throw new IOException("Unknown packet type");
        };
    }


    public static void writePacket(DataOutputStream output, Packet packet) throws IOException {
        switch (packet.getType()) {
            case 1 -> { // TEXT
                TextPacket text = (TextPacket) packet;
                byte[] data = text.message().getBytes();
                output.writeInt(1);
                output.writeInt(data.length);
                output.write(data);
            }
            case 2 -> { // FILE
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
