package com.habbashx.tcpserver.command.manager;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.event.UserExecuteCommandEvent;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.security.container.NonVolatilePermissionContainer;
import com.habbashx.tcpserver.socket.Server;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.habbashx.tcpserver.logger.ConsoleColor.*;

/**
 * The CommandManager class is responsible for managing the registration, execution,
 * and lifecycle of commands within a server environment. It allows commands to be
 * registered with specified executors and provides an interface for executing those
 * commands with proper checks for permissions, cooldowns, and other command-specific configurations.
 * <p>
 * This class ensures that commands follow specified rules and restrictions as determined
 * by annotations, such as permission requirements and cooldown times. It supports both
 * synchronous and asynchronous execution of commands. Additionally, all registered
 * commands can be disabled individually or in bulk.
 */
public final class CommandManager {

    private static final String NO_PERMISSION_MESSAGE = RED + "you don`t have permission to execute this command." + RESET;
    private static final String ERROR_IN_EXECUTING_MESSAGE = RED + "Error in executing" + RESET;
    private static final String UNKNOWN_COMMAND_MESSAGE = "unknown command try /help.";
    private static final String ON_COOLDOWN_MESSAGE = RED + "you`re on cooldown for %s %s" + RESET;

    /**
     * A thread-safe map that stores the registered command executors associated with their command names.
     * The map ensures synchronization when modifying or accessing its contents, thereby supporting
     * concurrent access in a multi-threaded environment.
     * <p>
     * Key: The name of the command to which the executor is bound.
     * Value: The {@link CommandExecutor} responsible for handling the execution logic of the command.
     * <p>
     * This map is utilized throughout the {@code CommandManager} class to manage command registration,
     * lookup, and execution. It acts as the central storage for all command executors.
     */
    private final Map<String, CommandExecutor> executors = Collections.synchronizedMap(new HashMap<>());

    private final Server server;

    /**
     * A thread pool-based {@link ExecutorService} used for executing tasks asynchronously in the
     * context of command management within the {@code CommandManager} class. This executor
     * employs a cached thread pool, which creates new threads as needed and reuses previously
     * constructed threads when they are available.
     * <p>
     * The {@code asyncExecutor} is particularly useful for executing long-running or resource-intensive
     * tasks outside the main execution thread, ensuring that synchronous workflows are not blocked.
     * <p>
     * Note that it is important to properly shut down the executor service to release resources when
     * the system or application is being terminated.
     */
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();


    public CommandManager(Server server) {
        this.server = server;
    }

    /**
     * Registers a command with the given name and associates it with a specified {@link CommandExecutor}.
     * The registered command can later be executed by using its name.
     *
     * @param commandName     the name of the command to register
     * @param commandExecutor the {@link CommandExecutor} instance responsible for handling the command logic
     */
    public void registerCommand(String commandName, CommandExecutor commandExecutor) {
        executors.put(commandName, commandExecutor);

    }

    /**
     * Registers a command with the system by mapping the provided {@code CommandExecutor} to the
     * command's name and aliases. The method first checks if the given {@code CommandExecutor} is
     * annotated with the {@code @Command} annotation to retrieve the necessary command metadata
     * (name, aliases). If the annotation is missing, an error is logged, and the registration process
     * is aborted for this specific command.
     *
     * @param commandExecutor the command executor to register. This object must be annotated with
     *                        {@code @Command} to provide metadata about the command,
     *                        including its name and optional aliases. If the annotation is missing,
     *                        the command will not be registered, and an error message will
     *                        be logged.
     */
    public void registerCommand(@NotNull CommandExecutor commandExecutor) {

        final Class<? extends CommandExecutor> commandExecutorClass = commandExecutor.getClass();

        if (commandExecutorClass.isAnnotationPresent(Command.class)) {
            final Command commandInformation = commandExecutorClass.getAnnotation(Command.class);
            final String commandName = commandInformation.name();
            registerCommand(commandName, commandExecutor);
            server.getServerLogger().info("Registering command: " + commandExecutor + " is Successfully!. " + LIME_GREEN + "[✔️]" + RESET);

            if (0 < commandInformation.aliases().length) {
                for (final String alias : commandInformation.aliases()) {
                    registerCommand(alias, commandExecutor);
                    server.getServerLogger().info("Registering command: " + commandExecutor + " with alias " + alias + " is Successfully!. " + LIME_GREEN + "[✔️]" + RESET);
                }
            }
        } else {
            final String command = commandExecutorClass.getName();
            server.getServerLogger().warning("""
                    %s is missing the @Command annotation.
                    Please ensure that the command class is annotated with @Command and includes the 'name' and optional 'aliases' attributes.
                    The command %s will not be executed until properly annotated.
                    """.formatted(command, command));

        }
    }

    /**
     * Executes a given command based on the provided input and permissions.
     *
     * @param senderName    The name of the sender executing the command. Cannot be null.
     * @param message       The command message input by the sender. Cannot be null and must start with a "/".
     * @param commandSender The CommandSender instance representing the entity executing the command. Cannot be null.
     */
    public void executeCommand(@NotNull String senderName, @NotNull String message, @NotNull CommandSender commandSender) {

        if (!message.startsWith("/")) return;

        @Language(value = "RegExp") final String[] parts = message.substring(1).split(" ");
        final String commandName = parts[0].toLowerCase();
        final List<String> args = List.of(parts).subList(1, parts.length);

        final CommandExecutor commandExecutor = executors.get(commandName);

        if (commandExecutor != null) {
            final Class<? extends CommandExecutor> commandExecutorClass = commandExecutor.getClass();

            if (commandExecutorClass.isAnnotationPresent(Command.class)) {

                final int permissionValue = commandExecutorClass.getAnnotation(Command.class).permission();
                final Command commandInformation = commandExecutorClass.getAnnotation(Command.class);
                long cooldownTime = getCooldownTimeUnit(commandInformation.cooldownTime(), commandInformation.cooldownTimeUnit());
                commandExecutor.getCooldownManager().setCooldownTime(cooldownTime);
                if (commandSender instanceof UserHandler userHandler) {
                    final NonVolatilePermissionContainer container = userHandler.getNonVolatilePermissionContainer();

                    if (hasPermission(permissionValue, userHandler)) {

                        if (!commandExecutor.getCooldownManager().isOnCooldown(senderName)) {
                            CommandContext commandContext = new CommandContext(senderName, args, userHandler);
                            executeCommand(commandExecutor, commandContext, commandInformation.isAsync());
                            if (commandInformation.executionLog()) {
                                server.getEventManager().triggerEvent(new UserExecuteCommandEvent(senderName, userHandler, commandExecutor));
                            }
                            commandExecutor.getCooldownManager().setCooldown(senderName);
                        } else {
                            final int cooldown = (int) commandExecutor.getCooldownManager().getRemainingTime(senderName);
                            if (commandInformation.cooldownTimeUnit() == TimeUnit.SECONDS) {
                                userHandler.sendMessage(ON_COOLDOWN_MESSAGE.formatted(cooldown, "seconds"));
                            } else if (commandInformation.cooldownTimeUnit() == TimeUnit.MILLI_SECONDS) {
                                userHandler.sendMessage(ON_COOLDOWN_MESSAGE.formatted(cooldown, "milli seconds"));
                            }
                        }
                    } else {
                        userHandler.sendMessage(NO_PERMISSION_MESSAGE);
                    }
                } else {
                    CommandContext commandContext = new CommandContext(senderName, args, commandSender);
                    executeCommand(commandExecutor, commandContext, commandInformation.isAsync());
                }
            } else {
                sendMessage(commandSender, ERROR_IN_EXECUTING_MESSAGE);
            }
        } else {
            sendMessage(commandSender, UNKNOWN_COMMAND_MESSAGE);
        }
    }

    /**
     * Executes a command using the given {@link CommandExecutor} and {@link CommandContext}.
     * The execution can be done either synchronously or asynchronously depending on the value of {@code isAsync}.
     *
     * @param commandExecutor the executor responsible for handling the command logic
     * @param commandContext  the context of the command containing details such as sender and arguments
     * @param isAsync         indicates whether the command should be executed asynchronously
     */
    private void executeCommand(CommandExecutor commandExecutor, CommandContext commandContext, boolean isAsync) {

        if (isAsync) {
            asyncExecutor.submit(() -> commandExecutor.execute(commandContext));
        } else {
            commandExecutor.execute(commandContext);
        }
    }

    private void sendMessage(CommandSender commandSender, String message) {

        if (commandSender instanceof UserHandler userHandler) {
            userHandler.sendMessage(message);
        } else {
            System.out.println(message);
        }
    }

    /**
     * Calculates the cooldown time in the specified time unit.
     * If the provided time unit is invalid, logs an error and returns the cooldown unchanged.
     *
     * @param cooldown The cooldown value to be converted.
     * @param timeUnit The time unit to which the cooldown value should be converted.
     *                 Accepted values are {@link TimeUnit#MILLI_SECONDS} and {@link TimeUnit#SECONDS}.
     * @return The cooldown value converted to the specified time unit, or the original cooldown if the time unit is invalid.
     */
    private long getCooldownTimeUnit(long cooldown, int timeUnit) {
        if (timeUnit == TimeUnit.MILLI_SECONDS) {
            return cooldown / 1000;
        } else if (timeUnit == TimeUnit.SECONDS) {
            return cooldown;
        } else {
            server.getServerLogger().warning("invalid time unit: " + timeUnit);
            return cooldown;
        }
    }

    /**
     * Disables all registered commands by clearing the internal mapping of command executors.
     * After invoking this method, no commands will be executable until they are re-registered.
     */
    public void disableAllCommands() {
        executors.clear();
    }

    /**
     * Disables a specified command by removing it and its aliases
     * from the command executors registry.
     *
     * @param command the command to be disabled
     * @return {@code true} if the command and its aliases are successfully removed,
     * {@code false} if the command does not exist in the registry
     */
    public boolean disableCommand(String command) {

        final CommandExecutor executor = executors.get(command);

        if (executor != null) {
            final Command commandInformation = executor.getClass().getAnnotation(Command.class);

            if (0 < commandInformation.aliases().length) Arrays.stream(commandInformation.aliases())
                    .forEach(executors::remove);
            executors.remove(command);
            return true;
        } else {
            return false;
        }
    }


    /**
     * Retrieves a mapping of command names to their corresponding {@link CommandExecutor} instances.
     * This map represents all currently registered commands and their executors.
     *
     * @return a {@link Map} where the keys are command names (as {@link String}) and the values are
     * {@link CommandExecutor} instances responsible for handling the command logic.
     */
    public Map<String, CommandExecutor> getExecutors() {
        return executors;
    }

    /**
     * Retrieves a list of all registered command names.
     *
     * @return a list of command names that have been registered with the system.
     */
    public @NotNull List<String> getAllCommands() {
        return new ArrayList<>(executors.keySet());
    }

    /**
     * Retrieves a list of all executor names currently registered in the system.
     *
     * @return a non-null list of all registered executor names as strings.
     */
    public @NotNull List<String> getAllExecutors() {

        return new ArrayList<>(executors.keySet());
    }

    /**
     * Checks if a user has the specified permission.
     *
     * @param permissionValue the value of the permission to check
     * @param userHandler     the user handler responsible for managing user permissions
     * @return {@code true} if the user has the specified permission or meets at least one of the required conditions;
     * {@code false} otherwise
     */
    private boolean hasPermission(int permissionValue, UserHandler userHandler) {
        return permissionValue == 0X00 ||
                userHandler.hasPermission(permissionValue) ||
                userHandler.hasPermission(0X0EFA) ||
                userHandler.getNonVolatilePermissionContainer().hasPermission(permissionValue) ||
                userHandler.hasVolatilePermission(permissionValue);
    }

}
