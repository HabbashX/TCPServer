package com.habbashx.tcpserver.handler.console;

import com.habbashx.tcpserver.event.ServerConsoleChatEvent;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.socket.Server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.habbashx.tcpserver.logger.ConsoleColor.GRAY;
import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

public final class ServerConsoleHandler extends CommandSender implements Runnable , Closeable {

    private final Server server;

    public ServerConsoleHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            String message;
            while ((message = reader.readLine()) != null) {

                if (message.startsWith("/")) {
                    server.getCommandManager().executeCommand("Server",message,this);
                } else {
                    server.getEventManager().triggerEvent(new ServerConsoleChatEvent(message));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public void close() throws IOException {
        server.close();
    }
}
