package com.habbashx.tcpserver.configuration;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The CommandConfiguration class is responsible for loading and managing configurations related to
 * commands. It retrieves the configuration file path defined in the {@link Command} annotation
 * on the command class and initializes a configuration object for further operations. This class
 * assumes that the configuration files are in JSON format and delegates the file parsing and
 * management to the {@link JsonConfiguration} class.
 */
public class CommandConfiguration {

    /**
     * Loads the configuration file associated with the command and initializes
     * a JSON-based configuration object. The configuration file path is derived
     * from the {@link Command} annotation of the class.
     *
     * @param server the server instance used to provide context or logging during
     *               the configuration loading process.
     * @return a {@link JsonConfiguration} object representing the loaded configuration,
     *         which enables interaction with configuration data stored in a JSON file.
     */
    public Configuration loadConfiguration(Server server, @NotNull CommandExecutor commandExecutor) {

        final @Nullable String configFile = commandExecutor.getClass().getAnnotation(Command.class).configFile();
        assert configFile != null;
        return new JsonConfiguration(configFile,server);
    }

}
