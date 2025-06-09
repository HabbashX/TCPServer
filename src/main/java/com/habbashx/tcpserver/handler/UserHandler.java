package com.habbashx.tcpserver.handler;

import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.event.UserChatEvent;
import com.habbashx.tcpserver.event.UserLeaveEvent;
import com.habbashx.tcpserver.handler.connection.ConnectionHandler;
import com.habbashx.tcpserver.io.CountingOutputStream;
import com.habbashx.tcpserver.security.auth.Authentication;
import com.habbashx.tcpserver.security.container.NonVolatilePermissionContainer;
import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.user.UserDetails;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import static com.habbashx.tcpserver.logger.ConsoleColor.*;

/**
 * Handles user interactions and communication with the server.
 * <p>
 * This class manages the lifecycle of a user connection, including registration, login,
 * message handling, and```java
 * /**
 * Handles user interactions and communication with the server.
 * <p>
 * This class manages the lifecycle of user sessions, including authentication,
 * message handling, and interaction through commands. It extends the functionality
 * of {@link CommandSender} for executing commands and implements {@link Runnable}
 * for running user interaction on command execution. It operates as a thread, enabling asynchronous
 * communication between the user and the server.
 * <p>
 * UserHandler extends CommandSender, inheriting its functionalities for
 * executing commands and managing message communication. It also implements
 * Runnable to allow concurrent execution.
 * <p>
 * The class is responsible for maintaining user details, handling authentication,
 * and interacting with server events like messaging and user connection management.
 * <p>
 * Thread-safety is managed internally via the use of locking in the parent class
 * and proper resource cleanup upon shutting down the connection.
 */
public final class UserHandler extends ConnectionHandler implements CommandSender {

    /**
     * Represents the details of a user associated with this handler.
     * <p>
     * The `userDetails` field stores information about the user, including identity,
     * role, contact information, and account status. This information is used to manage
     * interactions between the user and the system, and to enforce role-based behavior
     * and permissions.
     */
    private UserDetails userDetails;

    /**
     * A {@link BufferedReader} instance used for reading input from the user's socket connection.
     * <p>
     * This variable facilitates processing incoming data from the client, enabling the server
     * to handle user commands or messages. The {@code input} is initialized during the creation
     * of a {@code UserHandler} instance and remains immutable thereafter. It provides a
     * connection-specific stream for reading text-based input in a blocking and thread-safe manner.
     * <p>
     * It is vital for interpreting the user's communication with the server, allowing the server
     * to perform tasks such as authentication, command execution, or message broadcasting.
     * <p>
     * Note that this reader must not be closed directly, as it is managed within the lifecycle
     * of the {@code UserHandler} instance.
     */
    private final BufferedReader input;
    /**
     * A {@code PrintWriter} used to send output data to the connected user.
     * <p>
     * This field is initialized during the construction of the {@code UserHandler} instance
     * and is associated with the user's socket output stream. It facilitates sending messages
     * or responses to the user during the connection lifecycle.
     * <p>
     * Being declared as {@code final}, it ensures that the reference to the output stream
     * remains constant, preventing reassignment after initialization. Thread-safety must be
     * handled externally if needed for concurrent access.
     */
    private final PrintWriter output;

    /**
     * Represents an authentication mechanism for the associated {@link UserHandler}.
     * <p>
     * This field is used to manage user authentication processes such as login
     * and registration within the system. The {@code Authentication} class,
     * which this field is an instance of, provides abstract methods to handle
     * these processes securely and efficiently.
     * <p>
     * Being declared as {@code final}, this field is immutable and ensures that
     * the authentication mechanism for a specific {@link UserHandler} instance
     * remains consistent throughout its lifecycle.
     * <p>
     * The field is integral to enabling user-specific authentication workflows,
     * including establishing and maintaining authenticated sessions.
     */
    private final Authentication authentication;

    /**
     * A list of integer values representing the permissions assigned to the user
     * associated with this handler. These permissions determine the actions or
     * operations that the user is allowed to perform within the system.
     * <p>
     * This list is immutable in terms of its reference, ensuring thread-safety,
     * but may have elements modified through dedicated methods provided in the
     * class, such as adding or removing specific permissions.
     * <p>
     * Used primarily to manage and validate user permissions for various commands
     * and interactions handled by the server.
     */
    private final List<Integer> permissions = new ArrayList<>();

    /**
     * A {@link CountingOutputStream} instance used within the {@code UserHandler} class
     * to track the amount of data written to the output stream associated with a user connection.
     * <p>
     * This object serves as a decorator for the output stream, allowing real-time measurement
     * of the number of bytes transmitted during communication between the server and client.
     * <p>
     * The {@code countingOutputStream} is initialized when setting up the user's connection,
     * and its byte count can be accessed for monitoring purposes or other data management needs.
     * <p>
     * Thread-safety is ensured through the atomic operations provided by the underlying {@code CountingOutputStream}.
     */
    private final CountingOutputStream countingOutputStream;

    /**
     * A boolean variable that indicates whether the {@link UserHandler} instance
     * is actively running its operations.
     * <p>
     * This variable is set to {@code true} by default, meaning the instance is actively
     * processing, and can be modified to {@code false} to signal the instance to stop its execution.
     * It is primarily used to control the execution flow of the {@link UserHandler#run()} method,
     * allowing for graceful shutdown or termination of the handler's processes.
     */
    private boolean running = true;

    /**
     * Constructs a UserHandler instance for managing a user connection and server communication.
     *
     * @param user   the SSL socket representing the user's connection; must not be null
     * @param server the server instance associated with this user handler; must not be null
     */
    public UserHandler(@NotNull SSLSocket user, @NotNull Server server) {
        super(user, server);
        userDetails = new UserDetails();
        authentication = server.getAuthentication();
        try {
            countingOutputStream = new CountingOutputStream(user.getOutputStream());
            input = new BufferedReader(new InputStreamReader(user.getInputStream()));
            output = new PrintWriter(countingOutputStream, true);
            setupSettings();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the main logic for handling user interaction, authentication, and
     * communication with the server. This method is invoked when the thread for
     * the associated user handler starts.
     * <p>
     * The `run` method performs the following tasks:
     * 1. Prompts the user to either register or log in, executing the corresponding
     * authentication process based on user input.
     * - Registers a new user with a username, password, email, and phone number.
     * - Logs in an existing user with their username and password.
     * 2. If no valid input is provided, terminates the session.
     * <p>
     * Once authenticated, the method:
     * - Initializes a chat cooldown mechanism using server settings.
     * - Listens for user input in a loop, processing commands prefixed with `/`
     * and handling chat messages.
     * - Commands are executed via the server's command manager.
     * - Chat messages trigger a `UserChatEvent`, applying cooldown restrictions
     * if applicable.
     * <p>
     * The method ensures that resources are properly closed in case of exceptions
     * or upon terminating the session. This includes closing the input and output
     * streams, and shutting down the socket connection if necessary.
     * <p>
     * Error Handling:
     * - IO exceptions during user input or output result in resource cleanup
     * and session shutdown.
     * <p>
     * Assumptions:
     * - The server provides valid configurations for chat cooldown.
     * - The input and output streams are correctly initialized before execution.
     */
    @Override
    public void run() {

        try {

            authenticationRequest();

            final int cooldownSecond = Integer.parseInt(getServer().getServerSettings().getUserChatCooldown());
            final UserChatEvent userChatEvent = new UserChatEvent(userDetails.getUsername(), this, cooldownSecond);

            if (running) {
                do {
                    String message;
                    while (((message = input.readLine())) != null) {
                        if (message.startsWith("/")) {
                            getServer().getCommandManager().executeCommand(userDetails.getUsername(), message, this);
                        } else {
                            userChatEvent.setMessage(message);
                            getServer().getEventManager().triggerEvent(userChatEvent);
                        }
                    }
                } while (running);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    /**
     * Handles the authentication request process by prompting the user to either register
     * or log in and redirecting them to the appropriate functionality based on their input.
     * <p>
     * The method performs the following steps:
     * 1. Displays a prompt to guide the user to choose between registration or login.
     * 2. Reads the user input to determine their choice.
     * - If the input is "register", the method invokes {@code sendRegisterRequest()} to
     * start the registration process.
     * - If the input is "login", the method invokes {@code sendLoginRequest()} to
     * initiate the login process.
     * - If the input is invalid, an error message is displayed, and the method is
     * recursively invoked to request a valid choice.
     * <p>
     * This method relies on user input through the input stream and sends output using the
     * {@code sendMessage()} method.
     * <p>
     * Error Handling:
     * - Any I/O issues during input or output operations may propagate as IOException.
     * - Recursion handles invalid input until a proper choice is made or the session is terminated.
     *
     * @throws IOException if an I/O error occurs during input or output handling.
     */
    private void authenticationRequest() throws IOException {
        sendMessage("%s%sregister%s or %s%slogin%s".formatted(BG_ORANGE, BLACK, RESET, BG_BRIGHT_BLUE, BLACK, RESET));
        final String choice = input.readLine();
        switch (choice) {
            case "register" -> sendRegisterRequest();
            case "login" -> sendLoginRequest();
            default -> {
                sendMessage(RED + "please register or login" + RESET);
                authenticationRequest();
            }
        }
    }

    /**
     * Handles the process of registering a new user by interacting with the client
     * through the input and output streams. The method prompts the client for the
     * required registration details, such as username, password, email, and phone
     * number, and uses the associated authentication mechanism to complete the registration.
     * <p>
     * The method performs the following steps:
     * 1. Sends a prompt requesting the username and reads the client's input.
     * 2. Sends a prompt requesting the password and reads the client's input.
     * 3. Sends a prompt requesting the email and reads the client's input.
     * 4. Sends a prompt requesting the phone number and reads the client's input.
     * 5. Invokes the `register` method of the `authentication` instance, passing
     * the collected details and the current `UserHandler` as arguments.
     * <p>
     * Error Handling:
     * - Any errors during input or output operations may propagate as an IOException.
     * <p>
     * Preconditions:
     * - The `input` stream must be correctly initialized for reading.
     * - The `authentication` instance must be configured to handle the registration process.
     *
     * @throws IOException if an I/O error occurs during input or output handling.
     */
    private void sendRegisterRequest() throws IOException {
        sendMessage("enter username");
        final String username = input.readLine();
        sendMessage("enter password");
        final String password = input.readLine();
        sendMessage("enter email");
        final String email = input.readLine();
        sendMessage("enter phone number");
        final String phoneNumber = input.readLine();
        authentication.register(username, password, email, phoneNumber, this);
    }

    /**
     * Initiates the login process for a user by prompting them to enter their
     * username and password, and performs authentication using the provided
     * credentials.
     * <p>
     * The method performs the following steps:
     * 1. Sends a prompt message instructing the user to enter their username.
     * 2. Reads the username input from the input stream.
     * 3. Sends a prompt message instructing the user to enter their password.
     * 4. Reads the password input from the input stream.
     * 5. Calls the authentication mechanism to validate the credentials and
     * handle the login process.
     * <p>
     * Dependencies:
     * - The input stream must be correctly initialized to read user input.
     * - The sendMessage method is used to send prompt messages to the user.
     * - The authentication.login method validates the user-provided credentials.
     * <p>
     * Error Handling:
     * - If an I/O error occurs while reading the inputs, the exception is propagated
     * to the caller.
     * <p>
     * Preconditions:
     * - The input stream and authentication mechanism should not be null before the method invocation.
     *
     * @throws IOException if an I/O error occurs while reading user input.
     */
    private void sendLoginRequest() throws IOException {
        sendMessage("enter username");
        final String username = input.readLine();
        sendMessage("enter password");
        final String password = input.readLine();
        authentication.login(username, password, this);
    }

    /**
     * Sends a message to the output stream associated with this handler.
     *
     * @param message the message to be sent; must not be null
     */
    public void sendMessage(String message) {
        output.println(message);
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    @Override
    public SSLSocket getUserSocket() {
        return super.getUserSocket();
    }

    public BufferedReader getReader() {
        return input;
    }

    public PrintWriter getWriter() {
        return output;
    }

    @Override
    public Server getServer() {
        return super.getServer();
    }

    /**
     * Checks if the role associated with the user has a specific permission.
     *
     * @param permission the permission value to check
     * @return {@code true} if the user's role has the specified permission; {@code false} otherwise
     */
    public boolean isRoleHasPermission(int permission) {
        return userDetails.getUserRole().getPermissions().contains(permission);
    }

    @Override
    public boolean hasVolatilePermission(int permission) {
        return permissions.contains(0);
    }

    @Override
    public void addPermission(int permission) {
        permissions.add(permission);
    }

    @Override
    public void removePermission(int permission) {
        permissions.remove(permission);
    }

    @Override
    public void addPermissions(List<Integer> permissions) {
        this.permissions.addAll(permissions);
    }

    @Override
    public void removePermissions(List<Integer> permissions) {
        this.permissions.removeAll(permissions);
    }


    @Override
    public List<Integer> getHandlerPermissions() {
        return permissions;
    }

    @Override
    public NonVolatilePermissionContainer getNonVolatilePermissionContainer() {
        return super.getNonVolatilePermissionContainer();
    }

    public CountingOutputStream getCountingOutputStream() {
        return countingOutputStream;
    }

    /**
     * Terminates the current connection and handles necessary cleanup.
     * <p>
     * This method shuts down the connection by performing the following:
     * - Sets the running state to false, signaling the termination of the session.
     * - Removes the current connection from the server's active connections list.
     * - Triggers a UserLeaveEvent if a username is associated with the connection.
     * - Closes input and output streams associated with the connection.
     * - Ensures that the socket input and output streams are closed.
     * - Closes the user socket if it has not already been closed.
     * <p>
     * Any IOException that occurs during the shutdown process is caught and ignored.
     */
    public void shutdown() {

        try {
            running = false;

            getServer().getConnections().remove(this);
            final String username = userDetails.getUsername();

            if (username != null) {
                getServer().getEventManager().triggerEvent(new UserLeaveEvent(username, this));
            }

            input.close();
            output.close();
            if (!getUserSocket().isInputShutdown() && !getUserSocket().isOutputShutdown()) {
                getUserSocket().shutdownInput();
                getUserSocket().shutdownOutput();
                getUserSocket().getInputStream().close();
                getUserSocket().getOutputStream().close();
            }

            if (!getUserSocket().isClosed()) {
                getUserSocket().close();
            }


        } catch (IOException ignored) {
        }
    }

    @Override
    public ReentrantLock getReentrantLock() {
        return CommandSender.super.getReentrantLock();
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

    @Contract(pure = true)
    @Override
    public @NotNull String getHandlerType() {
        return "UserHandler";
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getHandlerDescription() {
        return """
                The `UserHandler` class is responsible for managing and facilitating communication between
                the server and a connected user. It extends `ConnectionHandler` and implements the `CommandSender`
                interface.
                
                Key Details:
                
                1. Purpose:
                   - Handles user interaction, including authentication, command execution, and messaging.
                   - Maintains user-specific session controls and permissions.
                
                2. Attributes:
                   - `UserDetails`: Stores user identity, role, and account status.
                   - `BufferedReader input`: Reads user commands/messages from the connection.
                   - `PrintWriter output`: Sends messages or responses to the user.
                   - `Authentication`: Manages login and registration workflows securely.
                   - `CountingOutputStream`: Tracks the volume of data sent to the user.
                   - `List<Integer> permissions`: Represents user-specific permissions for role-based actions.
                   - `boolean running`: Indicates whether the handler is actively processing interactions.
                
                3. Functionalities:
                   - Authentication:
                     - Guides users through login or registration processes.
                     - Validates user credentials using the `Authentication` class.
                   - Command & Messaging:
                     - Executes commands handled by the server's command manager.
                     - Processes and broadcasts user messages or triggers relevant events.
                   - Session Management:
                     - Starts, monitors, and shuts down user sessions gracefully.
                   - Resource Safety:
                     - Handles errors and ensures proper closure of input/output streams.
                
                4. Core Methods:
                   - `run()`: Main loop for executing user interaction logic.
                   - `authenticationRequest()`: Prompts the user for login or registration.
                   - `sendRegisterRequest()`: Handles new user registration.
                   - `sendLoginRequest()`: Handles user login.
                   - `sendMessage(String message)`: Sends messages to the user.
                
                Designed for maintaining secure and reliable communication with connected users,
                the `UserHandler` ensures scalability, error handling, and proper resource management
                throughout the server-client interaction lifecycle.
                """;
    }
}
