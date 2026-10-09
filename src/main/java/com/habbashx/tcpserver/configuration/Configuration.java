package com.habbashx.tcpserver.configuration;

import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.Contract;

/**
 * An abstract class that represents a configuration management system. This class provides
 * a blueprint for defining how configurations are accessed and modified.
 */
public abstract class Configuration {

    private final Server serverInstance;

    @Contract(pure = true)
    public Configuration(final Server serverInstance) {
        this.serverInstance = serverInstance;
    }

    public abstract Object returnValue(String element);

    public abstract void modify(String element, String newValue);

    public Server getServerInstance() {
        return serverInstance;
    }
}
