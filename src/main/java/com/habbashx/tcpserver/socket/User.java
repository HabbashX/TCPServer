package com.habbashx.tcpserver.socket;

import com.habbashx.annotation.InjectPrefix;
import com.habbashx.injector.PropertyInjector;
import com.habbashx.tcpserver.handler.console.UserConsoleInputHandler;
import com.habbashx.tcpserver.settings.ServerSettings;
import com.habbashx.tcpserver.util.UserUtil;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;
import static com.habbashx.tcpserver.util.ServerUtils.SERVER_SETTINGS_PATH;

/**
 * The User class represents a client application that communicates with a server using SSL sockets.
 * It implements both the {@link Runnable} and {@link Closeable} interfaces to support threaded operation
 * and resource cleanup, respectively.
 *
 * The class is designed to facilitate secure communication by establishing a connection to the server
 * on a specified port, reading incoming messages, and sending outgoing messages from the user's console.
 * It initializes SSL truststore settings using configuration properties from the {@link ServerSettings} class.
 *
 * Features:
 * - Establishes an SSL socket connection to the server on the provided port.
 * - Handles user input via a {@link UserConsoleInputHandler}, running in a separate thread.
 * - Continuously listens for and processes incoming messages from the server.
 * - Provides safe resource cleanup via the {@link Closeable} interface.
 *
 * Threading:
 * - The class runs its operations in a separate thread by implementing {@link Runnable}.
 * - Console input is processed asynchronously using {@link UserConsoleInputHandler}.
 *
 * Resource Management:
 * - Resources such as sockets, readers, and writers are properly closed during shutdown or when
 *   the {@link #close()} method is invoked.
 * - Ensures graceful handling of connection errors or resource cleanup failures by wrapping exceptions
 *   in {@link RuntimeException}.
 */
public final class User implements Runnable , Closeable {

    private final int port;
    /**
     * Represents an SSL socket for communicating with the server.
     *
     * This socket is used to establish a secure connection between the user and the server.
     * It ensures encrypted communication using the SSL/TLS protocol, providing confidentiality
     * and integrity of transmitted data.
     *
     * The `userSocket` is expected to be initialized when connecting to the server, and
     * lifecycle management of the socket, including closing it, is handled within the containing `User` class.
     * The truststore settings for this socket are configured via the `registerTruststore` method to
     * enable mutual SSL authentication if required.
     *
     * It serves as the underlying transport layer for exchanging data between the user and server.
     */
    private SSLSocket userSocket;
    /**
     * Represents a buffered character-input stream. This variable is used to read
     * text from an input source efficiently, buffering characters to provide
     * fast performance when reading a large amount of data.
     */
    private BufferedReader input;
    /**
     * A PrintWriter object used for output operations, such as writing data
     * to files, network streams, or other output streams. This variable
     * facilitates character-based output with automatic flushing and
     * efficient writing capabilities.
     */
    private PrintWriter output;

    /**
     * Indicates whether the user's thread is actively running or should be shut down.
     *
     * This flag is used to control the lifecycle of the `User` instance, including
     * managing when threads should continue execution or terminate. It is initialized
     * to `true` by default, signifying that the thread is running. The running state
     * can be modified using the associated methods to safely stop the user's activity.
     *
     * The value of this flag is checked in the `run` method to determine if the process
     * should continue execution or gracefully terminate. It is also used in conjunction
     * with other components such as `UserConsoleInputHandler` to coordinate safe shutdown
     * operations and resource cleanup.
     */
    private boolean running = true;

    /**
     * Represents the configuration settings required for the server operation.
     *
     * The `serverSettings` field is an instance of the {@link ServerSettings} class,
     * which encapsulates a variety of server-related configurations. These configurations
     * include network setup, security properties, user interaction configurations, and
     * database connection details.
     *
     * The properties of this field are injected using the external configuration file
     * specified by the {@code SERVER_SETTINGS_PATH} in the `injectServerSettings` method.
     * This allows the server to dynamically adapt settings based on external configurations
     * without hardcoding them.
     *
     * This field assists in initializing critical system properties such as SSL/TLS
     * truststore paths and passwords (used in the `registerTruststore` method). It is also
     * designed as immutable for safe access in a multi-threaded environment.
     *
     * Annotated with {@code @InjectPrefix("server.setting")}, it indicates that all relevant
     * configuration keys for this instance are prefixed as `server.setting` in the external
     * property file.
     */
    @InjectPrefix("server.setting")
    private final ServerSettings serverSettings = new ServerSettings();

    /**
     * Handles user console input operations within the {@code User} class.
     *
     * The {@code userConsoleInputHandler} field is responsible for managing the console input provided
     * by the user and associating it with the current instance of the {@code User} class. It facilitates
     * functionality such as processing console input, forwarding messages to other components, and
     * ensuring asynchronous behavior by supporting execution in a separate thread.
     *
     * This handler works in conjunction with the user's output stream to enable real-time interaction.
     * It also assists in managing resources and ensuring proper cleanup when the input handling is no
     * longer required.
     *
     * The field is an instance of {@link UserConsoleInputHandler}, which implements the {@link Runnable}
     * and {@link Closeable} interfaces to enable thread-based execution and safe resource closure.
     */
    private UserConsoleInputHandler userConsoleInputHandler;

    public User(int port) {
        this.port = port;
        injectServerSettings();
        registerTruststore();
    }

    /**
     * Configures the application's truststore settings for SSL/TLS communication.
     *
     * This method sets the system properties for the truststore file path and password, enabling
     * secure communication by defining the trusted certificates for SSL/TLS connections. The truststore
     * path and password are retrieved from the `ServerSettings` instance associated with the application.
     *
     * Use of this method ensures that the application properly identifies and verifies trusted certificates
     * during secure communication.
     *
     * Modify the truststore configurations in the server settings file if changes to the truststore path or
     * password are required.
     *
     * Throws runtime exceptions if invalid truststore properties or configurations are provided,
     * which could impact application security or functionality.
     */
    public void registerTruststore() {
        System.setProperty("javax.net.ssl.trustStore",serverSettings.getTruststorePath());
        System.setProperty("javax.net.ssl.trustStorePassword",serverSettings.getTruststorePassword());
    }

    /**
     * Executes the main operational logic of the user connection handler.
     * This method is responsible for establishing a secure SSL/TLS connection to the server and
     * facilitating bidirectional communication using input and output streams.
     *
     * The method performs the following tasks:
     * - Establishes an SSL socket connection using the server's host address and port.
     * - Initializes input and output streams for communication over the secure socket.
     * - Continuously listens for incoming messages from the server and prints them to the console.
     * - Starts a separate thread to handle user console input using a `UserConsoleInputHandler` instance.
     *
     * If the connection cannot be established due to a ConnectException, an error message is displayed,
     * and the method terminates gracefully. For other IOExceptions, a RuntimeException is thrown.
     *
     * The method continues operation until the `running` flag is set to false.
     *
     * @throws RuntimeException if an I/O error occurs, except in the case of a ConnectException.
     */
    @Override
    public void run() {

        try {
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            userSocket = (SSLSocket) sslSocketFactory.createSocket(UserUtil.getUserHostAddress(),port);

            input = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            output = new PrintWriter(userSocket.getOutputStream(),true);

            while (running) {
                userConsoleInputHandler = new UserConsoleInputHandler(this);
                new Thread(userConsoleInputHandler).start();

                String inMessage;
                while ((inMessage = input.readLine()) != null) {
                    System.out.println(inMessage);
                }
            }
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                System.out.println(RED+"cannot connect to server"+RESET);
                return;
            }
            throw new RuntimeException(e);
        }

    }

    /**
     * Gracefully shuts down the user's connection and associated resources.
     *
     * This method stops the user session by performing the following actions:
     * 1. Sets the `running` flag to false, indicating that the user session is no longer active.
     * 2. Closes the user's socket if it is not already closed, releasing network resources.
     * 3. Closes the input and output streams to terminate data transmission.
     * 4. Invokes the `closeUserInput` method from the `UserConsoleInputHandler` to cleanly terminate
     *    the user console input handling and release system resources.
     *
     * If any of the operations fail due to an I/O error, an unchecked `RuntimeException` is thrown
     * to propagate the underlying `IOException`.
     *
     * This method ensures that all resources associated with the user session are properly released,
     * preventing potential resource leaks and ensuring a clean shutdown process.
     *
     * @throws RuntimeException if an I/O error occurs during the shutdown process
     */
    public void shutdown() {

        try {
            running = false;
            if (!userSocket.isClosed()) {
                userSocket.close();
            }
            input.close();
            output.close();
            userConsoleInputHandler.closeUserInput();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public BufferedReader getInput() {
        return input;
    }

    public PrintWriter getOutput() {
        return output;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Closes all resources associated with this User instance, ensuring proper cleanup.
     *
     * This method performs the following actions:
     * - Sets the `running` flag to false, signaling that the instance is no longer active.
     * - Closes the `userSocket` if it is not already closed, releasing the associated network resources.
     * - Closes the `input` and `output` streams to terminate communication channels.
     * - Invokes the `userConsoleInputHandler.closeUserInput()` method to handle any remaining user input cleanup.
     *
     * This method is essential for releasing system resources, terminating active connections,
     * and ensuring a clean application shutdown. If any operation fails during resource closure,
     * an `IOException` is thrown.
     *
     * @throws IOException if an I/O error occurs while closing the resources
     */
    @Override
    public void close() throws IOException {
        running = false;

        if (!userSocket.isClosed()) {
            userSocket.close();
        }
        input.close();
        output.close();;
        userConsoleInputHandler.closeUserInput();

    }

    /**
     * Injects server settings from an external configuration file into the current User instance.
     *
     * This method utilizes the `PropertyInjector` utility to read and inject properties
     * from a configuration file located at the path specified by `SERVER_SETTINGS_PATH`.
     * The injected properties configure the internal state of the application, enabling
     * it to adhere to the desired behavior and settings defined in the server configuration.
     *
     * The method is designed to provide a streamlined way of centralizing configuration management
     * by ensuring that all properties specified in the configuration file are automatically
     * applied to the user's required fields or settings.
     *
     * Throws a runtime exception in case of errors, such as:
     * - The configuration file is missing or cannot be read.
     * - There is a failure to inject the properties.
     * - Invalid configuration parameters are provided, which could hinder application functionality.
     *
     * This method is invoked at the initialization phase of the `User` class to ensure all
     * necessary configurations are applied before further operation.
     *
     * @throws RuntimeException if an error occurs during the property injection process
     */
    public void injectServerSettings() {
        try {
            PropertyInjector propertyInjector = new PropertyInjector(new File(SERVER_SETTINGS_PATH));
            propertyInjector.inject(this);
         } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {

        try (User user = new User(8080)) {
            user.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
