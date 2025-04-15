package com.habbashx.tcpserver.socket;

import com.habbashx.tcpserver.handler.console.UserConsoleInputHandler;
import com.habbashx.tcpserver.settings.ServerSettings;
import com.habbashx.tcpserver.util.UserUtil;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

public final class User implements Runnable , Closeable {

    private final int port;
    private SSLSocket userSocket;
    private BufferedReader input;
    private PrintWriter output;

    private boolean running = true;

    private final ServerSettings serverSettings = new ServerSettings();

    private UserConsoleInputHandler userConsoleInputHandler;

    public User(int port) {
        this.port = port;
        registerTruststore();
    }

    public void registerTruststore() {
        System.setProperty("javax.net.ssl.trustStore",serverSettings.getTruststorePath());
        System.setProperty("javax.net.ssl.trustStorePassword",serverSettings.getTruststorePassword());
    }

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
    public static void main(String[] args) {

        try (User user = new User(8080)) {
            user.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
