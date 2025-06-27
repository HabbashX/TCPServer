package com.habbashx.tcpserver.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The {@code ClientUtils} class provides utility methods for managing client settings.
 * It includes functionality to generate a client settings file if it does not already exist.
 * The settings file contains the host and port configuration for the client connection.
 */
public class ClientUtils {

    /**
     * The path to the client settings file.
     * This file contains configuration properties for the client connection.
     */
    public static final String CLIENT_SETTINGS_PATH = "connection/settings/connection.properties";

    /**
     * Generates a client settings file at the specified path if it does not already exist.
     * The file will contain default host and port settings for the client.
     *
     * @throws RuntimeException if an I/O error occurs while creating the file or writing to it.
     */
    public static void generateClientSettingsFile() {

        Path path = Path.of(CLIENT_SETTINGS_PATH);

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                FileWriter writer = new FileWriter(CLIENT_SETTINGS_PATH);

                writer.write("""
                        client.host=127.0.0.1
                        client.port=8080
                        """);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
