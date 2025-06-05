package com.habbashx.tcpserver.security.auth;

import com.habbashx.tcpserver.handler.UserHandler;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract sealed class designed to handle user authentication processes such as registration and login.
 * This class serves as a base for specific implementations of authentication mechanisms.
 *
 * Permitted subclasses of this class must implement the methods provided to ensure
 * the functionality of user authentication.
 *
 * This abstract class defines the contract for handling authentication, requiring:
 *
 * - User registration with credentials such as username, password, and optional contact details (email and phone number).
 * - User login with credentials such as username and password.
 *
 * Authentication methods must also integrate a UserHandler to perform specific actions related to the user
 * during registration or login.
 */
public sealed abstract class Authentication permits DefaultAuthentication {
    /**
     * Registers a new user with the specified credentials and additional contact information.
     * This method must be implemented by subclasses of the Authentication class to handle
     * the registration process tailored to the specific needs of the authentication mechanism.
     *
     * @param username the username to be registered; must not be null
     * @param password the password for the username; must not be null
     * @param email the email address associated with the user; can be null
     * @param phoneNumber the phone number associated with the user; can be null
     * @param userHandler the handler for managing the user registration process; must not be null
     */
    public abstract void register(@NotNull String username, @NotNull String password, String email, String phoneNumber ,@NotNull UserHandler userHandler);
    /**
     * Authenticates a user by validating the provided credentials and performing
     * any actions defined by the provided UserHandler.
     *
     * @param username the username provided for authentication; must not be null.
     * @param password the password provided for authentication; must not be null.
     * @param userHandler an instance of UserHandler to handle user-related actions during login; must not be null.
     */
    public abstract void login(@NotNull String username , @NotNull String password ,@NotNull UserHandler userHandler);
}
