package com.habbashx.tcpserver.socket.client;

import com.habbashx.tcpserver.connection.console.UserConsoleInputHandler;
import com.habbashx.tcpserver.socket.client.foundation.ClientFoundation;
import com.habbashx.tcpserver.socket.server.settings.ServerSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * The User class represents a client application that communicates with a server using SSL sockets.
 * It implements both the {@link Runnable} interface to support threaded operation
 * and resource cleanup, respectively.
 * <p>
 * The class is designed to facilitate secure communication by establishing a connection to the server
 * on a specified port, reading incoming messages, and sending outgoing messages from the user's console.
 * It initializes SSL truststore settings using configuration properties from the {@link ServerSettings} class.
 * <p>
 * Features:
 * - Establishes an SSL socket connection to the server on the provided port.
 * - Handles user input via a {@link UserConsoleInputHandler}, running in a separate thread.
 * - Continuously listens for and processes incoming messages from the server.
 * <p>
 * Threading:
 * - The class runs its operations in a separate thread by implementing {@link Runnable}.
 * - Console input is processed asynchronously using {@link UserConsoleInputHandler}.
 */
public final class User extends ClientFoundation {

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
     * Handles user console input operations within the {@code User} class.
     * <p>
     * The {@code userConsoleInputHandler} field is responsible for managing the console input provided
     * by the user and associating it with the current instance of the {@code User} class. It facilitates
     * functionality such as processing console input, forwarding messages to other components, and
     * ensuring asynchronous behavior by supporting execution in a separate thread.
     * <p>
     * This handler works in conjunction with the user's output stream to enable real-time interaction.
     * It also assists in managing resources and ensuring proper cleanup when the input handling is no
     * longer required.
     * <p>
     * The field is an instance of {@link UserConsoleInputHandler}, which implements the {@link Runnable}
     */
    private final UserConsoleInputHandler userConsoleInputHandler = new UserConsoleInputHandler(this);

    /**
     * Indicates whether the user's thread is actively running or should be shut down.
     * <p>
     * This flag is used to control the lifecycle of the `User` instance, including
     * managing when threads should continue execution or terminate. It is initialized
     * to `true` by default, signifying that the thread is running. The running state
     * can be modified using the associated methods to safely stop the user's activity.
     * <p>
     * The value of this flag is checked in the `run` method to determine if the process
     * should continue execution or gracefully terminate. It is also used in conjunction
     * with other components such as `UserConsoleInputHandler` to coordinate safe shutdown
     * operations and resource cleanup.
     */
    private boolean running = true;

    public User() {
        super();
    }

    /**
     * Executes the main operational logic of the user connection handler.
     * This method is responsible for establishing a secure SSL/TLS connection to the server and
     * facilitating bidirectional communication using input and output streams.
     * <p>
     * The method performs the following tasks:
     * - Establishes an SSL socket connection using the server's host address and port.
     * - Initializes input and output streams for communication over the secure socket.
     * - Continuously listens for incoming messages from the server and prints them to the console.
     * - Starts a separate thread to handle user console input using a `UserConsoleInputHandler` instance.
     * <p>
     * If the connection cannot be established due to a ConnectException, an error message is displayed,
     * and the method terminates gracefully. For other IOExceptions, a RuntimeException is thrown.
     * <p>
     * The method continues operation until the `running` flag is set to false.
     *
     * @throws RuntimeException if an I/O error occurs, except in the case of a ConnectException.
     */
    @Override
    public void run() {

        try {

            super.run();
            input = new BufferedReader(new InputStreamReader(getUserSocket().getInputStream()));
            output = new PrintWriter(getUserSocket().getOutputStream(), true);

            if (running) {
                do {

                    new Thread(userConsoleInputHandler).start();
                    String inMessage;
                    while ((inMessage = input.readLine()) != null) {
                        System.out.println(inMessage);
                    }
                } while (running);
            }
        } catch (IOException e) {
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

    @Override
    public void shutdown() {
        try {
            super.shutdown();

            input.close();
            output.close();
            running = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        User user = new User();
        user.run();
    }
}
