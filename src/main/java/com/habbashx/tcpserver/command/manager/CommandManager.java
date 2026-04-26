package com.habbashx.tcpserver.command.manager;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.event.UserExecuteCommandEvent;
import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * Central manager responsible for handling the full lifecycle of commands
 * in the server system.
 * <p>
 * This includes:
 * <ul>
 *     <li>Command registration (with name and aliases)</li>
 *     <li>Command execution (sync and async)</li>
 *     <li>Permission validation</li>
 *     <li>Cooldown handling</li>
 *     <li>Event triggering</li>
 *     <li>Command lifecycle management (enable/disable)</li>
 * </ul>
 * <p>
 * The manager supports high-concurrency environments using virtual threads
 * and thread-safe data structures.
 * <p>
 * Command metadata is resolved through the {@link Command} annotation
 * and cached for performance optimization.
 */
public final class CommandManager {

    /**
     * Message sent when a user lacks permission to execute a command.
     */
    private static final String NO_PERMISSION_MESSAGE = RED + "you don`t have permission to execute this command." + RESET;
    /**
     * Message sent when a command execution fails due to internal error.
     */
    private static final String ERROR_IN_EXECUTING_MESSAGE = RED + "Error in executing" + RESET;
    /**
     * Message sent when a user enters an unknown command.
     */
    private static final String UNKNOWN_COMMAND_MESSAGE = "unknown command try /help.";

    /**
     * Message sent when a user is still under command cooldown.
     */
    private static final String ON_COOLDOWN_MESSAGE = RED + "you`re on cooldown for %s %s" + RESET;

    /**
     * Thread-safe registry of all registered commands.
     * <p>
     * Key: command name or alias (lowercased)
     * Value: executor responsible for handling the command
     */
    private final ConcurrentHashMap<String, CommandExecutor> executors = new ConcurrentHashMap<>();

    /**
     * Cache of {@link Command} annotations to avoid repeated reflection lookups.
     * <p>
     * Key: executor class
     * Value: cached annotation metadata
     */
    private final ConcurrentHashMap<Class<?>, Command> commandCache = new ConcurrentHashMap<>();


    /**
     * Server instance used for logging, events, and global context.
     */
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
    private final ExecutorService asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Creates a new CommandManager instance.
     *
     * @param server the server instance used for logging, events, and context
     */
    public CommandManager(Server server) {
        this.server = server;
    }

    /**
     * Registers a command with the given commandName and associates it with a specified {@link CommandExecutor}.
     * The registered command can later be executed by using its commandName.
     *
     * @param commandName     the commandName of the command to register
     * @param commandExecutor the {@link CommandExecutor} instance responsible for handling the command logic
     */
    public void registerCommand(String commandName, CommandExecutor commandExecutor) {
        executors.put(commandName.toLowerCase(Locale.ROOT), commandExecutor);
    }

    /**
     * Registers a command using its {@link Command} annotation metadata.
     * <p>
     * Automatically registers:
     * <ul>
     *     <li>Main command name</li>
     *     <li>All defined aliases</li>
     * </ul>
     * <p>
     * If the annotation is missing, the command is ignored and a warning is logged.
     *
     * @param executor the command executor to register
     */
    public void registerCommand(@NotNull CommandExecutor executor) {

        Command cmd = getCommandAnnotation(executor);

        if (cmd == null) {
            server.getServerLogger().warning(
                    executor.getClass().getSimpleName() +
                            " missing @Command annotation"
            );
            return;
        }

        registerCommand(cmd.name(), executor);

        for (String alias : cmd.aliases()) {
            registerCommand(alias, executor);
        }
    }

    private Command getCommandAnnotation(CommandExecutor executor) {
        return commandCache.computeIfAbsent(
                executor.getClass(),
                c -> c.getAnnotation(Command.class)
        );
    }

    /**
     * Executes a command from a raw input message.
     * <p>
     * The method performs:
     * <ul>
     *     <li>Command parsing</li>
     *     <li>Command lookup</li>
     *     <li>Permission validation</li>
     *     <li>Cooldown validation</li>
     *     <li>Sync or async execution</li>
     * </ul>
     *
     * @param sender        identifier of the command sender
     * @param message       raw input message (must start with '/')
     * @param commandSender sender implementation (user or console)
     */
    public void executeCommand(
            @NotNull String sender,
            @NotNull String message,
            @NotNull CommandSender commandSender
    ) {

        if (message.isEmpty() || message.charAt(0) != '/') return;

        int space = message.indexOf(' ');
        String commandName = (space == -1
                ? message.substring(1)
                : message.substring(1, space)
        ).toLowerCase(Locale.ROOT);

        List<String> args = (space == -1)
                ? List.of()
                : List.of(message.substring(space + 1).split(" "));

        CommandExecutor executor = executors.get(commandName);

        if (executor == null) {
            send(commandSender, UNKNOWN_COMMAND_MESSAGE);
            return;
        }

        Command cmd = getCommandAnnotation(executor);

        if (cmd == null) {
            send(commandSender, ERROR_IN_EXECUTING_MESSAGE);
            return;
        }

        long cooldownMs = convertCooldown(cmd.cooldownTime(), cmd.cooldownTimeUnit());
        executor.getCooldownManager().setCooldownTime(cooldownMs);

        if (commandSender instanceof UserHandler user) {

            if (!hasPermission(cmd.permission(), user)) {
                user.sendTextMessage(NO_PERMISSION_MESSAGE);
                return;
            }

            if (executor.getCooldownManager().isOnCooldown(sender)) {
                handleCooldown(user, executor, cmd, sender);
                return;
            }

            executeWithLifecycle(sender, args, user, executor, cmd);

        } else {
            run(executor, new CommandContext(sender, args, commandSender), cmd.isAsync());
        }
    }

    /**
     * Executes a command and handles post-execution lifecycle tasks.
     * <p>
     * This includes:
     * <ul>
     *     <li>Command execution</li>
     *     <li>Cooldown assignment</li>
     *     <li>Event triggering (if enabled)</li>
     * </ul>
     */
    private void executeWithLifecycle(
            String sender,
            List<String> args,
            UserHandler user,
            CommandExecutor executor,
            Command cmd
    ) {
        run(executor, new CommandContext(sender, args, user), cmd.isAsync());

        executor.getCooldownManager().setCooldown(sender);

        if (cmd.executionLog()) {
            server.getEventManager().triggerEvent(
                    new UserExecuteCommandEvent(sender, user, executor)
            );
        }
    }

    /**
     * Executes a command either synchronously or asynchronously.
     *
     * @param executor command executor
     * @param ctx      command context
     * @param async    whether execution should be asynchronous
     */
    private void run(CommandExecutor executor, CommandContext ctx, boolean async) {
        if (async) {
            asyncExecutor.execute(() -> executor.execute(ctx));
        } else {
            executor.execute(ctx);
        }
    }

    /**
     * Handles cooldown notification for a user.
     *
     * @param user     command sender
     * @param executor command executor
     * @param cmd      command metadata
     * @param name     sender identifier
     */
    private void handleCooldown(
            UserHandler user,
            CommandExecutor executor,
            Command cmd,
            String name
    ) {
        int remaining = (int) executor.getCooldownManager().getRemainingTime(name);
        user.sendTextMessage(ON_COOLDOWN_MESSAGE.formatted(
                remaining,
                cmd.cooldownTime()
        ));
    }

    /**
     * Converts cooldown value into milliseconds depending on time unit.
     *
     * @param value cooldown value
     * @param unit  time unit (seconds or milliseconds)
     * @return cooldown in milliseconds
     */
    private long convertCooldown(long value, int unit) {
        return unit == TimeUnit.SECONDS ? value : value / 1000;
    }

    /**
     * Sends a message to either a user or console.
     *
     * @param sender command sender
     * @param msg    message to send
     */
    private void send(CommandSender sender, String msg) {
        if (sender instanceof UserHandler u) {
            u.sendTextMessage(msg);
        } else {
            System.out.println(msg);
        }
    }

    /**
     * Checks whether a user has permission to execute a command.
     *
     * @param perm required permission level
     * @param user user attempting execution
     * @return true if allowed, false otherwise
     */
    private boolean hasPermission(int perm, UserHandler user) {
        return perm == 0 ||
                user.isRoleHasPermission(perm) ||
                user.hasVolatilePermission(perm) ||
                user.getNonVolatilePermissionContainer().hasPermission(perm);
    }

    /**
     * Removes all registered commands from the system.
     */
    public void disableAllCommands() {
        executors.clear();
    }

    /**
     * Disables a specific command by name.
     *
     * @param name command name
     * @return true if removed successfully, false if not found
     */
    public boolean disableCommand(String name) {
        return executors.remove(name) != null;
    }

    /**
     * @return list of all registered command names
     */
    @Contract(" -> new")
    public @NotNull List<String> getAllCommands() {
        return new ArrayList<>(executors.keySet());
    }

    /**
     * @return internal annotation cache (class → @Command metadata)
     */
    public ConcurrentHashMap<Class<?>, Command> getCommandCache() {
        return commandCache;
    }

    /**
     * @return command registry (name → executor)
     */
    public ConcurrentHashMap<String, CommandExecutor> getExecutors() {
        return executors;
    }

    /**
     * @return executor service used for async command execution
     */
    public ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }
}
