package com.habbashx.tcpserver.connection.packet;

public record TextPacket(String message) implements Packet {

    @Override
    public int getType() {
        return Protocol.PLAIN_TEXT_PROTOCOL;
    }
}
