package com.habbashx.tcpserver.handler.console;

import com.habbashx.tcpserver.socket.User;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Handles user console input by continuously reading input from the console and forwarding it
 * to the associated user's output stream.
 *
 * This class is designed to facilitate real-time interaction from the console with a given user,
 * forwarding messages in a separate thread to ensure asynchronous behavior. It also provides
 * methods to safely close resources when input handling is no longer needed.
 *
 * Implements the {@link Runnable} interface to allow execution in a separate thread and the
 * {@link Closeable} interface to support safe resource cleanup.
 */
public final class UserConsoleInputHandler implements Runnable , Closeable {

    private final User user;
    private final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    public UserConsoleInputHandler(User user) {
        super();
        this.user = user;

    }

    /**
     * Reads input messages from the console and forwards them to the associated user's output stream.
     *
     * This method continuously listens for messages from the system's standard input (console) through the
     * `BufferedReader` instance assigned to `input`. When a message is received, it is immediately forwarded
     * to the `PrintWriter` instance obtained from the associated `User` object.
     *
     * The method will block on reading input from the console until an input line is available
     * or the stream is closed. If the input is null (indicating end of stream), the loop terminates. In case
     * of an I/O error during reading, the method throws a RuntimeException wrapping the `IOException`.
     *
     * This method is constructed to operate in a separate thread, enabling asynchronous handling of user input
     * without blocking the main application flow.
     *
     * @throws RuntimeException if an I/O error occurs during input reading
     */
    @Override
    public void run() {
        try {

            String message;
            while ((message = input.readLine()) != null) {
                user.getOutput().println(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the user input stream and terminates the application.
     *
     * This method ensures that the `BufferedReader` associated with
     * the user input is closed properly to release system resources.
     * It subsequently exits the application by calling `System.exit(0)`.
     * If an IOException occurs during the closing operation, it is
     * wrapped and propagated as a RuntimeException.
     *
     * This method is intended to be invoked when the application needs to
     * cleanly terminate user input handling and shut down.
     *
     * @throws RuntimeException if an I/O error occurs while closing the input stream
     */
    public void closeUserInput() {

        try {
            input.close();
            System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes resources associated with this instance, including the console input and the user connection.
     *
     * This method ensures that both the console reader (`input`) and the associated `user` resources are
     * properly closed. Closing these resources is necessary to release I/O streams and ensure no active
     * connections remain. If an error occurs while closing the resources, an IOException will be thrown.
     *
     * @throws IOException if an I/O error occurs when closing the resources
     */
    @Override
    public void close() throws IOException {
        input.close();
        user.close();
    }
}
