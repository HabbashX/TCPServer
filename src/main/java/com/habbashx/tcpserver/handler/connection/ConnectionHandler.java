package com.habbashx.tcpserver.handler.connection;

import com.habbashx.tcpserver.security.Permissible;
import com.habbashx.tcpserver.socket.Server;

import javax.net.ssl.SSLSocket;


public abstract class ConnectionHandler implements Permissible, Runnable {

    private final Server server;
    private final SSLSocket socket;

    public ConnectionHandler(SSLSocket sslSocket, Server server) {
        this.server = server;
        this.socket = sslSocket;
    }

    /**
     * Retrieves the type of the handler represented by the implementing class.
     * The handler type usually indicates the specific role or functionality
     * the handler provides within the connection management process.
     *
     * @return a string representation of the handler type.
     */
    public abstract String getHandlerType();

    /**
     * Retrieves a description of the handler. The description typically provides
     * meaningful information about the purpose or functionality of the handler.
     *
     * @return a string containing the description of the handler.
     */
    public abstract String getHandlerDescription();

    public Server getServer() {
        return server;
    }

    public SSLSocket getUserSocket() {
        return socket;
    }
}
