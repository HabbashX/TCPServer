package com.habbashx.tcpserver.handler.console;

import com.habbashx.tcpserver.socket.User;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

public final class UserConsoleInputHandler implements Runnable , Closeable {

    private final User user;
    private final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    public UserConsoleInputHandler(User user) {
        super();
        this.user = user;

    }

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

    public void closeUserInput() {

        try {
            input.close();
            System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        input.close();
        user.close();
    }
}
