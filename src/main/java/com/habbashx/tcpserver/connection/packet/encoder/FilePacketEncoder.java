package com.habbashx.tcpserver.connection.packet.encoder;

import com.habbashx.tcpserver.connection.packet.FilePacket;
import com.habbashx.tcpserver.connection.packet.Packet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Encodes {@link FilePacket} into a binary stream format.
 * <p>
 * Format:
 * <pre>
 * [int type = 2][int nameLength][byte[nameLength] UTF-8 fileName]
 * [long fileSize][file bytes]
 * </pre>
 */
public class FilePacketEncoder implements PacketEncoder {


    /**
     * Writes a file packet into the output stream, including metadata and file data.
     *
     * @param packet the file packet (must be {@link FilePacket})
     * @param out    the output stream to write encoded data
     * @throws IOException if I/O error occurs during encoding
     */
    @Override
    public void encode(Packet packet, DataOutputStream out) throws IOException {

        FilePacket file = (FilePacket) packet;

        byte[] nameBytes = file.fileName().getBytes(StandardCharsets.UTF_8);

        out.writeInt(2);
        out.writeInt(nameBytes.length);
        out.write(nameBytes);
        out.writeLong(file.fileSize());

        try (InputStream in = file.data()) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}