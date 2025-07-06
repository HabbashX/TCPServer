package com.habbashx.tcpserver.event;

import com.habbashx.tcpserver.connection.UserHandler;

/**
 * The AuthenticationEvent class represents an event that occurs during an authentication operation.
 * This event is typically triggered during a user's login or registration process to indicate
 * whether the operation was successful or not.
 *
 * The class extends the Event class, providing additional information specific
 * to authentication actions, including the user handler involved in the operation,
 * the authentication status, and whether the operation was a registration attempt.
 */
public class AuthenticationEvent extends Event {

    /**
     * Represents a handler for managing a specific user in the context of an authentication event.
     *
     * The {@code userHandler} is responsible for processing user-specific operations, such as
     * authentication, registration, and communication with the server. It encapsulates functionality
     * for interacting with a user's input and output streams, handling user details, and managing
     * user events within the server.
     *
     * This variable is utilized for accessing and manipulating user-related data within the
     * {@link AuthenticationEvent} class.
     */
    private final UserHandler userHandler;
    /**
     * Represents whether the authentication event has been successfully processed.
     *
     * This boolean variable indicates if the user associated with the event has been
     * authenticated successfully. It is initialized upon the creation of an
     * {@code AuthenticationEvent} instance and remains immutable throughout the
     * event's lifecycle.
     *
     * A value of {@code true} signifies that the authentication process successfully
     * validated the user's credentials, while a value of {@code false} indicates a
     * failure in authentication.
     */
    private final boolean authenticated;
    /**
     * Indicates whether this authentication event is related to a user registration operation.
     *
     * This boolean variable determines if the current event was triggered during a user registration
     * process. A value of {@code true} specifies that the event corresponds to a registration action,
     * while {@code false} implies it is associated with a different authentication operation, such as login.
     *
     * This field is immutable and is set during the construction of an {@code AuthenticationEvent} instance.
     */
    private final boolean isRegisterOperation;

    /**
     * Constructs an AuthenticationEvent to signify the outcome of an authentication-related operation.
     *
     * @param userHandler the UserHandler representing the user involved in the authentication process
     * @param authenticated a boolean indicating whether the authentication operation was successful
     * @param isRegisterOperation a boolean indicating whether the operation was a registration (true) or a login (false)
     */
    public AuthenticationEvent(UserHandler userHandler,boolean authenticated,boolean isRegisterOperation) {
        super("AuthenticationEvent");
        this.userHandler = userHandler;
        this.authenticated = authenticated;
        this.isRegisterOperation = isRegisterOperation;
    }

    public UserHandler getUserHandler() {
        return userHandler;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isRegisterOperation() {
        return isRegisterOperation;
    }

    /**
     * Checks if the event has been cancelled.
     *
     * This method indicates whether the event in question
     * has been marked as cancelled, typically to prevent its
     * further propagation or execution.
     *
     * @return true if the event is cancelled; false otherwise.
     */
    @Override
    public boolean isCancelled() {
        return super.isCancelled();
    }
}
