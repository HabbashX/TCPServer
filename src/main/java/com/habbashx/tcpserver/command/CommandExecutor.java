package com.habbashx.tcpserver.command;

import com.habbashx.tcpserver.command.configuration.JsonConfiguration;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.socket.Server;

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

    public JsonConfiguration loadConfiguration(Server server) {

        String configFile = getConfigFile();

        return new JsonConfiguration(configFile,server);
    }

    /**
     * Retrieves the configuration file path specified in the {@link Command} annotation
     * applied to the class. The configuration file path is used to load command-specific
     * settings.
     *
     * @return the configuration file path defined in the {@link Command#configFile()} attribute
     * of the annotation applied to the current class.
     */
    private String getConfigFile() {
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
