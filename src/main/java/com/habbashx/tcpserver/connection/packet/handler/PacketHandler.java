package com.habbashx.tcpserver.connection.packet.handler;

import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.connection.packet.Packet;

import java.io.IOException;

public interface PacketHandler<T extends Packet> {

    void handle(T t, UserHandler userHandler) throws IOException;
}
