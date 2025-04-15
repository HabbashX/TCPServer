package com.habbashx.tcpserver.delayevent;

import com.habbashx.tcpserver.socket.Server;

public class BroadcastEvent extends DelayEvent {

    private final String broadcastMessage;
    private final Server server;

    public BroadcastEvent(String broadcastMessage,Server server) {
        super("BroadCastEvent");
        this.broadcastMessage = broadcastMessage;
        this.server = server;
    }

    public String getBroadcastMessage() {
        return broadcastMessage;
    }


    public Server getServer() {
        return server;
    }
}
