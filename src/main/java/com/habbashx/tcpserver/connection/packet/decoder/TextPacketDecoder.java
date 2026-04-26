package com.habbashx.tcpserver.connection.packet.decoder;

import com.habbashx.tcpserver.connection.packet.Packet;
import com.habbashx.tcpserver.connection.packet.TextPacket;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Decodes {@link TextPacket} instances from a binary input stream.
 * <p>
 * Expected format:
 * <pre>
 * [int length][byte[length] UTF-8 text]
 * </pre>
 */
public class TextPacketDecoder implements PacketDecoder {

    /**
     * Reads a UTF-8 encoded text packet from the input stream.
     *
     * @param in the input stream containing packet data
     * @return a decoded {@link TextPacket}
     * @throws IOException if reading fails or data is corrupted
     */
    @Override
    public Packet decode(DataInputStream in) throws IOException {
        int length = in.readInt();

        byte[] data = in.readNBytes(length);

        return new TextPacket(
                new String(data, StandardCharsets.UTF_8)
        );
    }
}