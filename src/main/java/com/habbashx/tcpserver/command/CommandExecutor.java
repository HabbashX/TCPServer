package com.habbashx.tcpserver.command;

import com.habbashx.tcpserver.configuration.Configuration;
import com.habbashx.tcpserver.configuration.JsonConfiguration;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an abstract base class for executing commands in the system.
 * Subclasses are expected to provide their own implementation of the abstract
 * execute method, defining the specific logic for each command.
 *
 * This class includes functionality for managing cooldowns and loading
 * configuration files related to the command. The configuration file path
 * is determined based on the {@link Command} annotation applied to the subclass.
 *
 * It also provides utilities to interact with a central cooldown manager and
 * supports the retrieval and handling of command-specific configurations.
 */
public abstract class CommandExecutor {

    /**
     * Manages cooldown periods for the command execution to prevent repeated execution
     * within a defined time interval. This instance is tightly coupled to the command
     * execution framework and provides utility methods to check, set, and remove cooldowns
     * for individual users.
     *
     * The cooldown periods are primarily determined through configuration specified
     * in the {@link Command} annotation applied to the enclosing class. The cooldown time
     * and time unit are respected and enforced by this manager.
     *
     * This instance is declared as final to ensure consistent behavior throughout the
     * command lifecycle and shared among subclass implementations of the framework.
     */
    private final CooldownManager cooldownManager = new CooldownManager();

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
    public Configuration loadConfiguration(Server server) {

        final String configFile = getConfigFile();
        assert configFile != null;
        return new JsonConfiguration(configFile,server);
    }

    /**
     * Retrieves the configuration file path specified in the {@link Command} annotation
     * applied to the current class. This method accesses the annotation's `configFile`
     * property and returns its value.
     *
     * @return the configuration file path as a {@link String}, or {@code null} if the
     *         `configFile` property is not specified or the annotation is not present.
     */
    private @Nullable String getConfigFile() {
        return this.getClass().getAnnotation(Command.class).configFile();
    }

   /**
    * Executes the command logic using the provided {@link CommandContext}.
    * This method is expected to be implemented by subclasses to define specific
    * behavior for processing the command.
    *
    * @param commandContext the context of the command execution, containing details
    *                       such as the sender, arguments, and the invoking entity.
    */
   public abstract void execute(CommandContext commandContext);

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}
