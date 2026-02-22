package com.habbashx.tcpserver.socket.server.foundation;

import com.habbashx.tcpserver.Shutdownable;
import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.connection.console.DefaultServerConsoleHandler;
import com.habbashx.tcpserver.connection.handler.ConnectionHandler;
import com.habbashx.tcpserver.delayevent.manager.DelayEventManager;
import com.habbashx.tcpserver.event.manager.EventManager;
import com.habbashx.tcpserver.logger.ServerLogger;
import com.habbashx.tcpserver.socket.server.settings.ServerSettings;
import com.habbashx.tcpserver.socket.server.settings.annotation.Settings;
import com.habbashx.tcpserver.socket.server.settings.annotation.manager.SettingsManager;
import com.habbashx.tcpserver.util.ServerUtils;
import com.habbashx.tcpserver.version.VersionChecker;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.*;

/**
 * ServerFoundation is an abstract base class that provides a secure, configurable, and concurrent
 * server framework using SSL/TLS for encrypted communication. It features thread pool management,
 * customizable logging, runtime command handling, and support for graceful shutdown.
 *
 * <p>This class is meant to be extended by specific server implementations that manage connections
 * and define application-specific behavior.</p>
 */
public abstract class ServerFoundation implements Shutdownable, Runnable {

    /**
     * Represents an SSL server socket for securely accepting client connections.
     * The serverSocket is used to listen for incoming secure connections using the SSL/TLS protocol.
     * It is a core component of the server's networking functionality.
     *
     * <p>This socket is initialized to handle encrypted communication, ensuring that
     * client-server interactions are protected against unauthorized access or data interception.</p>
     *
     * <p>The socket must be configured with specific keystores and protocols to enable secure communication.</p>
     */
    private SSLServerSocket serverSocket;

    /**
     * A thread pool executor service used to manage and execute server tasks concurrently.
     * Initialized with a fixed number of threads based on twice the number of available processor cores.
     *
     * <p>This configuration helps balance computational load while supporting concurrent task execution.</p>
     */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private final Set<ConnectionHandler> connectionHandlers = ConcurrentHashMap.newKeySet();

    private final ConcurrentMap<String, UserHandler> authenticatedUsers = new ConcurrentHashMap<>();

    /**
     * The eventManager is responsible for managing and dispatching events
     * within the application. It acts as a centralized handler for
     * registering, unregistering, and notifying event listeners.
     * <p>
     * This variable ensures that different parts of the application can
     * communicate asynchronously by subscribing to or publishing specific
     * events through the EventManager instance.
     * <p>
     * It is declared as a final field to ensure that the reference to the
     * EventManager remains constant throughout the lifetime of the
     * containing class.
     */
    private final EventManager eventManager = new EventManager(this);

    /**
     * Manages and coordinates delayed execution of events within the system.
     * This variable is responsible for handling operations related to event scheduling,
     * ensuring that events are executed after their specified delay intervals.
     * <p>
     * It is a final instance to maintain a single, consistent event manager associated
     * with the current object. Internally, it leverages mechanisms to manage the precise
     * timing of delayed tasks and processes.
     */
    private final DelayEventManager delayEventManager = new DelayEventManager(this);

    /**
     * A logging utility instance for the server, providing functionality for formatted,
     * colored logs at various levels such as INFO, WARNING, ERROR, and MONITOR.
     */
    private final ServerLogger serverLogger = new ServerLogger();

    /**
     * Contains server configuration values such as port and SSL settings.
     */
    private final ServerSettings serverSettings = new ServerSettings();

    /**
     * Flags to control default behaviors such as logging, version checking, and console handler.
     * These can be disabled to customize server startup behavior.
     */
    private boolean defaultLogging = true;
    /**
     * Indicates whether the server should perform version checking during startup.
     * This can be disabled to skip version validation.
     */
    private boolean versionChecker = true;
    /**
     * Indicates whether the default console handler should be started.
     * This can be disabled to prevent automatic console handling during server startup.
     */
    private boolean defaultConsoleHandler = true;

    /**
     * Constructs a new ServerFoundation instance and injects server settings from an external file.
     */
    public ServerFoundation() {
        initServerSettings();
    }

    /**
     * Starts the server execution by initializing SSL, logging, and optional components.
     */
    @Override
    public void run() {
        try {

            if (versionChecker) {
                checkVersion();
            }

            if (defaultConsoleHandler) {
                DefaultServerConsoleHandler defaultServerConsoleHandler = new DefaultServerConsoleHandler();
                threadPool.execute(defaultServerConsoleHandler);
            }

            SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
            assert serverSettings.getHost() != null;
            serverSocket.bind(new InetSocketAddress(serverSettings.getHost(), serverSettings.getPort()));
            if (defaultLogging) {
                serverLogger.info("server started at port: " + serverSettings.getPort());
                serverLogger.info("waiting for user connections.");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a new connection handler to the active connections list.
     *
     * @param connectionHandler the connection handler to be registered
     * @return the same connection handler instance
     */
    public ConnectionHandler connect(@NotNull ConnectionHandler connectionHandler) {
        connectionHandlers.add(connectionHandler);
        return connectionHandler;
    }

    /**
     * Connects a new connection handler and optionally executes it in the thread pool.
     *
     * @param connectionHandler the connection handler to be registered
     * @param autoExecute       if true, the connection handler will be executed in the thread pool
     */
    public void connect(@NotNull ConnectionHandler connectionHandler, boolean autoExecute) {

        if (autoExecute) {
            threadPool.execute(connectionHandler);
        }
        connect(connectionHandler);
    }

    public void registerAuthenticatedUser(String username, UserHandler handler) {
        authenticatedUsers.put(username, handler);
    }

    /**
     * @return the server's SSL server socket
     */
    public SSLServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * @return the server's thread pool executor
     */
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * @return a list of all active connection handlers
     */
    public Set<ConnectionHandler> getConnectionHandlers() {
        return connectionHandlers;
    }

    public ConcurrentMap<String, UserHandler> getAuthenticatedUsers() {
        return authenticatedUsers;
    }

    /**
     * @return the event manager responsible for handling events in the server
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * @return the delay event manager responsible for managing delayed events
     */
    public DelayEventManager getDelayEventManager() {
        return delayEventManager;
    }

    /**
     * @return the server's logger
     */
    public ServerLogger getServerLogger() {
        return serverLogger;
    }

    /**
     * @return the server's configuration settings
     */
    public ServerSettings getServerSettings() {
        return serverSettings;
    }

    /**
     * Injects server settings from an external configuration file into the server instance.
     *
     * <p>This method reads configuration from {@code ServerUtils.SERVER_SETTINGS_PATH} and applies
     * them to the {@link ServerSettings} object using reflection-based property injection.</p>
     */
    public void initServerSettings() {
        try {

            if (this.getClass().isAnnotationPresent(Settings.class)) {
                SettingsManager settingsManager = new SettingsManager(this);
                settingsManager.initSettings();

            } else {
                ServerUtils.injectServerSettings(serverSettings);
            }

            serverLogger.info("initialized server settings successfully.");
            serverLogger.info("server data store type:" + serverSettings.getAuthStorageType());
        } catch (Exception e) {
            serverLogger.error(e);
        }
    }

    /**
     * Gracefully shuts down the server, closing resources such as sockets and threads.
     *
     * <p>Performs the following operations:
     * <ul>
     *     <li>Closes the SSL server socket</li>
     *     <li>Shuts down the thread pool</li>
     *     <li>Waits for tasks to complete or forces shutdown after timeout</li>
     * </ul>
     *
     * @throws IOException          if closing the socket fails
     * @throws InterruptedException if the shutdown is interrupted
     */
    @Override
    public void shutdown() throws IOException, InterruptedException {
        if (serverSocket.isClosed()) {
            serverSocket.close();
        }

        if (!threadPool.isShutdown()) {
            threadPool.shutdown();
        }

        if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
            threadPool.shutdownNow();
        }
    }

    /**
     * Adds a JVM shutdown hook to call {@link #shutdown()} when the server exits.
     *
     * <p><strong>Deprecated:</strong> It's recommended to use explicit shutdown mechanisms instead of JVM shutdown hooks.</p>
     */
    @Deprecated()
    private void initializeShutdownHookOperation() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown();
            } catch (IOException | InterruptedException e) {
                serverLogger.error(e);
            }
        }));
    }

    /**
     * Disables the default console handler that is started during server startup.
     *
     * <p>This method allows customization of the server's console handling behavior
     * by preventing the automatic instantiation and execution of the default console handler.</p>
     */
    public void disableDefaultConsoleHandler() {
        defaultConsoleHandler = false;
    }

    /**
     * Disables default logging output during server startup.
     */
    public void disableDefaultLogging() {
        defaultLogging = false;
    }

    /**
     * Disables automatic version checking.
     */
    public void disableVersionChecker() {
        versionChecker = false;
    }

    /**
     * @return {@code true} if default logging is enabled
     */
    public boolean isDefaultLoggingEnabled() {
        return defaultLogging;
    }

    /**
     * @return {@code true} if version checker is enabled
     */
    public boolean isVersionCheckerEnabled() {
        return versionChecker;
    }

    /**
     * Executes the version check logic via {@link VersionChecker}.
     */
    public void checkVersion() {
        VersionChecker.checkProjectVersion(this);
    }

    /**
     * Disables all default features of the server, including logging, version checking,
     * and the default console handler.
     *
     * <p>This method is useful for customizing server behavior by turning off
     * built-in functionalities that may not be needed in certain deployments.</p>
     */
    public void disableDefaultFeatures() {
        disableDefaultLogging();
        disableVersionChecker();
        disableDefaultConsoleHandler();

    }

}

