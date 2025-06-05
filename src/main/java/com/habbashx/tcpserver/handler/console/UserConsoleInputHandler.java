package com.habbashx.tcpserver.handler.console;

import com.habbashx.tcpserver.socket.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Handles user input from the console and forwards it to the associated output stream.
 * <p>
 * This class implements the `Runnable` interface and is designed to manage interaction with the user via
 * the system's standard input. It captures messages entered by the user from the console in real-time and
 * directs them to the appropriate output stream defined in the associated `User` object.
 * <p>
 * The class is intended to run in a separate thread to allow non-blocking, asynchronous operations.
 */
public final class UserConsoleInputHandler implements Runnable {

    private final User user;
    private final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    public UserConsoleInputHandler(User user) {
        super();
        this.user = user;
    }

    /**
     * Reads input messages from the console and forwards them to the associated user's output stream.
     * <p>
     * This method continuously listens for messages from the system's standard input (console) through the
     * `BufferedReader` instance assigned to `input`. When a message is received, it is immediately forwarded
     * to the `PrintWriter` instance obtained from the associated `User` object.
     * <p>
     * The method will block on reading input from the console until an input line is available
     * or the stream is closed. If the input is null (indicating end of stream), the loop terminates. In case
     * of an I/O error during reading, the method throws a RuntimeException wrapping the `IOException`.
     * <p>
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
     * <p>
     * This method ensures that the `BufferedReader` associated with
     * the user input is closed properly to release system resources.
     * It subsequently exits the application by calling `System.exit(0)`.
     * If an IOException occurs during the closing operation, it is
     * wrapped and propagated as a RuntimeException.
     * <p>
     * This method is intended to be invoked when the application needs to
     * cleanly terminate user input handling and shut down.
     *
     * @throws RuntimeException if an I/O error occurs while closing the input stream
     */
    public void closeUserInput() {

        try {
            user.shutdown();
            input.close();
            System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
