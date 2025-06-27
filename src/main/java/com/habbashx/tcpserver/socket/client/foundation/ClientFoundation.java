package com.habbashx.tcpserver.socket.client.foundation;


import com.habbashx.injector.PropertyInjector;
import com.habbashx.tcpserver.Shutdownable;
import com.habbashx.tcpserver.socket.client.settings.ClientSettings;
import com.habbashx.tcpserver.socket.server.settings.ServerSettings;
import com.habbashx.tcpserver.util.ClientUtils;
import com.habbashx.tcpserver.util.ServerUtils;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * Represents the foundation for a client in a TCP server environment.
 * <p>
 * This abstract class provides the basic functionality required for a client to connect
 * to a server, including socket management, truststore registration, and settings injection.
 * It implements the `Runnable` interface to allow for concurrent execution and the `Shutdownable`
 * interface to provide a mechanism for graceful shutdown of the client connection.
 * <p>
 * The concrete implementation of this class should define specific behaviors and functionalities
 * required by the client application.
 */
public abstract class ClientFoundation implements Runnable, Shutdownable {

    /**
     * Represents an SSL socket for communicating with the server.
     * <p>
     * This socket is used to establish a secure connection between the user and the server.
     * It ensures encrypted communication using the SSL/TLS protocol, providing confidentiality
     * and integrity of transmitted data.
     * <p>
     * The `userSocket` is expected to be initialized when connecting to the server, and
     * lifecycle management of the socket, including closing it, is handled within the containing `User` class.
     * The truststore settings for this socket are configured via the `registerTruststore` method to
     * enable mutual SSL authentication if required.
     * <p>
     * It serves as the underlying transport layer for exchanging data between the user and server.
     */
    private SSLSocket userSocket;

    /**
     * Represents the server settings used for establishing a connection.
     * <p>
     * This instance holds the configuration parameters required to connect to the server,
     * such as host, port, and security settings. It is initialized by injecting properties
     * from an external configuration file, allowing for flexible and dynamic server configurations.
     */
    private final ServerSettings serverSettings = new ServerSettings();

    /**
     * Represents the client settings used for configuring the client application.
     * <p>
     * This instance holds the configuration parameters specific to the client, such as
     * host, port, and other client-specific settings. It is initialized by injecting properties
     * from an external configuration file, allowing for flexible and dynamic client configurations.
     */
    private final ClientSettings clientSettings = new ClientSettings();

    public ClientFoundation() {
        injectClientSettings();
        injectServerSettings();
        registerTruststore();
    }

    @Override
    public void run() {

        try {
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            userSocket = (SSLSocket) sslSocketFactory.createSocket();
            System.out.println(clientSettings.getHost());
            userSocket.connect(new InetSocketAddress(clientSettings.getHost(), clientSettings.getPort()));
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                System.out.println(RED + "cannot connect to server" + RESET);
                return;
            }
            throw new RuntimeException(e);
        }
    }

    public SSLSocket getUserSocket() {
        return userSocket;
    }

    /**
     * Configures the application's truststore settings for SSL/TLS communication.
     * <p>
     * This method sets the system properties for the truststore file path and password, enabling
     * secure communication by defining the trusted certificates for SSL/TLS connections. The truststore
     * path and password are retrieved from the `ServerSettings` instance associated with the application.
     * <p>
     * Use of this method ensures that the application properly identifies and verifies trusted certificates
     * during secure communication.
     * <p>
     * Modify the truststore configurations in the server settings file if changes to the truststore path or
     * password are required.
     * <p>
     * Throws runtime exceptions if invalid truststore properties or configurations are provided,
     * which could impact application security or functionality.
     */
    public void registerTruststore() {
        System.setProperty("javax.net.ssl.trustStore", serverSettings.getTruststorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", serverSettings.getTruststorePassword());
    }


    /**
     * Injects server settings from an external configuration file into the current User instance.
     * <p>
     * This method utilizes the `PropertyInjector` utility to read and inject properties
     * from a configuration file located at the path specified by `SERVER_SETTINGS_PATH`.
     * The injected properties configure the internal state of the application, enabling
     * it to adhere to the desired behavior and settings defined in the server configuration.
     * <p>
     * The method is designed to provide a streamlined way of centralizing configuration management
     * by ensuring that all properties specified in the configuration file are automatically
     * applied to the user's required fields or settings.
     * <p>
     * Throws a runtime exception in case of errors, such as:
     * - The configuration file is missing or cannot be read.
     * - There is a failure to inject the properties.
     * - Invalid configuration parameters are provided, which could hinder application functionality.
     * <p>
     * This method is invoked at the initialization phase of the `User` class to ensure all
     * necessary configurations are applied before further operation.
     *
     * @throws RuntimeException if an error occurs during the property injection process
     */
    public void injectServerSettings() {
        PropertyInjector propertyInjector = new PropertyInjector(new File(ServerUtils.SERVER_SETTINGS_PATH));
        propertyInjector.inject(serverSettings);
    }

    /**
     * Injects client settings from an external configuration file into the current User instance.
     * <p>
     * This method utilizes the `PropertyInjector` utility to read and inject properties
     * from a configuration file located at the path specified by `CLIENT_SETTINGS_PATH`.
     * The injected properties configure the internal state of the application, enabling
     * it to adhere to the desired behavior and settings defined in the client configuration.
     * <p>
     * The method is designed to provide a streamlined way of centralizing configuration management
     * by ensuring that all properties specified in the configuration file are automatically
     * applied to the user's required fields or settings.
     * <p>
     * Throws a runtime exception in case of errors, such as:
     * - The configuration file is missing or cannot be read.
     * - There is a failure to inject the properties.
     * - Invalid configuration parameters are provided, which could hinder application functionality.
     * <p>
     * This method is invoked at the initialization phase of the `User` class to ensure all
     * necessary configurations are applied before further operation.
     *
     * @throws RuntimeException if an error occurs during the property injection process
     */
    public void injectClientSettings() {
        ClientUtils.generateClientSettingsFile();
        PropertyInjector propertyInjector = new PropertyInjector(new File(ClientUtils.CLIENT_SETTINGS_PATH));
        propertyInjector.inject(clientSettings);
    }

    /**
     * Gracefully shuts down the user's connection and associated resources.
     * <p>
     * This method stops the user session by performing the following actions:
     * 1. Sets the `running` flag to false, indicating that the user session is no longer active.
     * 2. Closes the user's socket if it is not already closed, releasing network resources.
     * 3. Closes the input and output streams to terminate data transmission.
     * 4. Invokes the `closeUserInput` method from the `UserConsoleInputHandler` to cleanly terminate
     * the user console input handling and release system resources.
     * <p>
     * If any of the operations fail due to an I/O error, an unchecked `RuntimeException` is thrown
     * to propagate the underlying `IOException`.
     * <p>
     * This method ensures that all resources associated with the user session are properly released,
     * preventing potential resource leaks and ensuring a clean shutdown process.
     *
     * @throws RuntimeException if an I/O error occurs during the shutdown process
     */
    @Override
    public void shutdown() {

        try {
            if (!userSocket.isClosed()) {
                userSocket.close();
            }

            if (!userSocket.isInputShutdown()) {
                userSocket.shutdownInput();
            }
            if (!userSocket.isOutputShutdown()) {
                userSocket.shutdownOutput();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

