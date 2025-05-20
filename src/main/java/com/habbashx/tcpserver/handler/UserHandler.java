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
import java.util.concurrent.locks.ReentrantLock;

import static com.habbashx.tcpserver.logger.ConsoleColor.BG_BRIGHT_BLUE;
import static com.habbashx.tcpserver.logger.ConsoleColor.BG_ORANGE;
import static com.habbashx.tcpserver.logger.ConsoleColor.BLACK;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * Handles user interactions and communication with the server.
 *
 * This class manages the lifecycle of a user connection, including registration, login,
 * message handling, and```java
 /**
 * Handles user interactions and communication with the server.
 *
 * This class manages the lifecycle of user sessions, including authentication,
 * message handling, and interaction through commands. It extends the functionality
 * of {@link CommandSender} for executing commands and implements {@link Runnable}
 * for running user interaction on command execution. It operates as a thread, enabling asynchronous
 * communication between the user and the server.
 *
 * UserHandler extends CommandSender, inheriting its functionalities for
 * executing commands and managing message communication. It also implements
 * Runnable to allow concurrent execution.
 *
 * The class is responsible for maintaining user details, handling authentication,
 * and interacting with server events like messaging and user connection management.
 *
 * Thread-safety is managed internally via the use of locking in the parent class
 * and proper resource cleanup upon shutting down the connection.
 */
public final class UserHandler extends CommandSender implements Runnable {

    private final Server server;

    /**
     * Represents the secure socket connection associated with a user.
     *
     * This variable holds the {@link SSLSocket} instance representing the user's connection
     * to the server. It is used for encrypted communication between the server and the user.
     * The socket ensures confidentiality and integrity of the data being transmitted.
     *
     * This field is immutable and is initialized during the creation of a {@code UserHandler} instance.
     * It serves as the primary communication channel for the user handled by this class.
     */
    private final SSLSocket userSocket;

    /**
     * Represents the details of a user associated with this handler.
     *
     * The `userDetails` field stores information about the user, including identity,
     * role, contact information, and account status. This information is used to manage
     * interactions between the user and the system, and to enforce role-based behavior
     * and permissions.
     */
    private UserDetails userDetails;


    /**
     * A {@link BufferedReader} instance used for reading input from the user's socket connection.
     *
     * This variable facilitates processing incoming data from the client, enabling the server
     * to handle user commands or messages. The {@code input} is initialized during the creation
     * of a {@code UserHandler} instance and remains immutable thereafter. It provides a
     * connection-specific stream for reading text-based input in a blocking and thread-safe manner.
     *
     * It is vital for interpreting the user's communication with the server, allowing the server
     * to perform tasks such as authentication, command execution, or message broadcasting.
     *
     * Note that this reader must not be closed directly, as it is managed within the lifecycle
     * of the {@code UserHandler} instance.
     */
    private final BufferedReader input;
    /**
     * A {@code PrintWriter} used to send output data to the connected user.
     *
     * This field is initialized during the construction of the {@code UserHandler} instance
     * and is associated with the user's socket output stream. It facilitates sending messages
     * or responses to the user during the connection lifecycle.
     *
     * Being declared as {@code final}, it ensures that the reference to the output stream
     * remains constant, preventing reassignment after initialization. Thread-safety must be
     * handled externally if needed for concurrent access.
     */
    private final PrintWriter output;

    /**
     * Represents an authentication mechanism for the associated {@link UserHandler}.
     *
     * This field is used to manage user authentication processes such as login
     * and registration within the system. The {@code Authentication} class,
     * which this field is an instance of, provides abstract methods to handle
     * these processes securely and efficiently.
     *
     * Being declared as {@code final}, this field is immutable and ensures that
     * the authentication mechanism for a specific {@link UserHandler} instance
     * remains consistent throughout its lifecycle.
     *
     * The field is integral to enabling user-specific authentication workflows,
     * including establishing and maintaining authenticated sessions.
     */
    private final Authentication authentication;

    /**
     * A boolean variable that indicates whether the {@link UserHandler} instance
     * is actively running its operations.
     *
     * This variable is set to {@code true} by default, meaning the instance is actively
     * processing, and can be modified to {@code false} to signal the instance to stop its execution.
     * It is primarily used to control the execution flow of the {@link UserHandler#run()} method,
     * allowing for graceful shutdown or termination of the handler's processes.
     */
    private boolean running = true;

    /**
     * Constructs a UserHandler instance for managing a user connection and server communication.
     *
     * @param user the SSL socket representing the user's connection; must not be null
     * @param server the server instance associated with this user handler; must not be null
     */
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

    /**
     * Executes the main logic for handling user interaction, authentication, and
     * communication with the server. This method is invoked when the thread for
     * the associated user handler starts.
     *
     * The `run` method performs the following tasks:
     * 1. Prompts the user to either register or log in, executing the corresponding
     *    authentication process based on user input.
     *    - Registers a new user with a username, password, email, and phone number.
     *    - Logs in an existing user with their username and password.
     * 2. If no valid input is provided, terminates the session.
     *
     * Once authenticated, the method:
     * - Initializes a chat cooldown mechanism using server settings.
     * - Listens for user input in a loop, processing commands prefixed with `/`
     *   and handling chat messages.
     * - Commands are executed via the server's command manager.
     * - Chat messages trigger a `UserChatEvent`, applying cooldown restrictions
     *   if applicable.
     *
     * The method ensures that resources are properly closed in case of exceptions
     * or upon terminating the session. This includes closing the input and output
     * streams, and shutting down the socket connection if necessary.
     *
     * Error Handling:
     * - IO exceptions during user input or output result in resource cleanup
     *   and session shutdown.
     *
     * Assumptions:
     * - The server provides valid configurations for chat cooldown.
     * - The input and output streams are correctly initialized before execution.
     */
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

    /**
     * Terminates the current connection and handles necessary cleanup.
     *
     * This method shuts down the connection by performing the following:
     * - Sets the running state to false, signaling the termination of the session.
     * - Removes the current connection from the server's active connections list.
     * - Triggers a UserLeaveEvent if a username is associated with the connection.
     * - Closes input and output streams associated with the connection.
     * - Ensures that the socket input and output streams are closed.
     * - Closes the user socket if it has not already been closed.
     *
     * Any IOException that occurs during the shutdown process is caught and ignored.
     */
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
    public ReentrantLock getReentrantLock() {
        return super.getReentrantLock();
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
