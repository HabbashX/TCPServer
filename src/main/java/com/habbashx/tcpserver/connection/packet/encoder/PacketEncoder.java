package com.habbashx.tcpserver.connection.packet.encoder;

import com.habbashx.tcpserver.connection.packet.Packet;
import com.habbashx.tcpserver.connection.packet.decoder.PacketDecoder;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Responsible for encoding a {@link Packet} into raw binary format
 * and writing it to an output stream.
 * <p>
 * Each implementation must ensure that the encoded format matches
 * the corresponding {@link PacketDecoder} implementation.
 */
public interface PacketEncoder {

    /**
     * Encodes the given packet and writes it to the output stream.
     *
     * @param packet the packet to encode
     * @param out    the output stream to write the encoded data to
     * @throws IOException if an I/O error occurs during writing
     */
    void encode(Packet packet, DataOutputStream out) throws IOException;
}