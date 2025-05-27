package com.habbashx.tcpserver.listener;

import com.habbashx.tcpserver.configuration.Configuration;
import com.habbashx.tcpserver.configuration.JsonConfiguration;
import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.Nullable;

/**
 * ListenerConfiguration is a marker interface that provides the ability for implementing classes
 * to load a specific configuration file associated with event handlers. The class is designed for
 * use in systems where configuration management is essential for handling events dynamically.
 *
 * Key Responsibilities:
 * - Acts as a base interface for configuring event listener behavior.
 * - Provides a default method to load a configuration file tied to the implementing class's event.
 *
 * Default Method:
 * - `Configuration loadConfiguration(Server server)`: This method utilizes the `@EventHandler`
 *   annotation present on the implementing class to retrieve the specified configuration file
 *   and load it as a `JsonConfiguration`. The configuration file is expected to be defined in the
 *   annotation and linked to the server context.
 *
 * Expected Usage:
 * - Implemented by classes that require event listeners with defined configuration mechanisms.
 * - Leveraged in event-driven architectures, ensuring that each event handler has access
 *   to its corresponding configuration.
 */
public interface EventListenerConfiguration {

    /**
     * Loads a configuration file specified in the `@EventHandler` annotation of the implementing
     * class and returns a `JsonConfiguration` instance initialized with the file and server context.
     * This method ensures that each implementing class has access to its designated configuration
     * file for use within an event-driven context.
     *
     * @param server the server instance used to provide context for the configuration operations.
     * @return a `Configuration` instance containing the data loaded from the specified config file.
     * @throws AssertionError if the `@EventHandler` annotation is missing or the configFile value is null.
     */
    default Configuration loadConfiguration(Server server) {
        final @Nullable String configFile = this.getClass().getAnnotation(EventHandler.class).configFile();
        if (configFile != null) {
            return new JsonConfiguration(configFile, server);
        } else {
            throw new NullPointerException("The @EventHandler annotation is missing or the configFile value is null.");
        }
    }
}
