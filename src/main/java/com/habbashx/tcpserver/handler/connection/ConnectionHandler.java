package com.habbashx.tcpserver.handler.connection;

import com.habbashx.annotation.InjectPrefix;
import com.habbashx.injector.PropertyInjector;
import com.habbashx.tcpserver.Shutdownable;
import com.habbashx.tcpserver.handler.connection.configuration.ConnectionSettings;
import com.habbashx.tcpserver.handler.connection.util.ConnectionUtils;
import com.habbashx.tcpserver.security.Permissible;
import com.habbashx.tcpserver.socket.server.foundation.ServerFoundation;

import javax.net.ssl.SSLSocket;
import java.io.File;
import java.net.SocketException;


/**
 * The ConnectionHandler abstract class is designed for managing secure
 * connections using SSLSocket. It serves as a base class for specialized
 * connection handlers that implement specific roles or functionalities.
 * This class also applies certain connection settings and ensures that
 * necessary configurations are injected.
 * <p>
 * ConnectionHandler implements the Permissible and Runnable interfaces,
 * allowing it to handle permission-based logic and be executed as a
 * concurrent task, respectively.
 */
public abstract class ConnectionHandler implements Permissible, Runnable, Shutdownable {

    /**
     * The ServerFoundation instance that provides the context and resources
     * for the connection handler, such as logging and server management.
     */
    private final ServerFoundation server;

    /**
     * The SSLSocket instance that represents the secure socket connection
     * established for communication. This socket is used to send and receive
     * data securely over the network.
     */
    private final SSLSocket socket;

    /**
     * Configuration object that encapsulates the settings for establishing and managing
     * connections within the {@code ConnectionHandler} class. These settings include
     * properties such as buffering options, socket timeouts, and other network-related
     * configurations that dictate the behavior of the connection.
     * <p>
     * The fields of this object are injected dynamically at runtime based on the
     * specified prefix, allowing the settings to be customized and modified
     * without altering the codebase. The {@code @InjectPrefix("connection")}
     * annotation indicates that properties prefixed with "connection" in the
     * configuration source will be injected into this instance.
     * <p>
     * The injected settings control factors such as:
     * - Enabling or disabling socket timeout.
     * - Configuring buffer size for data transmission.
     * - Reuse of socket addresses.
     * - Persistent or transient socket connections.
     */
    @InjectPrefix("connection")
    private final ConnectionSettings connectionSettings = new ConnectionSettings();

    /**
     * Constructs a new ConnectionHandler with the specified SSLSocket and ServerFoundation.
     * This constructor initializes the connection settings by invoking the
     * {@code injectConnectionSettings} method to ensure that the handler is configured
     * with the necessary properties for managing secure connections.
     *
     * @param sslSocket the SSLSocket representing the secure connection.
     * @param server    the ServerFoundation instance providing server context and resources.
     */
    public ConnectionHandler(SSLSocket sslSocket, ServerFoundation server) {
        this.server = server;
        this.socket = sslSocket;
        injectConnectionSettings();
    }

    /**
     * Retrieves the type of the handler represented by the implementing class.
     * The handler type usually indicates the specific role or functionality
     * the handler provides within the connection management process.
     *
     * @return a string representation of the handler type.
     */
    public abstract String getHandlerType();

    /**
     * Retrieves a description of the handler. The description typically provides
     * meaningful information about the purpose or functionality of the handler.
     *
     * @return a string containing the description of the handler.
     */
    public abstract String getHandlerDescription();

    public ServerFoundation getServer() {
        return server;
    }

    public SSLSocket getUserSocket() {
        return socket;
    }

    /**
     * Configures the connection settings file and applies the respective properties for the current connection handler.
     * <p>
     * This method handles the following actions:
     * - Generates or ensures the existence of a configuration file specific to the current connection handler by invoking
     * {@code ConnectionUtils.generateConnectionHandlerFile}.
     * - Retrieves the file path of the generated configuration file using {@code ConnectionUtils.getConnectionHandlerFile}.
     * - Instantiates a {@code PropertyInjector} with the configuration file to dynamically inject the
     * connection-related settings into the current connection handler instance.
     * <p>
     * The main purpose of this method is to ensure the ConnectionHandler instance is initialized with the necessary
     * configurations derived from an external properties file, enabling customized connection behavior.
     */
    private void injectConnectionSettings() {
        ConnectionUtils.generateConnectionHandlerFile(this, server.getServerLogger());

        final String connectionHandlerFileName = ConnectionUtils.getConnectionHandlerFile(this);
        final File file = new File(connectionHandlerFileName);
        final PropertyInjector propertyInjector = new PropertyInjector(file);
        propertyInjector.inject(this, socket, server);
    }

    /**
     * Configures the settings for the underlying SSL socket based on the specified connection settings.
     * This method ensures that appropriate socket options are applied, such as buffer sizes,
     * timeouts, address reuse, and keep-alive settings, if the socket is currently connected.
     * <p>
     * The configuration relies on properties provided by the {@code connectionSettings} object:
     * <ul>
     * - If buffering is enabled, both receive and send buffer sizes will be set.
     * - If socket timeout is enabled, the timeout value will be applied.
     * - Address reuse and keep-alive settings will be applied directly based on their respective flags.
     * </ul>
     * <p>
     * In case of a {@link SocketException}, the exception is logged using the server's logger.
     * The method itself does not propagate the exception, ensuring safe execution without interruption.
     * <p>
     * It is recommended to invoke this method after establishing the SSL socket connection to apply the settings properly.
     */
    public void setupSettings() {

        try {
            if (socket.isConnected()) {

                if (connectionSettings.isBufferingEnabled()) {
                    socket.setReceiveBufferSize(connectionSettings.getBufferSize());
                    socket.setSendBufferSize(connectionSettings.getBufferSize());
                }
                if (connectionSettings.isSoTimeOutEnabled()) {
                    socket.setSoTimeout(connectionSettings.getSoTimeout());
                }
                socket.setReuseAddress(connectionSettings.isReuseAddress());
                socket.setKeepAlive(connectionSettings.isKeepAlive());
            }
        } catch (SocketException e) {
            server.getServerLogger().error(e);
        }
    }
}
