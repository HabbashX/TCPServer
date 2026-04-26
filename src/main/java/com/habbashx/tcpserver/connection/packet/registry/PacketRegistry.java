package com.habbashx.tcpserver.connection.packet.registry;

import com.habbashx.tcpserver.connection.packet.Packet;
import com.habbashx.tcpserver.connection.packet.decoder.PacketDecoder;
import com.habbashx.tcpserver.connection.packet.encoder.PacketEncoder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry responsible for managing packet encoding and decoding logic.
 * <p>
 * Each packet type is identified by a unique integer ID, which maps to
 * its corresponding {@link PacketDecoder} and {@link PacketEncoder}.
 * <p>
 * This class acts as the core of the networking serialization layer,
 * ensuring that packets can be correctly transformed between raw binary
 * data and structured objects.
 */
public class PacketRegistry {

    /**
     * Stores packet decoders mapped by packet ID.
     */
    private static final Map<Integer, PacketDecoder> DECODERS = new ConcurrentHashMap<>();

    /**
     * Stores packet encoders mapped by packet ID.
     */
    private static final Map<Integer, PacketEncoder> ENCODERS = new ConcurrentHashMap<>();

    /**
     * Registers a packet type with its corresponding encoder and decoder.
     *
     * @param id      unique packet identifier
     * @param decoder the decoder used to convert raw data into a packet object
     * @param encoder the encoder used to serialize a packet into raw data
     */
    public static void register(int id, PacketDecoder decoder, PacketEncoder encoder) {
        DECODERS.put(id, decoder);
        ENCODERS.put(id, encoder);
    }

    /**
     * Decodes a packet from the input stream using the registered decoder.
     *
     * @param id packet type identifier
     * @param in input stream containing raw packet data
     * @return decoded {@link Packet} instance
     * @throws IOException          if reading from the stream fails or data is invalid
     * @throws NullPointerException if no decoder is registered for the given ID
     */
    public static Packet decode(int id, DataInputStream in) throws IOException {
        return DECODERS.get(id).decode(in);
    }

    /**
     * Encodes a packet into binary format using the registered encoder.
     *
     * @param packet the packet to encode
     * @param out    output stream to write encoded data
     * @throws IOException          if writing to the stream fails
     * @throws NullPointerException if no encoder is registered for the packet type
     */
    public static void encode(Packet packet, DataOutputStream out) throws IOException {
        ENCODERS.get(packet.getType()).encode(packet, out);
    }
}