package com.habbashx.tcpserver.socket.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habbashx.tcpserver.command.defaultcommand.*;
import com.habbashx.tcpserver.command.manager.BanCommandManager;
import com.habbashx.tcpserver.command.manager.CommandManager;
import com.habbashx.tcpserver.command.manager.MuteCommandManager;
import com.habbashx.tcpserver.delayevent.BroadcastEvent;
import com.habbashx.tcpserver.delayevent.manager.DelayEventManager;
import com.habbashx.tcpserver.event.manager.EventManager;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.handler.connection.ConnectionHandler;
import com.habbashx.tcpserver.handler.console.ServerConsoleHandler;
import com.habbashx.tcpserver.listener.handler.*;
import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.security.auth.Authentication;
import com.habbashx.tcpserver.security.auth.DefaultAuthentication;
import com.habbashx.tcpserver.security.auth.storage.AuthStorageType;
import com.habbashx.tcpserver.socket.server.foundation.ServerFoundation;
import com.habbashx.tcpserver.user.UserDetails;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static com.habbashx.tcpserver.logger.ConsoleColor.LIME_GREEN;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * Represents a secure server that handles client connections, supports SSL/TLS protocols,
 * and manages events, commands, and user connections. The server is designed to be highly
 * concurrent, using a thread pool to efficiently handle multiple users and commands. It also
 * supports customizable authentication mechanisms.
 * <p>
 * The {@code Server} class is responsible for:
 * - Managing and handling client connections via SSL sockets.
 * - Registering and executing event listeners for server and user activities.
 * - Registering and executing commands that users can invoke.
 * - Broadcasting messages to multiple connected clients.
 * - Managing server resources including logging, configurations, user data, and authentication.
 * - Allowing a clean shutdown process to properly release all resources.
 * <p>
 * Implements {@code Runnable} for running the server inside a thread
 */
public final class Server extends ServerFoundation implements Runnable {

    /**
     * A singleton instance of the Server class, ensuring only one instance is created
     * and providing thread-safe access across multiple threads.
     * <p>
     * The volatile keyword ensures that changes to the instance variable
     * are visible to all threads immediately and prevents instruction reordering
     * by the compiler or runtime.
     */
    private static volatile Server instance;

    /**
     * Manages the registration, organization, and execution of commands within the server.
     * Acts as the central component for handling command-related operations,
     * enabling modularity and extensibility for command functionalities.
     */
    private final CommandManager commandManager = new CommandManager(this);
    /**
     * Manages data-related operations within the server. Responsible for user retrieval
     * and storage operations based on the configured authentication storage type, such as
     * CSV, JSON, or database.
     * <p>
     * This variable is an instance of the {@code ServerDataManager} class and is used to:
     * - Retrieve user data by username, user ID, or email.
     * - Handle CRUD operations for user data.
     * - Interact with the server's data storage mechanisms.
     * <p>
     * It is a final field that is initialized with a reference to the enclosing {@code Server}
     * instance.
     */
    private final ServerDataManager serverDataManager;

    /**
     * The {@code muteCommandManager} is an instance of {@link MuteCommandManager} used to
     * handle commands and operations related to muting and unmuteCommandingManager users} on used to the server manage.
     * <p>
     * * This mut objecting manages-related the operations lifecycle on of the mute server commands., This including maintaining handling a commands persistent for
     * mut *ing data un storei offing muted users,
     * validating * operations maintaining to the avoid list duplication of or currently inconsistent multipotencies users,     * , * and ensuring orchestra consternating across notifications related to command operations send.
     * ers *
     * about * It results is of responsible their for actions:
     * .
     * *
     * - * St Storing is a persistent central record component of within muted the users {@. code * Server -} Providing class functionality to enforce mute and manage or user un     * mute specific * communication users restrictions.
     * * ensuring - smooth Sending operation appropriate by feedback interacting messages with to the command underlying init
     * actors * based server on infrastructure the.
     * success
     */
    private final MuteCommandManager muteCommandManager = new MuteCommandManager();
    /**
     * The {@code banCommandManager} is an instance of {@link BanCommandManager} that manages
     * operations related to banning and unbanning users on the server.
     * It provides methods to enforce user bans, check the banned status of users, and manage
     * the storage of banned user data in a file-based storage system.
     * <p>
     * Key responsibilities of this manager include:
     * - Banning a user and adding them to the banned users list.
     * - Unbanning a user and removing them from the banned users list.
     * - Verifying if a user is currently banned.
     * - Providing access to the list of all banned users.
     * - Notifying both command senders and affected users of banning-related actions.
     * <p>
     * This variable is declared as {@code final}, ensuring the reference to the {@link BanCommandManager}
     * instance does not change after initialization.
     */
    private final BanCommandManager banCommandManager = new BanCommandManager();

    /**
     * Represents the authentication mechanism used by the {@code Server} class to handle
     * user registration and login processes. The {@code authentication} field is initialized
     * using the {@code DefaultAuthentication} implementation, which provides default
     * behavior for managing authentication tasks.
     * <p>
     * This field is critical for ensuring that only authorized users can interact with
     * server functionalities and for preserving the security and integrity of the system.
     */
    private Authentication authentication;

    /**
     * An instance of the ServerMemoryMonitor class that monitors the memory usage
     * of the server. This object is used to track and manage server memory
     * resources, providing useful methods and functionalities for memory
     * monitoring, diagnostics, and optimization as necessary.
     * <p>
     * The instance is declared as final to ensure it is immutable and cannot
     * be reassigned, maintaining the integrity of the monitoring process.
     */
    private final ServerMemoryMonitor serverMemoryMonitor = new ServerMemoryMonitor();

    /**
     * A flag indicating whether dumb commands are enabled or not.
     * When set to {@code true}, the system may execute commands
     * that are considered non-intelligent or simplistic in nature.
     * Defaults to {@code false}, disabling such commands.
     */
    private boolean dumbCommands = false;
    /**
     * A flag indicating whether the "dumb events" mode is enabled.
     * <p>
     * When set to true, specific operations or event handling might follow
     * a simplified or less intelligent logic. This can be useful in scenarios
     * where complex event processing needs to be temporarily disabled
     * or simplified for performance, debugging, or other contextual reasons.
     */
    private boolean dumbEvents = false;

    private boolean dumbDelayEvent = false;

    private boolean commandRegisterationLoggingIsEnabled = true;

    private boolean eventRegisterationLoggingIsEnabled = true;

    private boolean delayEventRegisterationLoggingIsEnabled = true;

    /**
     * Indicates whether the server is currently running.
     * This flag is used to control the server's operational state.
     * When set to {@code true}, the server is running and active.
     * When set to {@code false}, the server is in a shutdown or inactive state.
     */
    private boolean running = true;

    public Server() {
        super();
        disableDefaultFeatures();
        disableCommandRegisterationLogging();
        disableEventRegisterationLogging();
        disableDelayEventRegisterationLogging();
        authentication = new DefaultAuthentication(this);
        serverDataManager = new ServerDataManager(this);
        registerDefaultEvents();
        registerDefaultDelayEvents();
        registerDefaultCommands();
        registerKeystore();
    }

    /**
     * Configures the Java SSL keystore for the server by setting system properties.
     * <p>
     * This method fetches the keystore path and password from the server settings
     * and applies them to the system properties `javax.net.ssl.keyStore` and
     * `javax.net.ssl.keyStorePassword`. These properties are required for enabling
     * SSL-based secure communication in the server.
     */
    public void registerKeystore() {
        System.setProperty("javax.net.ssl.keyStore", getServerSettings().getKeystorePath());
        System.setProperty("javax.net.ssl.keyStorePassword", getServerSettings().getKeystorePassword());
    }

    /**
     * Starts the server and listens for incoming client connections using SSL.
     * <p>
     * This method is the primary execution entry point for the {@code Server} class. It initializes
     * an {@code SSLServerSocket} on the port specified in the server configuration, logs server startup
     * details, and continuously waits for secure client connections. It also launches a console handler
     * thread for managing server-side commands and verifies the project version.
     * <p>
     * Functionality:
     * - Logs server startup details, including the port and activation of SSL encryption.
     * - Creates an SSL-based server socket to securely listen for client connections.
     * - Launches a console handler thread to process server-side commands and administrative tasks.
     * - Continuously accepts incoming client connections through SSL sockets and assigns each to
     * a dedicated {@code UserHandler}, running them in the configured thread pool.
     * - Adds each new {@code UserHandler} instance to the active connections list.
     * - Triggers a project version check using the {@code VersionChecker}.
     * <p>
     * Exception Handling:
     * - If an {@code IOException} occurs during socket setup or client connection handling, an error
     * is logged, and the {@code shutdown()} method is invoked to gracefully stop the server.
     * <p>
     * Thread-Safety and Concurrency:
     * - The console handler and user connections run in separate threads executed by a thread pool.
     * - The server maintains a synchronized list of active user connections.
     * <p>
     * Preconditions:
     * - Properly configured server settings, including SSL keystore setup.
     * <p>
     * Post conditions:
     * - Logs all interactions and errors related to client connections and command inputs.
     * - On error or server shutdown, cleans up resources and stops the server.
     */
    @Override
    public void run() {
        super.run();
        try {
            getServerLogger().info("Server started at port: " + getServerSettings().getPort());
            getServerLogger().info("Server is secured with ssl protocol");
            getServerLogger().info("waiting for user connections....");

            final ServerConsoleHandler serverConsoleHandler = new ServerConsoleHandler(this);
            getThreadPool().execute(serverConsoleHandler);

            if (running) {
                do {
                    SSLSocket user = (SSLSocket) getServerSocket().accept();
                    ConnectionHandler userHandler = connect(new UserHandler(user, this));
                    getThreadPool().execute(userHandler);
                } while (running);
            }
        } catch (IOException e) {
            getServerLogger().error(e);
            shutdown();
        }

    }

    /**
     * Registers the default event handlers for various server operations.
     * <p>
     * This method initializes and registers a predefined set of event listeners
     * with the server's {@code EventManager}. These listeners are responsible for
     * handling core events related to server activities such as user chat, user
     * connection/disconnection, command execution, authentication, and server console chat.
     * <p>
     * Event Handlers:
     * - {@link DefaultChatHandler}: Handles user chat events, including message validation,
     * broadcasting, and cooldown management.
     * - {@link DefaultMutedUserHandler}: Intercepts user chat events and enforces muted status
     * restrictions using the {@link MuteCommandManager}.
     * - {@link DefaultUserJoinHandler}: Manages events triggered when a user joins the server.
     * - {@link DefaultUserLeaveHandler}: Manages events triggered when a user leaves the server.
     * - {@link DefaultServerConsoleChatHandler}: Handles messages sent via the server console.
     * - {@link AuthenticationEventHandler}: Listens to authentication-related events to manage
     * user authentication processes.
     * - {@link DefaultUserExecuteCommandHandler}: Handles user command execution events and ensures
     * proper command processing.
     * <p>
     * Purpose:
     * This method centralizes the registration of all essential event handlers, ensuring
     * they are properly integrated with the server's event management system. These handlers
     * play a vital role in enabling core server functionality by responding to specific events.
     * <p>
     * Usage Notes:
     * This method is invoked internally during the server setup process to configure the required
     * event handlers. It should not be invoked manually; instead, rely on the server's initialization
     * flow to ensure proper event registration and handler setup.
     */
    private void registerDefaultEvents() {
        if (!dumbEvents) {

            getEventManager().registerEvent(new DefaultChatHandler(this));
            getEventManager().registerEvent(new DefaultMutedUserHandler(muteCommandManager));
            getEventManager().registerEvent(new DefaultUserJoinHandler(this));
            getEventManager().registerEvent(new DefaultUserLeaveHandler(this));
            getEventManager().registerEvent(new DefaultServerConsoleChatHandler(this));
            getEventManager().registerEvent(new AuthenticationEventHandler());
            getEventManager().registerEvent(new DefaultUserExecuteCommandHandler(this));

            if (eventRegisterationLoggingIsEnabled) {
                getEventManager().getRegisteredListeners().stream()
                        .forEach(listener ->
                                getServerLogger().info("Registering event handler: " + listener + " is Successfully!. " + LIME_GREEN + "[✔️]" + RESET));
            }

        } else {
            getServerLogger().info("dumbing events initialization.");
        }
    }

    /**
     * Registers the default delay-based event handlers with the {@code DelayEventManager}.
     * <p>
     * This method initializes and registers predefined delay-based event listeners, which manage
     * specific delayed operations within the server. It is designed to set up the basic handlers
     * required for the server's delayed event management system to function appropriately.
     * <p>
     * Current implementation registers the {@code DefaultBroadcastHandler}, a listener that handles
     * {@link BroadcastEvent} instances. This handler is configured with a low priority and a predefined
     * delay of 6000 milliseconds for processing broadcast messages. The {@code DefaultBroadcastHandler}
     * ensures that broadcast messages are sent to connected users after the specified delay.
     * <p>
     * Preconditions:
     * - The {@code DelayEventManager} instance must be initialized prior to invoking this method.
     * <p>
     * Post conditions:
     * - The {@code DefaultBroadcastHandler} is registered with the {@code DelayEventManager}.
     * <p>
     * Responsibilities:
     * - Configuring the server's delay-based event system with the default listener.
     * - Ensuring proper scheduling and execution of delayed events like broadcasting.
     * <p>
     * Notes:
     * - Additional delay-based event handlers could be added in the future by modifying this method.
     */
    private void registerDefaultDelayEvents() {
        if (!dumbDelayEvent) {
            getDelayEventManager().registerEvent(new DefaultBroadcastHandler());

            if (delayEventRegisterationLoggingIsEnabled) {
                getDelayEventManager().getRegisteredListeners().stream()
                        .forEach(listener -> getServerLogger().info("Registering the delay event handler: " + listener + " is Successfully!. " + LIME_GREEN + "[✔️]" + RESET));
            }
            return;
        }
        getServerLogger().info("dumbing delay events initialization.");

    }

    /**
     * Registers the default set of commands to the command manager.
     * This method initializes and adds core commands used by the application to the command manager,
     * ensuring that all default functionality is available.
     * <p>
     * Commands registered include:
     * - ChangeRoleCommand: Manages user roles.
     * - HelpCommand: Provides a list of available commands and their functionality.
     * - PrivateMessageCommand: Facilitates private messaging between users.
     * - ListUserCommand: Lists all users.
     * - BanCommand: Handles banning users with the support of the ban command manager.
     * - UnBanCommand: Handles unbanning users with the support of the ban command manager.
     * - MuteCommand: Handles muting users with the support of the mute command manager.
     * - UnMuteCommand: Handles unmuting users with the support of the mute command manager.
     * - UserDetailsCommand: Fetches and displays details about a specific user.
     * - InfoCommand: Provides general information about the application or server.
     * - NicknameCommand: Allows users to change their nickname.
     */
    private void registerDefaultCommands() {
        if (!dumbCommands) {

            commandManager.registerCommand(new ChangeRoleCommand(this));
            commandManager.registerCommand(new HelpCommand(this));
            commandManager.registerCommand(new PrivateMessageCommand(this));
            commandManager.registerCommand(new ListUserCommand(this));
            commandManager.registerCommand(new BanCommand(this, banCommandManager));
            commandManager.registerCommand(new UnBanCommand(this, banCommandManager));
            commandManager.registerCommand(new MuteCommand(this, muteCommandManager));
            commandManager.registerCommand(new UnMuteCommand(this, muteCommandManager));
            commandManager.registerCommand(new UserDetailsCommand(this));
            commandManager.registerCommand(new InfoCommand());
            commandManager.registerCommand(new NicknameCommand());
            commandManager.registerCommand(new ServerMemoryUsageCommand(this));
            commandManager.registerCommand(new AddPermissionCommand(this));
            commandManager.registerCommand(new RemovePermissionCommand(this));
            commandManager.registerCommand(new CheckPermissionCommand(this));
            commandManager.registerCommand(new RetrievesWrittenBytesCommand(this));
            if (commandRegisterationLoggingIsEnabled) {
                commandManager.getExecutors().values().stream().forEach(
                        commandExecutor -> getServerLogger().info("Registering command: " + commandExecutor + " is Successfully!. " + LIME_GREEN + "[✔️]" + RESET));
            }

            return;
        }
        getServerLogger().info("dumbing commands initialization.");
    }

    /**
     * Broadcasts a message to all connected users in the server.
     * <p>
     * This method retrieves the list of active user connections, acquires a
     * reentrant lock for each connection, and sends the given message to each
     * user. Locks ensure thread-safe delivery of messages to all users.
     *
     * @param message the message to broadcast to all connected users
     */
    public void broadcast(String message) {
        getConnectionHandlers().stream()
                .filter(connectionHandler -> connectionHandler instanceof UserHandler)
                .map(connectionHandler -> (UserHandler) connectionHandler)
                .forEach(user -> {
                    ReentrantLock reentrantLock = user.getReentrantLock();
                    reentrantLock.lock();
                    try {
                        user.sendMessage(message);
                    } finally {
                        reentrantLock.unlock();
                    }
                });
    }

    /**
     * Calculates and returns the total number of bytes written by all user connections.
     * <p>
     * This method iterates over all active user connections, ensuring thread safety by locking
     * critical sections when retrieving the written bytes count. It aggregates the bytes written
     * from the output stream of each user handler.
     *
     * @return the total number of bytes written across all user connections
     */
    public long getWrittenBytes() {

        AtomicLong writtenBytes = new AtomicLong(0L);

        getConnectionHandlers().stream()
                .filter(connectionHandler -> connectionHandler instanceof UserHandler)
                .map(connectionHandler -> (UserHandler) connectionHandler)
                .forEach(user -> {
                            ReentrantLock reentrantLock = user.getReentrantLock();
                            reentrantLock.lock();

                            try {
                                writtenBytes.addAndGet(user.getCountingOutputStream().getBytesWritten());
                            } finally {
                                reentrantLock.unlock();
                            }
                        }
                );
        return writtenBytes.get();
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }


    @Override
    public EventManager getEventManager() {
        return super.getEventManager();
    }

    @Override
    public DelayEventManager getDelayEventManager() {
        return super.getDelayEventManager();
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public BanCommandManager getBanCommandManager() {
        return banCommandManager;
    }

    public MuteCommandManager getMuteCommandManager() {
        return muteCommandManager;
    }

    public ServerDataManager getServerDataManager() {
        return serverDataManager;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public ServerMemoryMonitor getServerMemoryMonitor() {
        return serverMemoryMonitor;
    }

    /**
     * Gracefully shuts down the server, ensuring all running resources are properly closed.
     * <p>
     * This method is responsible for halting the server's operation while safely releasing allocated resources
     * such as sockets, thread pools, and active user connections. It performs the following steps:
     * <p>
     * 1. Sets the server's running status to `false` to stop accepting new processes.
     * 2. Closes the server socket if it exists and has not already been closed.
     * 3. Shuts down the thread pool and waits for all active tasks to complete. If the tasks do not terminate
     * within 60 seconds, it forcibly shuts down the thread pool.
     * 4. Iterates through all active connections and invokes the `shutdown` method on each associated
     * {@code UserHandler} to terminate user sessions gracefully.
     * 5. Logs a warning message to indicate the server shutdown process.
     * <p>
     * Exception Handling:
     * - Catches {@code IOException} and {@code InterruptedException} during resource closure, and rethrows
     * them wrapped in a {@code RuntimeException}.
     * <p>
     * Preconditions:
     * - This method is typically invoked when the server needs to shut down either intentionally
     * (e.g., via a shutdown signal) or due to an unexpected exception.
     */
    @Override
    public void shutdown() {
        try {
            super.shutdown();
            running = false;
            getConnectionHandlers().stream()
                    .filter(connection -> connection instanceof UserHandler)
                    .map(connection -> (UserHandler) connection)
                    .forEach(UserHandler::shutdown);

            getServerDataManager().getUserDao().closeConnection();
        } catch (IOException | InterruptedException e) {
            getServerLogger().error(e);
        }
    }

    /**
     * Initializes a shutdown hook for the server.
     * <p>
     * This method adds a JVM shutdown hook that invokes the server's {@code shutdown()}
     * method to ensure graceful cleanup of resources during application termination.
     * It leverages the {@code Runtime.getRuntime().addShutdownHook(Thread)} API to register
     * a new thread that executes the shutdown process when the JVM shuts down.
     * <p>
     * Purpose:
     * - To ensure that critical server operations, such as closing sockets, shutting down
     * the thread pool, and logging the shutdown event, are executed when the application
     * terminates unexpectedly or systematically.
     * <p>
     * Notes:
     * - The method is marked as deprecated because the approach of relying on a shutdown hook may
     * not align with current best practices or specific server shutdown workflows.
     * - Users are encouraged to explore alternative mechanisms for managing server shutdown processes.
     * <p>
     * Thread-Safety:
     * This operation is thread-safe as the shutdown hook is executed by the JVM in a controlled manner.
     * <p>
     * Dependencies:
     * - Invokes the {@link #shutdown()} method for releasing server resources.
     * <p>
     * Deprecated:
     * - The use of shutdown hooks may introduce complexities in controlled server shutdown processes,
     * as they are inherently tied to JVM lifecycle events rather than explicit application logic.
     */
    @Deprecated()
    private void initializeShutdownHookOperation() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void dumbCommandsInitialization() {
        dumbCommands = true;
    }

    public void dumbEventsInitialization() {
        dumbEvents = true;
    }

    public void dumbDelayEventInitialization() {
        dumbDelayEvent = true;
    }

    public void disableCommandRegisterationLogging() {
        commandRegisterationLoggingIsEnabled = false;
    }

    public void disableEventRegisterationLogging() {
        eventRegisterationLoggingIsEnabled = false;
    }

    public void disableDelayEventRegisterationLogging() {
        delayEventRegisterationLoggingIsEnabled = false;
    }

    /**
     * Provides access to the singleton instance of the Server class.
     * Ensures that only one instance is created and provides thread-safe access.
     *
     * @return the singleton instance of the Server class
     */
    public static Server getInstance() {
        if (instance == null) {
            synchronized (Server.class) {
                if (instance == null) {
                    instance = new Server();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {
        ServerFoundation server = new Server();
        server.run();
    }

    /**
     * The ServerDataManager class is a utility class designed to manage and interact with user data
     * for a given server instance. It provides methods to fetch user information based on various
     * criteria such as username, user ID, or email. User data can be retrieved from multiple storage
     * types including CSV files, JSON files, or an SQL database.
     * <p>
     * This class also offers functionality to access online user information, such as retrieving
     * connected users via their username or ID.
     * <p>
     * The behavior of data retrieval methods is determined by the server's configured
     * authentication storage type.
     */
    public static final class ServerDataManager {

        private final Server server;
        /**
         * Represents the type of storage mechanism used for authentication data
         * within the server.
         * <p>
         * This variable determines the method of storing and managing user
         * authentication information, such as usernames, passwords, and roles.
         * It is utilized by the server to ensure appropriate interaction with
         * the specified storage medium.
         * <p>
         * The available storage types are defined in the {@link AuthStorageType}
         * enum, which includes options like CSV files, JSON files, or SQL databases.
         */
        private final AuthStorageType authStorageType;
        /**
         * Represents a reference to the {@link UserDao} instance, which provides data access operations
         * for user-related queries and manipulations on the underlying data source.
         * <p>
         * This variable is used to perform CRUD operations such as inserting, updating, deleting,
         * and retrieving user details from the data storage system through the {@link UserDao} class.
         * It serves as a bridge between the {@link ServerDataManager} class and the data source.
         * <p>
         * The {@link UserDao} instance encapsulates the logic for interacting with the database or other
         * data storage mechanisms, allowing high-level methods in the {@link ServerDataManager} class
         * to retrieve or manipulate user-related data seamlessly.
         * <p>
         * Designed as a final field to ensure immutability and preserve the integrity of the data access layer within
         * the {@link ServerDataManager} lifecycle.
         */
        private final UserDao userDao;

        public ServerDataManager(@NotNull Server server) {
            this.server = server;
            final String authType = server.getServerSettings().getAuthStorageType().toUpperCase();
            authStorageType = AuthStorageType.valueOf(authType);
            userDao = new UserDao(server);
        }

        /**
         * Retrieves an online user by their username.
         * This method checks the server's active connections to find a user whose username matches the provided input.
         *
         * @param username the username of the user to search for; must not be null.
         *                 If no username is provided or if it is null, the method will return null.
         * @return the {@link UserHandler} instance representing the online user with the specified username,
         * or null if no such user is currently online.
         */
        public @Nullable UserHandler getOnlineUserByUsername(String username) {

            return server.getConnectionHandlers()
                    .stream()
                    .filter(connectionHandler -> connectionHandler instanceof UserHandler)
                    .map(connectionHandler -> (UserHandler) connectionHandler)
                    .filter(userHandler -> userHandler.getUserDetails().getUsername()
                            .equals(username)).findFirst().orElse(null);
        }

        /**
         * Retrieves an online user by their unique user ID.
         * This method iterates through the server's active connections to locate a user
         * whose user ID matches the provided input.
         *
         * @param id the unique identifier of the user to search for; must not be null.
         *           If no user ID is provided, or if the provided ID does not match
         *           any online user, the method will return null.
         * @return the {@link UserHandler} instance representing the online user with the specified user ID,
         * or null if no such user is currently online.
         */
        public @Nullable UserHandler getOnlineUserById(String id) {
            return server.getConnectionHandlers()
                    .stream()
                    .filter(connectionHandler -> connectionHandler instanceof UserHandler)
                    .map(connectionHandler -> (UserHandler) connectionHandler)
                    .filter(userHandler -> userHandler.getUserDetails().getUserID().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        /**
         * Retrieves user details using the provided username.
         * This method queries the user storage system based on the configured authentication storage type
         * (e.g., CSV, JSON, or SQL) to fetch the corresponding user information.
         *
         * @param username the username of the user to look up; must not be null
         * @return a {@link UserDetails} object containing the user's information if the username matches an existing record,
         * or null if no matching user is found
         */
        public @Nullable UserDetails getUserByUsername(String username) {
            return getUserDetails("username", username);
        }

        /**
         * Retrieves user details based on the provided user ID.
         *
         * @param id the unique identifier of the user to fetch details for
         * @return a UserDetails object if the user is found, or null if no user is associated with the given ID
         */
        public @Nullable UserDetails getUserById(String id) {
            return getUserDetails("userID", id);
        }

        /**
         * Retrieves user details by their email.
         *
         * @param email the email address of the user to look up; must not be null or empty
         * @return the UserDetails associated with the provided email, or null if no user is found
         */
        public @Nullable UserDetails getUserByEmail(String email) {
            return getUserDetails("userEmail", email);
        }

        /**
         * Retrieves user details based on the specified element and value.
         * This method determines the authentication storage type (CSV, JSON, or SQL)
         * and fetches the user details accordingly.
         *
         * @param element  the field/column name to query against; must not be null
         * @param specific the value of the element to match; must not be null
         * @return a {@link UserDetails} object containing the user's information if a match is found,
         * or null if no matching record exists
         */
        private @Nullable UserDetails getUserDetails(String element, String specific) {

            try {
                return switch (authStorageType) {
                    case CSV -> getUserDetailsFromCsvFile(element, specific);
                    case JSON -> getUserDetailsFromJsonFile(element, specific);
                    case SQL -> getUserDetailsFromDatabase(element, specific);
                };
            } catch (SQLException e) {
                server.getServerLogger().error(e);
                return null;
            }
        }

        /**
         * Retrieves the user details from a CSV file based on a specific field and its value.
         * This method parses the `data/users.csv` file and checks for a record where the value
         * of the specified element matches the provided specific value.
         *
         * @param element  the name of the CSV column to query; must not be null
         * @param specific the value to match against in the specified column; must not be null
         * @return a {@link UserDetails} object containing the details of the matching user if found,
         * or null if no matching record exists
         */
        private @Nullable UserDetails getUserDetailsFromCsvFile(String element, String specific) {
            try (final Reader reader = new FileReader("data/users.csv")) {
                Iterable<CSVRecord> userIterable = CSVFormat.DEFAULT.parse(reader);

                for (CSVRecord record : userIterable) {
                    if (record.get(element).equals(specific)) {
                        return new UserDetails().builder()
                                .userIP(record.get("userIP"))
                                .userID(record.get("userID"))
                                .userRole(Role.valueOf(record.get("userRole")))
                                .username(record.get("username"))
                                .userEmail(record.get("userEmail"))
                                .phoneNumber(record.get("phoneNumber"))
                                .activeAccount(Boolean.parseBoolean(record.get("isActiveAccount")))
                                .build();
                    }
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Retrieves user details from a JSON file based on the specified element and value.
         * This method reads a JSON file, iterates through its entries, and returns a {@link UserDetails}
         * object if a match is found between the provided element and the specified value.
         *
         * @param element  the JSON field name to query against; must not be null
         * @param specific the value that the specified element should match; must not be null
         * @return a {@link UserDetails} object containing the matched user's information if found,
         * or null if no matching user is identified
         */
        private @Nullable UserDetails getUserDetailsFromJsonFile(String element, String specific) {
            final ObjectMapper mapper = new ObjectMapper();

            try {
                List<Map<String, Object>> users = mapper.readValue(new File("data/users.json"), new TypeReference<>() {
                });

                if (users != null) {
                    return users.stream()
                            .filter(Objects::nonNull)
                            .filter(user -> user.get(element).equals(specific)).findFirst()
                            .map(user -> new UserDetails().builder()
                                    .userIP((String) user.get("userIP"))
                                    .userID((String) user.get("userID"))
                                    .userRole(Role.valueOf((String) user.get("userRole")))
                                    .userEmail((String) user.get("userEmail"))
                                    .username((String) user.get(("username")))
                                    .phoneNumber((String) user.get("phoneNumber"))
                                    .activeAccount((boolean) user.get("isActiveAccount"))
                                    .build()).orElse(null);
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        /**
         * Retrieves user details from the database based on the specified column and value.
         *
         * @param column   the database column to be used for the query (e.g., "username", "email")
         * @param specific the specific value to match in the specified column
         * @return the UserDetails object representing the user, or null if no matching user is found
         * @throws SQLException if a database access error occurs
         */
        private @Nullable UserDetails getUserDetailsFromDatabase(String column, String specific) throws SQLException {
            return userDao.getUser(column, specific);
        }

        public UserDao getUserDao() {
            return userDao;
        }
    }

    /**
     * The UserDao class provides data access functionality for managing user records in a database.
     * It supports operations such as user insertion, updating, retrieval, and deletion.
     * Instances of this class are initialized with a server instance to establish database connections.
     * This class utilizes the `java.sql` package for database interactions and assumes a table named "users"
     * exists with appropriate schema.
     */
    public static final class UserDao {

        private final Server server;

        /**
         * Represents a database connection used by the UserDao to perform various
         * user-related operations such as inserting, updating, retrieving, and
         * deleting user data. This connection is established and maintained
         * internally by the class.
         * <p>
         * The connection is designed to interact with a database instance to
         * execute SQL queries and is immutable to ensure thread safety and consistency
         * within the UserDao class.
         * <p>
         * This field is initialized when a UserDao object is created and should not
         * be directly exposed or modified externally.
         */
        private final Connection connection;

        @Contract(pure = true)
        public UserDao(@NotNull Server server) {
            this.server = server;
            try {
                connection = getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private Connection getConnection() throws SQLException {
            final String url = server.getServerSettings().getDatabaseURL();
            final String username = server.getServerSettings().getDatabaseUsername();
            final String password = server.getServerSettings().getDatabasePassword();
            return DriverManager.getConnection(url, username, password);
        }

        /**
         * Inserts a new user into the database with the specified details.
         *
         * @param username        the username of the user
         * @param password        the plaintext password of the user
         * @param userID          the unique identifier for the user
         * @param userIP          the IP address associated with the user
         * @param userRole        the role assigned to the user (e.g., admin, user)
         * @param userEmail       the email address of the user
         * @param phoneNumber     the phone number of the user
         * @param isActiveAccount the status of the user account (true if active, false otherwise)
         * @throws SQLException if a database access error occurs or the SQL statement fails
         */
        public void insertUser(String username,
                               String password,
                               String userID,
                               String userIP,
                               String userRole,
                               String userEmail,
                               String phoneNumber,
                               boolean isActiveAccount) throws SQLException {

            final String sql = "INSERT INTO users (userID, userIP, userRole, username, password, userEmail, phoneNumber, isActiveAccount) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, userID);
                stmt.setString(2, userIP);
                stmt.setString(3, userRole);
                stmt.setString(4, username);
                stmt.setString(5, password);
                stmt.setString(6, userEmail);
                stmt.setString(7, phoneNumber);
                stmt.setBoolean(8, isActiveAccount);
                stmt.executeUpdate();
            }
        }

        /**
         * Updates a specific user's information in the database by modifying a specified column's value.
         * The update is performed based on the column to match and the specified value.
         *
         * @param column       the column name to be used for identifying the specific user
         * @param targetColumn the column name that needs to be updated
         * @param specific     the specific value in the column used to identify the user
         * @param newValue     the new value to be assigned to the target column
         * @throws SQLException if a database access error occurs or the SQL statement fails
         */
        public void updateUser(String column, String targetColumn, String specific, String newValue) throws SQLException {
            final String sql = "UPDATE users SET %s = ? WHERE %s = ?".formatted(targetColumn, column);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newValue);
                stmt.setString(2, specific);
                stmt.executeUpdate();
            }
        }

        /**
         * Deletes a user from the database based on the specified column and value.
         *
         * @param element  the column name that serves as the criteria for deletion
         * @param specific the value corresponding to the specified column to identify the user for deletion
         * @throws SQLException if a database access error occurs or the SQL execution fails
         */
        public void deleteUser(String element, String specific) throws SQLException {
            final String sql = "DELETE FROM users WHERE %s = ?".formatted(element);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, specific);
                stmt.executeUpdate();
            }
        }

        /**
         * Retrieves user details from the database based on a specified column and value.
         *
         * @param column   the database column to be used for filtering the query
         * @param specific the specific value to match in the specified column
         * @return a UserDetails object containing the user's details if a match is found, or null if no match is found
         * @throws SQLException if a database access error occurs
         */
        public @Nullable UserDetails getUser(String column, String specific) throws SQLException {
            final String sql = "SELECT * FROM users WHERE %s = ?".formatted(column);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, specific);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    UserDetails user = new UserDetails();
                    user.setUserID(rs.getString("userID"));
                    user.setUserIP(rs.getString("userIP"));
                    user.setUserRole(Role.valueOf(rs.getString("userRole")));
                    user.setUsername(rs.getString("username"));
                    user.setUserEmail(rs.getString("userEmail"));
                    user.setPhoneNumber(rs.getString("phoneNumber"));
                    user.setActiveAccount(rs.getBoolean("isActiveAccount"));
                    return user;
                }
            }
            return null;
        }

        /**
         * Retrieves the hashed password of a user from the database based on the provided username.
         * If the username exists in the database, the hashed password is returned; otherwise, null is returned.
         *
         * @param username the username of the user whose hashed password is to be retrieved; must not be null
         * @return the hashed password associated with the username, or null if the username does not exist in the database
         * @throws SQLException if a database access error occurs
         */
        public @Nullable String getHashedPassword(String username) throws SQLException {
            final String sql = "SELECT * FROM users WHERE username = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
            return null;
        }

        public void closeConnection() {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                server.getServerLogger().error(e);
            }
        }
    }

    /**
     * The {@code ServerMemoryMonitor} class is a utility class designed to monitor the memory usage
     * of the server. It provides methods to retrieve information about the current memory status
     * including total memory, free memory, and maximum memory available to the Java Virtual Machine (JVM).
     * <p>
     * This class accesses the {@link Runtime} instance of the JVM to obtain memory-related data.
     */
    public static final class ServerMemoryMonitor {

        /**
         * Holds a reference to the {@link Runtime} instance associated with the current Java Virtual Machine (JVM).
         * This instance is used to retrieve memory-related information, such as total memory, free memory,
         * and maximum memory, allowing for efficient monitoring of the server's resource usage.
         */
        private final Runtime runtime = Runtime.getRuntime();

        /**
         * Retrieves the total memory currently allocated to the Java Virtual Machine (JVM).
         *
         * @return The total memory in bytes allocated to the JVM.
         */
        public long getMemoryUsage() {
            return runtime.totalMemory();
        }

        /**
         * Retrieves the amount of free memory available in the Java Virtual Machine (JVM).
         * <p>
         * This method accesses the {@link Runtime} instance of the JVM to determine
         * the amount of memory that is currently available for new object allocation.
         *
         * @return The amount of free memory in bytes available to the JVM.
         */
        public long getFreeMemory() {
            return runtime.freeMemory();
        }

        /**
         * Retrieves the maximum amount of memory that the Java Virtual Machine (JVM) will attempt to use.
         * <p>
         * This method accesses the {@link Runtime} instance to fetch the maximum memory limit configured
         * for the JVM. The value returned serves as an upper bound for the memory that the JVM can allocate
         * but is not guaranteed to be fully available depending on the system's resource constraints.
         *
         * @return The maximum memory in bytes the JVM will attempt to use.
         */
        public long getMaxMemory() {
            return runtime.maxMemory();
        }

        /**
         * Formats a given number of bytes into a human-readable string using appropriate units
         * such as KB, MB, GB, etc.
         *
         * @param bytes The size in bytes to be formatted.
         * @return A formatted string representing the size in a human-readable format with two decimal
         * precision, including unit suffix (e.g., "1.23 KB").
         */
        public @NotNull String formatBytes(long bytes) {
            int unit = 1024;
            if (bytes < unit) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            char pre = "KMGTPE".charAt(exp - 1);
            return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
        }

    }
}