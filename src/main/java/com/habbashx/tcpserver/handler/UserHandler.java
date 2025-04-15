package com.habbashx.tcpserver.handler;

import com.habbashx.tcpserver.command.CommandSender;

import com.habbashx.tcpserver.event.UserChatEvent;
import com.habbashx.tcpserver.event.UserLeaveEvent;
import com.habbashx.tcpserver.security.Authentication;
import com.habbashx.tcpserver.socket.Server;
import com.habbashx.tcpserver.user.UserDetails;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Objects;

import static com.habbashx.tcpserver.logger.ConsoleColor.BG_BRIGHT_BLUE;
import static com.habbashx.tcpserver.logger.ConsoleColor.BG_ORANGE;
import static com.habbashx.tcpserver.logger.ConsoleColor.BLACK;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

public final class UserHandler extends CommandSender implements Runnable {

    private final Server server;

    private final SSLSocket userSocket;

    private UserDetails userDetails;

    private final BufferedReader input;
    private final PrintWriter output;

    private final Authentication authentication;

    private boolean running = true;

    public UserHandler(@NotNull SSLSocket user, @NotNull Server server) {
        this.userSocket = user;
        this.server = server;
        userDetails = new UserDetails();
        authentication = server.getAuthentication();
        try {
            input = new BufferedReader(new InputStreamReader(user.getInputStream()));
            output = new PrintWriter(user.getOutputStream(),true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

        try {
            sendMessage("%s%sregister%s or %s%slogin%s".formatted(BG_ORANGE,BLACK,RESET,BG_BRIGHT_BLUE,BLACK,RESET));
            String choice = input.readLine();
            switch(choice) {
                case "register" -> {
                    sendMessage("enter username");
                    String username = input.readLine();
                    sendMessage("enter password");
                    String password = input.readLine();
                    sendMessage("enter email");
                    String email = input.readLine();
                    sendMessage("enter phone number");
                    String phoneNumber = input.readLine();
                    authentication.register(username,password,email,phoneNumber,this);
                }
                case "login" -> {
                    sendMessage("enter username");
                    String username = input.readLine();
                    sendMessage("enter password");
                    String password = input.readLine();
                    authentication.login(username,password,this);
                }
                default -> {
                    sendMessage("please register or login");
                    shutdown();
                }
            }
            final int cooldownSecond = Integer.parseInt(server.getServerSettings().getUserChatCooldown());
            final UserChatEvent userChatEvent = new UserChatEvent(userDetails.getUsername(),this,cooldownSecond);

            while (running) {
                String message;
                while (((message = input.readLine())) != null) {
                    if (message.startsWith("/")) {
                        server.getCommandManager().executeCommand(userDetails.getUsername(), message, this);
                    } else {
                        userChatEvent.setMessage(message);
                        server.getEventManager().triggerEvent(userChatEvent);
                    }
                }
            }
        } catch (IOException e) {
            if (!userSocket.isClosed()) {
                shutdown();
            }

            try {
                input.close();
                output.close();
            } catch (IOException ignore){

            }
        }
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public SSLSocket getUserSocket() {
        return userSocket;
    }

    public BufferedReader getReader() {
        return input;
    }

    public PrintWriter getWriter() {
        return output;
    }

    public Server getServer() {
        return server;
    }

    public boolean hasPermission(int permission) {
        return userDetails.getUserRole().getPermissions().contains(permission);
    }

    public void shutdown() {

        try {
            running = false;
            assert server != null;
            assert userSocket != null;

            server.getConnections().remove(this);
            String username = userDetails.getUsername();

            if (username != null) {
                server.getEventManager().triggerEvent(new UserLeaveEvent(username,this));
            }

            input.close();
            output.close();
            userSocket.getOutputStream().close();
            userSocket.getInputStream().close();

            if (!userSocket.isClosed()) {
                userSocket.close();
            }

        } catch (IOException ignored){}
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UserHandler that)) return false;
        return Objects.equals(userDetails, that.userDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userDetails);
    }
}
