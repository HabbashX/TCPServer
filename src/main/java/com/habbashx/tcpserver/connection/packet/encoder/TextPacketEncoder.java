package com.habbashx.tcpserver.connection.packet.encoder;

import com.habbashx.tcpserver.connection.packet.Packet;
import com.habbashx.tcpserver.connection.packet.TextPacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Encodes {@link TextPacket} into a binary format suitable for network transmission.
 * <p>
 * Format:
 * <pre>
 * [int type = 1][int length][byte[length] UTF-8 text]
 * </pre>
 */
public class TextPacketEncoder implements PacketEncoder {

    /**
     * Encodes a text packet and writes it to the output stream.
     *
     * @param packet the packet to encode (must be {@link TextPacket})
     * @param out    the output stream to write encoded data to
     * @throws IOException if writing fails or packet type is invalid
     */
    @Override
    public void encode(Packet packet, DataOutputStream out) throws IOException {

        TextPacket text = (TextPacket) packet;

        byte[] data = text.message().getBytes(StandardCharsets.UTF_8);

        out.writeInt(1);
        out.writeInt(data.length);
        out.write(data);
    }
}