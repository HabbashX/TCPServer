package com.habbashx.tcpserver.command;

import com.habbashx.tcpserver.configuration.CommandConfiguration;
import com.habbashx.tcpserver.cooldown.CooldownManager;

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
     * Stores the configuration settings specific to the command being executed. This configuration
     * is loaded based on the command's metadata, such as the file path defined in the {@link Command}
     * annotation of the implementing command class.
     *
     * The configuration includes details and parameters necessary for the command's execution,
     * such as cooldowns, permissions, and other customizable behaviors. This instance ensures
     * that all commands derive their configurations consistently from a central system.
     *
     * Declared as final to ensure immutability and consistency throughout the lifecycle of
     * the command executor.
     */
    private final CommandConfiguration commandConfiguration = new CommandConfiguration();

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

    /**
     * Retrieves the {@link CommandConfiguration} instance associated with this command executor.
     * This configuration object provides access to command-specific settings, including properties
     * defined in external configuration files as specified by the {@link Command} annotation.
     *
     * @return the {@link CommandConfiguration} instance used to manage and access the
     *         configuration settings for this command executor.
     */
    public CommandConfiguration getCommandConfiguration() {
        return commandConfiguration;
    }

}
