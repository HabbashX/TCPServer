package com.habbashx.tcpserver.socket.client.settings;

import com.habbashx.annotation.InjectProperty;


/**
 * The {@code ClientSettings} class is responsible for managing the configuration settings
 * for a TCP client. It uses dependency injection to retrieve the host and port values
 * from a settings file, allowing for easy configuration of the client's connection parameters.
 * The default host is set to "localhost" if not specified otherwise in the settings file.
 */
public class ClientSettings {

    /**
     * represent the value of defined host in settings file
     * If not defined, it will be set to "localhost" by default.
     */
    @InjectProperty("client.host")
    private String host;

    /**
     * represent the value of defined port in settings file
     * If not defined, it will be set to 8080 by default.
     */
    @InjectProperty("client.port")
    private int port;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
