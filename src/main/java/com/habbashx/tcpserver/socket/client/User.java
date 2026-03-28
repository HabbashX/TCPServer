package com.habbashx.tcpserver.socket.client;

import com.habbashx.tcpserver.connection.console.UserConsoleInputHandler;
import com.habbashx.tcpserver.connection.packet.Packet;
import com.habbashx.tcpserver.connection.packet.TextPacket;
import com.habbashx.tcpserver.connection.packet.factory.PacketFactory;
import com.habbashx.tcpserver.socket.client.foundation.ClientFoundation;
import com.habbashx.tcpserver.socket.server.settings.ServerSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    private DataInputStream input;
    private DataOutputStream output;
    private final UserConsoleInputHandler userConsoleInputHandler = new UserConsoleInputHandler(this);
    private boolean running = true;

    public User() {
        super();
    }

    @Override
    public void run() {
        try {
            super.run();

            input = new DataInputStream(getUserSocket().getInputStream());
            output = new DataOutputStream(getUserSocket().getOutputStream());

            new Thread(userConsoleInputHandler).start();

            while (running) {
                Packet packet = PacketFactory.readPacket(input);
                if (packet instanceof TextPacket(String message)) {
                    System.out.println(message);
                } else {
                    System.out.println("Received unknown packet type: " + packet.getType());
                }
            }
        } catch (IOException e) {
            shutdown();
            e.printStackTrace();
        }
    }

    public DataInputStream getInput() {
        return input;
    }

    public DataOutputStream getOutput() {
        return output;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void shutdown() {
        try {
            running = false;
            super.shutdown();
            if (input != null) input.close();
            if (output != null) output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void main(String[] args) {
        User user = new User();
        user.run();
    }
}
