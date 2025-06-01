package com.habbashx.tcpserver.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.habbashx.tcpserver.socket.Server;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A subclass of Configuration that handles configuration files stored in JSON format.
 * This class provides functionality to parse JSON files and interact with configuration data.
 */
@SuppressWarnings("unchecked")
public final class JsonConfiguration extends Configuration {

    private final File configurationFile;
    private final Map<String,Object> configData;


    /**
     * Constructs a new JsonConfiguration, initializing it by loading and parsing the specified
     * JSON configuration file. If an error occurs while reading or parsing the file,
     * a RuntimeException is thrown with the error cause.
     *
     * @param configurationFile The path to the JSON configuration file as a non-null string.
     *                          This file should contain valid JSON content.
     * @param server            An instance of the Server class used for logging errors that might
     *                          occur during file reading or parsing.
     */
    public JsonConfiguration(@NotNull String configurationFile , Server server) {
        this.configurationFile = new File(configurationFile);
        try {
            ObjectMapper mapper = new ObjectMapper();
            configData = mapper.readValue(this.configurationFile,Map.class);
        } catch (IOException e) {
            server.getServerLogger().error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the value of a configuration element specified by a dotted path string.
     * The method traverses the configuration data hierarchy using the provided path and
     * returns the corresponding value if found. If the path cannot be fully resolved,
     * the method returns null.
     *
     * @param node the dotted path representing the hierarchy of the desired configuration
     *             element. Each segment of the path represents a key in a nested map structure.
     * @return the value associated with the specified path, or null if the path cannot be resolved.
     */
    @Override
    public @Nullable Object returnValue(@NotNull String node) {

        @NotNull
        @Language("RegExp")
        final String[] pathParts = node.split("\\.");

        Map<String, Object> currentNode = configData;

        for (String part : pathParts) {
            Object value = currentNode.get(part);

            if (value instanceof Map) {
                currentNode = (Map<String, Object>) value;
            } else {
                return value;
            }
        }
        return null;
    }

    /**
     * Modifies the configuration data by setting the specified node to the given value.
     *
     * @param node the path to the configuration node in dot notation (e.g., "parent.child.key").
     *             Must not be null.
     * @param newValue the new value to assign to the specified configuration node. Must not be null.
     */
    @Override
    public void modify(@NotNull String node, @NotNull String newValue) {

        @Language("RegExp")
        final String[] pathParts = node.split("\\.");

        Map<String, Object> currentNode = configData;

        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            Object child = currentNode.get(part);

            if (!(child instanceof Map)) {
                child = new HashMap<String, Object>();
                currentNode.put(part, child);
            }

            currentNode = (Map<String, Object>) child;
        }

        currentNode.put(pathParts[pathParts.length - 1],newValue);
    }

    public File getConfigurationFile() {
        return configurationFile;
    }
}
