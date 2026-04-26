package com.habbashx.tcpserver.connection.packet.decoder;

import com.habbashx.tcpserver.connection.packet.FilePacket;
import com.habbashx.tcpserver.connection.packet.Packet;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Decodes {@link FilePacket} instances from a binary input stream.
 * <p>
 * Expected format:
 * <pre>
 * [int nameLength][byte[nameLength] UTF-8 fileName][long fileSize][file data stream]
 * </pre>
 * <p>
 * ⚠ The returned packet contains a live stream reference for file data.
 * The caller is responsible for consuming and closing it.
 */
public class FilePacketDecoder implements PacketDecoder {

    /**
     * Decodes a file packet, including metadata and streaming file content.
     *
     * @param in the input stream containing packet data
     * @return a {@link FilePacket} with file metadata and data stream
     * @throws IOException if reading fails or stream is corrupted
     */
    @Override
    public Packet decode(DataInputStream in) throws IOException {

        int nameLength = in.readInt();
        byte[] nameBytes = in.readNBytes(nameLength);
        String fileName = new String(nameBytes, StandardCharsets.UTF_8);

        long size = in.readLong();


        return new FilePacket(fileName, size, in);
    }
}