package com.habbashx.tcpserver.connection.packet.decoder;

import com.habbashx.tcpserver.connection.packet.Packet;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Responsible for decoding raw binary data from an input stream
 * into a structured {@link Packet} object.
 * <p>
 * Implementations of this interface must strictly follow the
 * expected binary protocol format for each packet type.
 * <p>
 * This interface is typically used in network I/O layers where
 * incoming data must be converted into high-level packet objects.
 */
public interface PacketDecoder {

    /**
     * Decodes a packet from the provided input stream.
     *
     * @param input the data input stream containing raw packet bytes
     * @return a decoded {@link Packet} instance
     * @throws IOException if an I/O error occurs while reading from the stream
     *                     or if the stream contains invalid/corrupted data
     */
    Packet decode(DataInputStream input) throws IOException;
}
