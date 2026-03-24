package com.habbashx.tcpserver.connection.console;

import com.habbashx.tcpserver.connection.packet.TextPacket;
import com.habbashx.tcpserver.connection.packet.factory.PacketFactory;
import com.habbashx.tcpserver.socket.client.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

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

    @Override
    public void run() {
        final Scanner scanner = new Scanner(System.in);
        final DataOutputStream output = user.getOutput();

        try {
            while (user.isRunning()) {
                String message = scanner.nextLine();
                if (message == null) continue;

                TextPacket packet = new TextPacket(message);
                PacketFactory.writePacket(output, packet);
            }
        } catch (IOException e) {
            System.err.println("Error sending message to server: " + e.getMessage());
            user.shutdown();
        }
    }
}
