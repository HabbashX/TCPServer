package com.habbashx.tcpserver.util;

import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for server-related constants and methods.
 * This class provides helper utilities for server configuration and settings.
 *
 * The class is designed for static access and should not be instantiated.
 */
public class ServerUtils {
    /**
     * Represents the file path to the properties file used
     * for configuring server settings.
     * This constant points to a file named "settings.properties"
     * located in the "settings" directory.
     */
    public static final String SERVER_SETTINGS_PATH = "settings/settings.properties";

    public static String getAuthStorageType(@NotNull Server server) {
        return server.getServerSettings().getAuthStorageType();
    }
}
