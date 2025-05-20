package com.habbashx.tcpserver.command.configuration;

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
public class JsonConfiguration extends Configuration {

    private final File configurationFile;
    private final Map<String,Object> configData;


    public JsonConfiguration(@NotNull String configurationFile , Server server) {
        this.configurationFile = new File(configurationFile);
        try {
            ObjectMapper mapper = new ObjectMapper();
            configData = mapper.readValue(this.configurationFile,Map.class);
        } catch (IOException e) {
            server.getServerLogger().error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

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
