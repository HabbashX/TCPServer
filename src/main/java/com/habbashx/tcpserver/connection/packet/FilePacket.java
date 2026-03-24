package com.habbashx.tcpserver.connection.packet;

import java.io.InputStream;

public record FilePacket(String fileName, long fileSize, InputStream data) implements Packet {

    @Override
    public int getType() {
        return Protocol.FILE_TRANSFEREE_PROTOCOL;
    }
}
