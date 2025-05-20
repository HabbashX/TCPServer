package com.habbashx.tcpserver.delayevent;

import com.habbashx.tcpserver.socket.Server;

/**
 * The BroadcastEvent class represents an event to broadcast a message to a server.
 * This class extends the DelayEvent class and contains information about the
 * message to be broadcasted and the target server.
 */
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
