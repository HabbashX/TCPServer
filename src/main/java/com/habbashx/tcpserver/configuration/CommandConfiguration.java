package com.habbashx.tcpserver.configuration;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for loading and resolving configuration files associated with
 * command executors based on their {@link Command} annotation metadata.
 * <p>
 * This class acts as a bridge between command definitions and their
 * corresponding configuration sources (e.g., JSON files).
 * <p>
 * It also includes an internal caching mechanism for {@link Command}
 * annotations to avoid repeated reflection lookups and improve performance
 * in high-frequency command execution environments.
 */
public final class CommandConfiguration {

    /**
     * Cache of {@link Command} annotations indexed by command executor class.
     * <p>
     * This avoids repeated reflection lookups and improves performance in
     * systems where commands are executed frequently.
     */
    private final Map<Class<?>, Command> commandCache = new ConcurrentHashMap<>();

    /**
     * Loads the configuration associated with a command executor.
     * <p>
     * The configuration file path is retrieved from the {@link Command}
     * annotation of the executor class.
     * <p>
     * If the annotation is missing or the config file is blank, an error
     * is logged and an {@link IllegalStateException} is thrown.
     *
     * @param server   the server instance used for logging and context
     * @param executor the command executor whose configuration is being loaded
     * @return a {@link Configuration} instance initialized from the command's config file
     * @throws IllegalStateException if the command has no valid configFile defined
     */
    public @NotNull Configuration loadConfiguration(
            @NotNull Server server,
            @NotNull CommandExecutor executor
    ) {

        Command command = getCommand(executor);

        if (command == null || command.configFile().isBlank()) {
            logError(server, executor);
            throw new IllegalStateException(
                    "Missing configFile for command: " + executor.getClass().getSimpleName()
            );
        }

        return new JsonConfiguration(command.configFile(), server);
    }

    /**
     * Loads the configuration associated with a command executor,
     * returning a fallback configuration if none is defined.
     * <p>
     * If the {@link Command} annotation is missing or does not define
     * a valid config file, the provided fallback configuration is returned.
     *
     * @param server   the server instance used for context and logging
     * @param executor the command executor whose configuration is being resolved
     * @param fallback the fallback configuration to use if no valid config exists
     * @return the resolved {@link Configuration} or the fallback configuration
     */
    public @NotNull Configuration loadConfiguration(
            @NotNull Server server,
            @NotNull CommandExecutor executor,
            @NotNull Configuration fallback
    ) {

        Command command = getCommand(executor);

        if (command == null || command.configFile().isBlank()) {
            logError(server, executor);
            return fallback;
        }

        return fallback;
    }


    /**
     * Retrieves the {@link Command} annotation associated with the given executor class.
     * <p>
     * Uses an internal cache to avoid repeated reflection lookups and improve performance.
     *
     * @param executor the command executor instance
     * @return the cached or newly resolved {@link Command} annotation, or null if not present
     */
    private Command getCommand(@NotNull CommandExecutor executor) {
        return commandCache.computeIfAbsent(
                executor.getClass(),
                c -> c.getAnnotation(Command.class)
        );
    }


    /**
     * Logs an error when a command configuration cannot be loaded due to
     * a missing or invalid {@code configFile} value in the {@link Command} annotation.
     *
     * @param server   the server used for logging
     * @param executor the command executor that failed configuration loading
     */
    private void logError(@NotNull Server server, @NotNull CommandExecutor executor) {
        server.getServerLogger().error(
                "Configuration loading failed for command: " +
                        executor.getClass().getSimpleName() +
                        "\nReason: missing @Command(configFile)"
        );
    }
}