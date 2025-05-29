package com.habbashx.tcpserver.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habbashx.annotation.InjectPrefix;
import com.habbashx.injector.PropertyInjector;
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

import com.habbashx.tcpserver.listener.handler.AuthenticationEventHandler;
import com.habbashx.tcpserver.listener.handler.DefaultBroadcastHandler;
import com.habbashx.tcpserver.listener.handler.DefaultChatHandler;
import com.habbashx.tcpserver.listener.handler.DefaultServerConsoleChatHandler;
import com.habbashx.tcpserver.listener.handler.DefaultUserExecuteCommandHandler;
import com.habbashx.tcpserver.listener.handler.DefaultUserJoinHandler;
import com.habbashx.tcpserver.listener.handler.DefaultUserLeaveHandler;
import com.habbashx.tcpserver.listener.handler.DefaultMutedUserHandler;

import com.habbashx.tcpserver.logger.ServerLogger;

import com.habbashx.tcpserver.security.AuthStorageType;
import com.habbashx.tcpserver.security.DefaultAuthentication;
import com.habbashx.tcpserver.security.Authentication;

import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.settings.ServerSettings;
import com.habbashx.tcpserver.user.UserDetails;
import com.habbashx.tcpserver.util.ServerUtils;
import com.habbashx.tcpserver.version.VersionChecker;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Represents a secure server that handles client connections, supports SSL/TLS protocols,
 * and manages events, commands, and user connections. The server is designed to be highly
 * concurrent, using a thread pool to efficiently handle multiple users and commands. It also
 * supports customizable authentication mechanisms.
 *
 * The {@code Server} class is responsible for:
 * - Managing and handling client connections via SSL sockets.
 * - Registering and executing event listeners for server and user activities.
 * - Registering and executing commands that users can invoke.
 * - Broadcasting messages to multiple connected clients.
 * - Managing server resources including logging, configurations, user data, and authentication.
 * - Allowing a clean shutdown process to properly release all resources.
 *
 * Implements {@code Runnable} for running the server inside a thread and
 * {@code Closeable} for ensuring proper resource cleanup.
 */
public final class Server implements Runnable {

    /**
     * A singleton instance of the Server class, ensuring only one instance is created
     * and providing thread-safe access across multiple threads.
     *
     * The volatile keyword ensures that changes to the instance variable
     * are visible to all threads immediately and prevents instruction reordering
     * by the compiler or runtime.
     */
    private static volatile Server instance;

    /**
     * Represents an SSL server socket for securely accepting client connections.
     * The serverSocket is used to listen for incoming secure connections using the SSL/TLS protocol.
     * It is a core component of the server's networking functionality.
     *
     * This socket is initialized to handle encrypted communication, ensuring that
     * client-server interactions are protected against unauthorized access or data interception.
     *
     * The socket must be configured with specific keystores and protocols to enable secure communication.
     */
    private SSLServerSocket serverSocket;
    /**
     * A thread pool executor service used to manage and execute server tasks concurrently.
     * The thread pool is initialized with a fixed number of threads based on twice the number
     * of available processor cores. This configuration helps balance computational load while
     * supporting concurrent task execution.
     *
     * This thread pool is utilized for handling various server operations, enhancing performance
     * and scalability by distributing workload across multiple threads.
     */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    /**
     * A thread-safe list of ConnectionHandler objects representing active connections.
     * The list is synchronized to ensure safe concurrent access and modifications
     * from multiple threads. Each entry in the list corresponds to an individual
     * user or client connection managed by the application.
     */
    private final List<ConnectionHandler> connections = Collections.synchronizedList(new ArrayList<>());

    /**
     * The eventManager is responsible for managing and dispatching events
     * within the application. It acts as a centralized handler for
     * registering, deregistering, and notifying event listeners.
     *
     * This variable ensures that different parts of the application can
     * communicate asynchronously by subscribing to or publishing specific
     * events through the EventManager instance.
     *
     * It is declared as a final field to ensure that the reference to the
     * EventManager remains constant throughout the lifetime of the
     * containing class.
     */
    private final EventManager eventManager = new EventManager(this);
    /**
     * Manages and coordinates delayed execution of events within the system.
     * This variable is responsible for handling operations related to event scheduling,
     * ensuring that events are executed after their specified delay intervals.
     *
     * It is a final instance to maintain a single, consistent event manager associated
     * with the current object. Internally, it leverages mechanisms to manage the precise
     * timing of delayed tasks and processes.
     */
    private final DelayEventManager delayEventManager = new DelayEventManager(this);
    /**
     * Manages the registration, organization, and execution of commands within the server.
     * Acts as the central component for handling command-related operations,
     * enabling modularity and extensibility for command functionalities.
     */
    private final CommandManager commandManager = new CommandManager(this);

    /**
     * A logging utility instance for the server, providing functionality for formatted,
     * colored logs at various levels such as INFO, WARNING, ERROR, and MONITOR.
     * This field is final, ensuring that the same logging instance is used throughout
     * the server lifecycle.
     *
     * The `serverLogger` facilitates tracking server operations, events, and issues
     * in a structured and visually distinct manner through the console output.
     */
    private final ServerLogger serverLogger = new ServerLogger();

    /**
     * The {@code serverSettings} field represents the configuration settings for the server.
     *
     * This field is an instance of the {@link ServerSettings} class and is annotated with
     * {@code @InjectPrefix("server.setting")}, enabling automatic injection of configuration
     * properties prefixed with "server.setting".
     *
     * It encapsulates essential configurations such as network settings, security parameters,
     * user interaction limits, and database connectivity details. These settings are used
     * throughout the server to maintain configuration consistency and facilitate runtime adjustments.
     *
     * Being immutable and final, {@code serverSettings} ensures thread-safe access and reliability
     * within the multi-threaded server environment.
     */
    @InjectPrefix("server.setting")
    private final ServerSettings serverSettings = new ServerSettings();

    /**
     * Manages data-related operations within the server. Responsible for user retrieval
     * and storage operations based on the configured authentication storage type, such as
     * CSV, JSON, or database.
     *
     * This variable is an instance of the {@code ServerDataManager} class and is used to:
     * - Retrieve user data by username, user ID, or email.
     * - Handle CRUD operations for user data.
     * - Interact with the server's data storage mechanisms.
     *
     * It is a final field that is initialized with a reference to the enclosing {@code Server}
     * instance.
     */
    private final ServerDataManager serverDataManager = new ServerDataManager(this);

    /**
     *
     * The {@code muteCommandManager} is an instance of {@link MuteCommandManager} used to
     * handle commands and operations related to muting and unmutCommandingManager users} on used the to server manage.
     *
     * * This mut objecting manages-related the operations lifecycle on of the mute server commands., This including includes maintaining handling a commands persistent for
     mut *ing data un storemut ofing muted users users,,
     validating * operations maintaining to the avoid list duplication of or currently inconsist mutedencies users,
     , * and and ensuring orchestr consistencyating across notifications related to command command operations send.
     ers *
     about * the It results is of responsible their for actions:
     .
     * *
     - * St Itoring is a a persistent central record component of within muted the users {@. code * Server -} Providing class functionality to to enforce mute and manage or user un
     mute specific * communication users restrictions.
     , * ensuring - smooth Sending operation appropriate by feedback interacting messages with to the command underlying initi
     ators * based server on infrastructure the.
     success */
    private final MuteCommandManager muteCommandManager = new MuteCommandManager();
    /**
     * The {@code banCommandManager} is an instance of {@link BanCommandManager} that manages
     * operations related to banning and unbanning users on the server.
     * It provides methods to enforce user bans, check the banned status of users, and manage
     * the storage of banned user data in a file-based storage system.
     *
     * Key responsibilities of this manager include:
     * - Banning a user and adding them to the banned users list.
     * - Unbanning a user and removing them from the banned users list.
     * - Verifying if a user is currently banned.
     * - Providing access to the list of all banned users.
     * - Notifying both command senders and affected users of banning-related actions.
     *
     * This variable is declared as {@code final}, ensuring the reference to the {@link BanCommandManager}
     * instance does not change after initialization.
     */
    private final BanCommandManager banCommandManager = new BanCommandManager();

    /**
     * Represents the authentication mechanism used by the {@code Server} class to handle
     * user registration and login processes. The {@code authentication} field is initialized
     * using the {@code DefaultAuthentication} implementation, which provides default
     * behavior for managing authentication tasks.
     *
     * This field is critical for ensuring that only authorized users can interact with
     * server functionalities and for preserving the security and integrity of the system.
     */
    private Authentication authentication = new DefaultAuthentication(this);

    private final ServerMemoryMonitor serverMemoryMonitor = new ServerMemoryMonitor();

    /**
     * Indicates whether the server is currently running.
     * This flag is used to control the server's operational state.
     * When set to {@code true}, the server is running and active.
     * When set to {@code false}, the server is in a shutdown or inactive state.
     */
    private boolean running = true;

    public Server() {
        injectServerSettings();
        registerDefaultEvents();
        registerDefaultDelayEvents();
        registerDefaultCommands();
        registerKeystore();
    }

    /**
     * Configures the Java SSL keystore for the server by setting system properties.
     *
     * This method fetches the keystore path and password from the server settings
     * and applies them to the system properties `javax.net.ssl.keyStore` and
     * `javax.net.ssl.keyStorePassword`. These properties are required for enabling
     * SSL-based secure communication in the server.
     */
    public void registerKeystore() {
        System.setProperty("javax.net.ssl.keyStore",serverSettings.getKeystorePath());
        System.setProperty("javax.net.ssl.keyStorePassword",serverSettings.getKeystorePassword());
    }

    /**
     * Starts the server and listens for incoming client connections using SSL.
     *
     * This method is the primary execution entry point for the {@code Server} class. It initializes
     * an {@code SSLServerSocket} on the port specified in the server configuration, logs server startup
     * details, and continuously waits for secure client connections. It also launches a console handler
     * thread for managing server-side commands and verifies the project version.
     *
     * Functionality:
     * - Logs server startup details, including the port and activation of SSL encryption.
     * - Creates an SSL-based server socket to securely listen for client connections.
     * - Launches a console handler thread to process server-side commands and administrative tasks.
     * - Continuously accepts incoming client connections through SSL sockets and assigns each to
     *   a dedicated {@code UserHandler}, running them in the configured thread pool.
     * - Adds each new {@code UserHandler} instance to the active connections list.
     * - Triggers a project version check using the {@code VersionChecker}.
     *
     * Exception Handling:
     * - If an {@code IOException} occurs during socket setup or client connection handling, an error
     *   is logged, and the {@code shutdown()} method is invoked to gracefully stop the server.
     *
     * Thread-Safety and Concurrency:
     * - The console handler and user connections run in separate threads executed by a thread pool.
     * - The server maintains a synchronized list of active user connections.
     *
     * Preconditions:
     * - Properly configured server settings, including SSL keystore setup.
     *
     * Postconditions:
     * - Logs all interactions and errors related to client connections and command inputs.
     * - On error or server shutdown, cleans up resources and stops the server.
     */
    @Override
    public void run() {

        try  {
            serverLogger.info("Server started at port: "+serverSettings.getPort());
            serverLogger.info("Server is secured with ssl protocol");
            serverLogger.info("waiting for user connections....");

            SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(serverSettings.getPort());

            ServerConsoleHandler serverConsoleHandler = new ServerConsoleHandler(this);
            threadPool.execute(serverConsoleHandler);
            VersionChecker.checkProjectVersion(this);

            if (running) {
                do {
                    SSLSocket user = (SSLSocket) serverSocket.accept();
                    user.setReuseAddress(serverSocket.getReuseAddress());
                    UserHandler userHandler = new UserHandler(user, this);
                    threadPool.execute(userHandler);
                    connections.add(userHandler);
                } while (running);
            }
        } catch (IOException e) {
            serverLogger.error(e.getMessage());
            shutdown();
        }

    }

    /**
     * Registers the default event handlers for various server operations.
     *
     * This method initializes and registers a predefined set of event listeners
     * with the server's {@code EventManager}. These listeners are responsible for
     * handling core events related to server activities such as user chat, user
     * connection/disconnection, command execution, authentication, and server console chat.
     *
     * Event Handlers:
     * - {@link DefaultChatHandler}: Handles user chat events, including message validation,
     *   broadcasting, and cooldown management.
     * - {@link DefaultMutedUserHandler}: Intercepts user chat events and enforces muted status
     *   restrictions using the {@link MuteCommandManager}.
     * - {@link DefaultUserJoinHandler}: Manages events triggered when a user joins the server.
     * - {@link DefaultUserLeaveHandler}: Manages events triggered when a user leaves the server.
     * - {@link DefaultServerConsoleChatHandler}: Handles messages sent via the server console.
     * - {@link AuthenticationEventHandler}: Listens to authentication-related events to manage
     *   user authentication processes.
     * - {@link DefaultUserExecuteCommandHandler}: Handles user command execution events and ensures
     *   proper command processing.
     *
     * Purpose:
     * This method centralizes the registration of all essential event handlers, ensuring
     * they are properly integrated with the server's event management system. These handlers
     * play a vital role in enabling core server functionality by responding to specific events.
     *
     * Usage Notes:
     * This method is invoked internally during the server setup process to configure the required
     * event handlers. It should not be invoked manually; instead, rely on the server's initialization
     * flow to ensure proper event registration and handler setup.
     */
    private void registerDefaultEvents() {
        eventManager.registerEvent(new DefaultChatHandler(this));
        eventManager.registerEvent(new DefaultMutedUserHandler(muteCommandManager));
        eventManager.registerEvent(new DefaultUserJoinHandler(this));
        eventManager.registerEvent(new DefaultUserLeaveHandler(this));
        eventManager.registerEvent(new DefaultServerConsoleChatHandler(this));
        eventManager.registerEvent(new AuthenticationEventHandler());
        eventManager.registerEvent(new DefaultUserExecuteCommandHandler(this));
    }

    /**
     * Registers the default delay-based event handlers with the {@code DelayEventManager}.
     *
     * This method initializes and registers predefined delay-based event listeners, which manage
     * specific delayed operations within the server. It is designed to set up the basic handlers
     * required for the server's delayed event management system to function appropriately.
     *
     * Current implementation registers the {@code DefaultBroadcastHandler}, a listener that handles
     * {@link BroadcastEvent} instances. This handler is configured with a low priority and a predefined
     * delay of 6000 milliseconds for processing broadcast messages. The {@code DefaultBroadcastHandler}
     * ensures that broadcast messages are sent to connected users after the specified delay.
     *
     * Preconditions:
     * - The {@code DelayEventManager} instance must be initialized prior to invoking this method.
     *
     * Postconditions:
     * - The {@code DefaultBroadcastHandler} is registered with the {@code DelayEventManager}.
     *
     * Responsibilities:
     * - Configuring the server's delay-based event system with the default listener.
     * - Ensuring proper scheduling and execution of delayed events like broadcasting.
     *
     * Notes:
     * - Additional delay-based event handlers could be added in the future by modifying this method.
     */
    private void registerDefaultDelayEvents() {
        delayEventManager.registerEvent(new DefaultBroadcastHandler());
    }

    /**
     * Registers the default set of commands to the command manager.
     * This method initializes and adds core commands used by the application to the command manager,
     * ensuring that all default functionality is available.
     *
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
        commandManager.registerCommand(new ChangeRoleCommand(this));
        commandManager.registerCommand(new HelpCommand(this));
        commandManager.registerCommand(new PrivateMessageCommand(this));
        commandManager.registerCommand(new ListUserCommand(this));
        commandManager.registerCommand(new BanCommand(this,banCommandManager));
        commandManager.registerCommand(new UnBanCommand(this,banCommandManager));
        commandManager.registerCommand(new MuteCommand(this ,muteCommandManager));
        commandManager.registerCommand(new UnMuteCommand(this,muteCommandManager));
        commandManager.registerCommand(new UserDetailsCommand(this));
        commandManager.registerCommand(new InfoCommand());
        commandManager.registerCommand(new NicknameCommand());
        commandManager.registerCommand(new ServerMemoryUsageCommand(this));

    }

    /**
     * Broadcasts a message to all connected users in the server.
     *
     * This method retrieves the list of active user connections, acquires a
     * reentrant lock for each connection, and sends the given message to each
     * user. Locks ensure thread-safe delivery of messages to all users.
     *
     * @param message the message to broadcast to all connected users
     */
    public void broadcast(String message) {
        getConnections().stream()
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

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public SSLServerSocket getServerSocket() {
        return serverSocket;
    }

    public List<ConnectionHandler> getConnections() {
        return connections;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public ServerLogger getServerLogger() {
        return serverLogger;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public DelayEventManager getDelayEventManager() {
        return delayEventManager;
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

    public ServerSettings getServerSettings() {
        return serverSettings;
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
     *
     * This method is responsible for halting the server's operation while safely releasing allocated resources
     * such as sockets, thread pools, and active user connections. It performs the following steps:
     *
     * 1. Sets the server's running status to `false` to stop accepting new processes.
     * 2. Closes the server socket if it exists and has not already been closed.
     * 3. Shuts down the thread pool and waits for all active tasks to complete. If the tasks do not terminate
     *    within 60 seconds, it forcibly shuts down the thread pool.
     * 4. Iterates through all active connections and invokes the `shutdown` method on each associated
     *    {@code UserHandler} to terminate user sessions gracefully.
     * 5. Logs a warning message to indicate the server shutdown process.
     *
     * Exception Handling:
     * - Catches {@code IOException} and {@code InterruptedException} during resource closure, and rethrows
     *   them wrapped in a {@code RuntimeException}.
     *
     * Preconditions:
     * - This method is typically invoked when the server needs to shut down either intentionally
     *   (e.g., via a shutdown signal) or due to an unexpected exception.
     */
    public void shutdown() {
        try {

            running = false;
            if (serverSocket != null) {
                if (!serverSocket.isClosed()) {
                    serverSocket.close();
                }
            }

            if (!threadPool.isShutdown()) {
                threadPool.shutdown();
            }

            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }

            connections.stream().filter(connection -> connection instanceof UserHandler).map(connection -> (UserHandler) connection).forEach(UserHandler::shutdown);

            serverLogger.warning("server shutdown");
        } catch (IOException  | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes a shutdown hook for the server.
     *
     * This method adds a JVM shutdown hook that invokes the server's {@code shutdown()}
     * method to ensure graceful cleanup of resources during application termination.
     * It leverages the {@code Runtime.getRuntime().addShutdownHook(Thread)} API to register
     * a new thread that executes the shutdown process when the JVM shuts down.
     *
     * Purpose:
     * - To ensure that critical server operations, such as closing sockets, shutting down
     *   the thread pool, and logging the shutdown event, are executed when the application
     *   terminates unexpectedly or systematically.
     *
     * Notes:
     * - The method is marked as deprecated because the approach of relying on a shutdown hook may
     *   not align with current best practices or specific server shutdown workflows.
     * - Users are encouraged to explore alternative mechanisms for managing server shutdown processes.
     *
     * Thread-Safety:
     * This operation is thread-safe as the shutdown hook is executed by the JVM in a controlled manner.
     *
     * Dependencies:
     * - Invokes the {@link #shutdown()} method for releasing server resources.
     *
     * Deprecated:
     * - The use of shutdown hooks may introduce complexities in controlled server shutdown processes,
     *   as they are inherently tied to JVM lifecycle events rather than explicit application logic.
     */
    @Deprecated()
    private void initializeShutdownHookOperation() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * Injects server settings from an external configuration file into the server instance.
     *
     * This method initializes the server's configuration parameters by reading them from a
     * properties file located at the path specified in {@code ServerUtils.SERVER_SETTINGS_PATH}.
     * A {@code PropertyInjector} is used to map the properties from the configuration file onto
     * the current server instance.
     *
     * Functionality:
     * - Reads server settings from a properties file.
     * - Applies the configuration values to the server's fields using reflection.
     *
     * Exception Handling:
     * - If any exception occurs during the injection process, it is caught and
     *   rethrown as a {@code RuntimeException}.
     *
     * Preconditions:
     * - The properties file specified by {@code ServerUtils.SERVER_SETTINGS_PATH} must exist and
     *   be properly configured with valid key-value pairs for the server settings.
     *
     * Postconditions:
     * - The server instance is configured with settings read from the properties file.
     *
     * This method is typically invoked during the initialization of the server or related
     * components requiring the server's configuration values.
     */
    private void injectServerSettings() {
        try {
            new PropertyInjector(new File(ServerUtils.SERVER_SETTINGS_PATH)).inject(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        Server server = new Server();

        server.run();
    }



    /**
     * The ServerDataManager class is a utility class designed to manage and interact with user data
     * for a given server instance. It provides methods to fetch user information based on various
     * criteria such as username, user ID, or email. User data can be retrieved from multiple storage
     * types including CSV files, JSON files, or an SQL database.
     *
     * This class also offers functionality to access online user information, such as retrieving
     * connected users via their username or ID.
     *
     * The behavior of data retrieval methods is determined by the server's configured
     * authentication storage type.
     */
    public static final class ServerDataManager {

        private final Server server;
        /**
         * Represents the type of storage mechanism used for authentication data
         * within the server.
         *
         * This variable determines the method of storing and managing user
         * authentication information, such as usernames, passwords, and roles.
         * It is utilized by the server to ensure appropriate interaction with
         * the specified storage medium.
         *
         * The available storage types are defined in the {@link AuthStorageType}
         * enum, which includes options like CSV files, JSON files, or SQL databases.
         */
        private final AuthStorageType authStorageType;
        /**
         * Represents a reference to the {@link UserDao} instance, which provides data access operations
         * for user-related queries and manipulations on the underlying data source.
         *
         * This variable is used to perform CRUD operations such as inserting, updating, deleting,
         * and retrieving user details from the data storage system through the {@link UserDao} class.
         * It serves as a bridge between the {@link ServerDataManager} class and the data source.
         *
         * The {@link UserDao} instance encapsulates the logic for interacting with the database or other
         * data storage mechanisms, allowing high-level methods in the {@link ServerDataManager} class
         * to retrieve or manipulate user-related data seamlessly.
         *
         * Designed as a final field to ensure immutability and preserve the integrity of the data access layer within
         * the {@link ServerDataManager} lifecycle.
         */
        private final UserDao userDao;

        public ServerDataManager(@NotNull Server server) {
            server.injectServerSettings();
            this.server = server;
            final String authType = ServerUtils.getAuthStorageType(server).toUpperCase();
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
         *         or null if no such user is currently online.
         */
        public @Nullable UserHandler getOnlineUserByUsername(String username) {

            return server.getConnections()
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
         *         or null if no such user is currently online.
         */
        public @Nullable UserHandler getOnlineUserById(String id) {
            return server.getConnections()
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
         *         or null if no matching user is found
         */
        public @Nullable UserDetails getUserByUsername(String username) {
            return getUserDetails("username",username);
        }

        /**
         * Retrieves user details based on the provided user ID.
         *
         * @param id the unique identifier of the user to fetch details for
         * @return a UserDetails object if the user is found, or null if no user is associated with the given ID
         */
        public @Nullable UserDetails getUserById(String id) {
            return getUserDetails("userID",id);
        }

        /**
         * Retrieves user details by their email.
         *
         * @param email the email address of the user to look up; must not be null or empty
         * @return the UserDetails associated with the provided email, or null if no user is found
         */
        public @Nullable UserDetails getUserByEmail(String email) {
            return getUserDetails("userEmail",email);
        }

        /**
         * Retrieves user details based on the specified element and value.
         * This method determines the authentication storage type (CSV, JSON, or SQL)
         * and fetches the user details accordingly.
         *
         * @param element the field/column name to query against; must not be null
         * @param specific the value of the element to match; must not be null
         * @return a {@link UserDetails} object containing the user's information if a match is found,
         *         or null if no matching record exists
         */
        private @Nullable UserDetails getUserDetails(String element, String specific) {

            try {
                return switch (authStorageType) {
                    case CSV -> getUserDetailsFromCsvFile(element, specific);
                    case JSON -> getUserDetailsFromJsonFile(element, specific);
                    case SQL -> getUserDetailsFromDatabase(element, specific);
                };
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Retrieves the user details from a CSV file based on a specific field and its value.
         * This method parses the `data/users.csv` file and checks for a record where the value
         * of the specified element matches the provided specific value.
         *
         * @param element the name of the CSV column to query; must not be null
         * @param specific the value to match against in the specified column; must not be null
         * @return a {@link UserDetails} object containing the details of the matching user if found,
         *         or null if no matching record exists
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
         * @param element the JSON field name to query against; must not be null
         * @param specific the value that the specified element should match; must not be null
         * @return a {@link UserDetails} object containing the matched user's information if found,
         *         or null if no matching user is identified
         */
        private @Nullable UserDetails getUserDetailsFromJsonFile(String element , String specific) {
            final ObjectMapper mapper = new ObjectMapper();

            try {
                List<Map<String,Object>> users = mapper.readValue(new File("data/users.json"), new TypeReference<>() {});

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
         * @param column the database column to be used for the query (e.g., "username", "email")
         * @param specific the specific value to match in the specified column
         * @return the UserDetails object representing the user, or null if no matching user is found
         * @throws SQLException if a database access error occurs
         */
        private @Nullable UserDetails getUserDetailsFromDatabase(String column,String specific) throws SQLException {
            return userDao.getUser(column,specific);
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
         *
         * The connection is designed to interact with a database instance to
         * execute SQL queries and is immutable to ensure thread safety and consistency
         * within the UserDao class.
         *
         * This field is initialized when a UserDao object is created and should not
         * be directly exposed or modified externally.
         */
        private final Connection connection;

        @Contract(pure = true)
        public UserDao(@NotNull Server server) {
            server.injectServerSettings();
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
            return DriverManager.getConnection(url, username,password);
        }

        /**
         * Inserts a new user into the database with the specified details.
         *
         * @param username the username of the user
         * @param password the plaintext password of the user
         * @param userID the unique identifier for the user
         * @param userIP the IP address associated with the user
         * @param userRole the role assigned to the user (e.g., admin, user)
         * @param userEmail the email address of the user
         * @param phoneNumber the phone number of the user
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
                stmt.setString(5,password);
                stmt.setString(6,userEmail);
                stmt.setString(7,phoneNumber);
                stmt.setBoolean(8,isActiveAccount);
                stmt.executeUpdate();
            }
        }

        /**
         * Updates a specific user's information in the database by modifying a specified column's value.
         * The update is performed based on the column to match and the specified value.
         *
         * @param column        the column name to be used for identifying the specific user
         * @param targetColumn  the column name that needs to be updated
         * @param specific      the specific value in the column used to identify the user
         * @param newValue      the new value to be assigned to the target column
         * @throws SQLException if a database access error occurs or the SQL statement fails
         */
        public void updateUser(String column,String targetColumn,String specific,String newValue) throws SQLException {
            final String sql = "UPDATE users SET %s = ? WHERE %s = ?".formatted(targetColumn,column);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newValue);
                stmt.setString(2, specific);
                stmt.executeUpdate();
            }
        }

        /**
         * Deletes a user from the database based on the specified column and value.
         *
         * @param element the column name that serves as the criteria for deletion
         * @param specific the value corresponding to the specified column to identify the user for deletion
         * @throws SQLException if a database access error occurs or the SQL execution fails
         */
        public void deleteUser(String element , String specific) throws SQLException {
            final String sql = "DELETE FROM users WHERE %s = ?".formatted(element);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, specific);
                stmt.executeUpdate();
            }
        }

        /**
         * Retrieves user details from the database based on a specified column and value.
         *
         * @param column the database column to be used for filtering the query
         * @param specific the specific value to match in the specified column
         * @return a UserDetails object containing the user's details if a match is found, or null if no match is found
         * @throws SQLException if a database access error occurs
         */
        public @Nullable UserDetails getUser(String column,String specific) throws SQLException {
            final String sql = "SELECT * FROM users WHERE %s = ?".formatted(column);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1,specific);
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
        public @Nullable String getHashedPassword(String username) throws SQLException{
            final String sql = "SELECT * FROM users WHERE username = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1,username);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
            return null;
        }
    }

    /**
     * The {@code ServerMemoryMonitor} class is a utility class designed to monitor the memory usage
     * of the server. It provides methods to retrieve information about the current memory status
     * including total memory, free memory, and maximum memory available to the Java Virtual Machine (JVM).
     *
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