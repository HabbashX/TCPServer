package com.habbashx.tcpserver.connection.console;

import com.habbashx.tcpserver.socket.client.User;

import java.io.IOException;

/**
 * Handles user input from the console and forwards it to the associated output stream.
 * <p>
 * This class implements the `Runnable` interface and is designed to manage interaction with the user via
 * the system's standard input. It captures messages entered by the user from the console in real-time and
 * directs them to the appropriate output stream defined in the associated `User` object.
 * <p>
 * The class is intended to run in a separate thread to allow non-blocking, asynchronous operations.
 */
public final class UserConsoleInputHandler extends ConsoleHandler implements Runnable {

    private final User user;

    public UserConsoleInputHandler(User user) {
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
        try (this) {
            String message;
            while ((message = getInput().readLine()) != null) {
                user.getOutput().println(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
