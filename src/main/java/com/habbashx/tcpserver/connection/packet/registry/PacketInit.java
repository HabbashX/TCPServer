package com.habbashx.tcpserver.connection.packet.registry;

import com.habbashx.tcpserver.connection.packet.decoder.FilePacketDecoder;
import com.habbashx.tcpserver.connection.packet.decoder.TextPacketDecoder;
import com.habbashx.tcpserver.connection.packet.encoder.FilePacketEncoder;
import com.habbashx.tcpserver.connection.packet.encoder.TextPacketEncoder;


/**
 * Responsible for initializing and registering all packet codecs
 * used by the server networking system.
 * <p>
 * This class acts as a bootstrap entry point for the packet system,
 * mapping packet IDs to their corresponding encoder and decoder implementations.
 * <p>
 * Must be called once during server startup before any packet processing begins.
 */
public class PacketInit {

    /**
     * Registers all available packet encoders and decoders in the system.
     * <p>
     * This method should only be called once during application initialization.
     * Registering packets multiple times may overwrite existing mappings or
     * cause inconsistent behavior.
     */
    public static void init() {

        PacketRegistry.register(
                1,
                new TextPacketDecoder(),
                new TextPacketEncoder()
        );

        PacketRegistry.register(
                2,
                new FilePacketDecoder(),
                new FilePacketEncoder()
        );
    }
}