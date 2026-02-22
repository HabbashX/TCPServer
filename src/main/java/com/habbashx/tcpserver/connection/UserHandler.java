package com.habbashx.tcpserver.connection;

import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.connection.handler.ConnectionHandler;
import com.habbashx.tcpserver.event.UserChatEvent;
import com.habbashx.tcpserver.event.UserLeaveEvent;
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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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
     * A thread-safe queue that buffers messages to be sent to the connected user.
     * <p>
     * This queue is used to store messages before they are transmitted to the user,
     * allowing asynchronous message handling without blocking the sender. It implements
     * {@link ConcurrentLinkedQueue} to ensure thread-safe operations in a multi-threaded environment.
     * <p>
     * Messages are added to this queue via the {@link #sendMessage(String)} method
     * and are processed and sent by the {@link #trySend()} method.
     */
    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>();

    /**
     * An atomic boolean flag that indicates whether a sending operation is currently in progress.
     * <p>
     * This flag is used to coordinate message sending operations, ensuring that only one
     * thread at a time is processing and transmitting messages from the {@code messageQueue}.
     * It uses atomic compare-and-set operations to avoid race conditions and ensure
     * thread-safety without explicit synchronization.
     * <p>
     * The flag is set to {@code true} when a send operation starts and reset to {@code false}
     * when the operation completes, allowing subsequent send attempts to proceed.
     */
    private final AtomicBoolean isSending = new AtomicBoolean(false);

    /**
     * Constructs a UserHandler instance for managing a user connection and server communication.
     *
     * @param user   the SSL socket representing the user's connection; must not be null
     * @param server the server instance associated with this user handler; must not be null
     */
    public UserHandler(@NotNull SSLSocket user, @NotNull Server server) throws IOException {
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

            assert getServer().getServerSettings().getUserChatCooldown() != null;
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
     * Sends a message to the user by adding it to the message queue and triggering the send mechanism.
     * <p>
     * This method ensures thread-safe message delivery by queuing the message and attempting to send it
     * asynchronously without blocking the caller.
     *
     * @param message the message to be sent to the user
     */
    public void sendMessage(String message) {
        messageQueue.offer(message);
        trySend();
    }

    /**
     * Attempts to send all queued messages to the user in an asynchronous manner.
     * <p>
     * This method uses an atomic flag to ensure that only one thread is sending messages at a time.
     * If a send operation is already in progress, this method will not initiate a new one.
     * The actual sending is delegated to the server's thread pool for non-blocking execution.
     */
    private void trySend() {

        if (isSending.compareAndSet(false, true)) {
            getServer().getThreadPool().submit(() -> {
                try {
                    while (true) {
                        String msg;

                        while ((msg = messageQueue.poll()) != null) {
                            output.println(msg);
                        }

                        isSending.set(false);

                        if (messageQueue.isEmpty()) {
                            break;
                        }
                        if (!isSending.compareAndSet(false, true)) {
                            break;
                        }
                        if (messageQueue.size() >= 200) {
                            this.shutdown();
                        }
                    }
                } catch (Exception e) {
                    isSending.set(false);
                    shutdown();
                }
            });
        }
    }

    /**
     * Sets the user details associated with this handler.
     *
     * @param userDetails the user details to be associated with this handler
     */
    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    /**
     * Retrieves the user details associated with this handler.
     *
     * @return the user details associated with this handler
     */
    public UserDetails getUserDetails() {
        return userDetails;
    }

    /**
     * Retrieves the SSL socket associated with this user handler.
     *
     * @return the SSL socket used for user communication
     */
    @Override
    public SSLSocket getUserSocket() {
        return super.getUserSocket();
    }

    /**
     * Retrieves the {@link BufferedReader} used for reading input from the user.
     *
     * @return the buffered reader for user input
     */
    public BufferedReader getReader() {
        return input;
    }

    /**
     * Retrieves the {@link PrintWriter} used for sending output to the user.
     *
     * @return the print writer for user output
     */
    public PrintWriter getWriter() {
        return output;
    }

    /**
     * Retrieves the server instance associated with this user handler.
     *
     * @return the server instance managing this user handler
     */
    @Override
    public Server getServer() {
        return (Server) super.getServer();
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

    /**
     * Checks if the user has a specific volatile permission.
     * <p>
     * Volatile permissions are temporary permissions that apply to the current session only.
     *
     * @param permission the permission value to check
     * @return {@code true} if the user has the volatile permission; {@code false} otherwise
     */
    @Override
    public boolean hasVolatilePermission(int permission) {
        return permissions.contains(0);
    }

    /**
     * Adds a single permission to the user's volatile permission list.
     *
     * @param permission the permission value to add
     */
    @Override
    public void addPermission(int permission) {
        permissions.add(permission);
    }

    /**
     * Removes a single permission from the user's volatile permission list.
     *
     * @param permission the permission value to remove
     */
    @Override
    public void removePermission(int permission) {
        permissions.remove(permission);
    }

    /**
     * Adds multiple permissions to the user's volatile permission list.
     *
     * @param permissions the list of permission values to add
     */
    @Override
    public void addPermissions(List<Integer> permissions) {
        this.permissions.addAll(permissions);
    }

    /**
     * Removes multiple permissions from the user's volatile permission list.
     *
     * @param permissions the list of permission values to remove
     */
    @Override
    public void removePermissions(List<Integer> permissions) {
        this.permissions.removeAll(permissions);
    }

    /**
     * Retrieves the list of volatile permissions assigned to this handler.
     *
     * @return the list of permission values assigned to this handler
     */
    @Override
    public List<Integer> getHandlerPermissions() {
        return permissions;
    }

    /**
     * Retrieves the non-volatile permission container associated with this handler.
     *
     * @return the non-volatile permission container
     */
    @Override
    public NonVolatilePermissionContainer getNonVolatilePermissionContainer() {
        return super.getNonVolatilePermissionContainer();
    }

    /**
     * Retrieves the {@link CountingOutputStream} instance associated with this handler.
     *
     * @return the counting output stream used for tracking data sent to the user
     */
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
    @Override
    public void shutdown() {

        try {
            running = false;

            getServer().getConnectionHandlers().remove(this);
            getServer().getAuthenticatedUsers().remove(getUserDetails().getUsername());
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

    /**
     * Retrieves the reentrant lock associated with this handler for thread synchronization.
     *
     * @return the reentrant lock used for synchronization
     */
    @Override
    public ReentrantLock getReentrantLock() {
        return CommandSender.super.getReentrantLock();
    }

    /**
     * Compares this UserHandler instance with another object for equality.
     * <p>
     * Two UserHandler instances are considered equal if they have the same user details.
     *
     * @param object the object to compare with
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UserHandler that)) return false;
        return Objects.equals(userDetails, that.userDetails);
    }

    /**
     * Returns the hash code value for this UserHandler instance.
     * <p>
     * The hash code is based on the user details associated with this handler.
     *
     * @return the hash code value for this UserHandler
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(userDetails);
    }

    /**
     * Returns the type identifier for this handler.
     *
     * @return the handler type as "UserHandler"
     */
    @Contract(pure = true)
    @Override
    public @NotNull String getHandlerType() {
        return "UserHandler";
    }

    /**
     * Returns a description of this handler.
     *
     * @return the handler description (currently empty)
     */
    @Contract(pure = true)
    @Override
    public @NotNull String getHandlerDescription() {
        return "";
    }
}
