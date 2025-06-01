package com.habbashx.tcpserver.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habbashx.tcpserver.event.AuthenticationEvent;
import com.habbashx.tcpserver.handler.UserHandler;

import com.habbashx.tcpserver.socket.Server;

import com.habbashx.tcpserver.user.UserDetails;
import com.habbashx.tcpserver.util.UserUtil;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;
import static com.habbashx.tcpserver.util.UserUtil.isValidEmail;
import static com.habbashx.tcpserver.util.UserUtil.isValidPhoneNumber;
import static com.habbashx.tcpserver.util.UserUtil.isValidUsername;

/**
 * DefaultAuthentication is a final implementation of the Authentication class, designed to manage user authentication
 * for a server environment. This class supports user registration and login using multiple storage types including
 * CSV, JSON, and SQL. It enforces user validation and locks critical sections during authentication to prevent race
 * conditions. It also triggers events for user authentication actions.
 *
 * Thread Safety:
 * The DefaultAuthentication class uses ReentrantLock to ensure thread-safe operations during user registration
 * and login processes.
 *
 * Features:
 * - Supports different data storage types: CSV, JSON, and SQL for user information.
 * - Ensures user details are validated before registration.
 * - Hashes user passwords securely using BCrypt for enhanced security.
 * - Triggers server events upon successful or failed authentication attempts.
 *
 * Storage Types Supported:
 * - CSV: Stores user data in a CSV file.
 * - JSON: Stores user data in a JSON file.
 * - SQL: Interfaces with an SQL database for user data storage and retrieval.
 *
 * Configuration:
 * The storage type is determined by server settings and can be configured as CSV, JSON, or SQL.
 *
 * Event Handling:
 * The DefaultAuthentication class interacts with the server's event management system to notify other components
 * of authentication events (e.g., successful registration or login, failed attempts).
 *
 * Validation Rules:
 * - Usernames must not contain symbols or spaces.
 * - Phone numbers must be valid.
 * - Emails must be valid based on a predefined validation process.
 *
 * Preconditions:
 * - The Server instance provided must be configured properly with server settings specifying the authentication
 *   storage type.
 * - The UserHandler parameter in registration and login methods must not be null.
 *
 * Exceptions:
 * - Throws RuntimeException in case of an IO, SQL, or other unexpected errors.
 */
@SuppressWarnings("deprecation")
public final class DefaultAuthentication extends Authentication {

    private final Server server;

    /**
     * Represents the header structure used for user-related data in the authentication mechanism.
     * This constant array defines the standardized field names expected for user information
     * in various contexts such as data storage, processing, or transmission.
     *
     * The fields include:
     * - "userIP": The IP address of the user.
     * - "userID": A unique identifier for the user.
     * - "userRole": The user's role or permissions within the system.
     * - "username": The username of the user.
     * - "password": The user's password.
     * - "userEmail": The email address associated with the user.
     * - "phoneNumber": The user's phone number.
     * - "isActiveAccount": A flag indicating whether the user's account is active.
     *
     * This header is likely to be used as a shared reference to ensure consistency
     * in handling user data across different methods or modules.
     */
    private static final String[] HEADER = {"userIP","userID","userRole","username","password","userEmail","phoneNumber","isActiveAccount"};

    /**
     * Specifies the type of storage mechanism used for managing authentication data.
     *
     * This variable determines how user credentials and related authentication
     * information are stored and accessed. It can be one of the following:
     * - CSV: Data is stored in a CSV file.
     * - SQL: Data is stored in a structured relational database.
     * - JSON: Data is stored in a JSON file.
     *
     * The choice of storage type impacts how authentication operations
     * (e.g., user registration and login) are implemented in the application.
     */
    private final AuthStorageType authStorageType;
    private final File file;
    /**
     * Represents the CSV format configuration used for reading or writing CSV data.
     * This is initialized with a default CSV format and includes specified headers.
     * It ensures a consistent approach to handling CSV data within the application.
     */
    private final CSVFormat format = CSVFormat.DEFAULT.withHeader(HEADER);
    /**
     * An instance of {@code ObjectMapper} used for serializing and deserializing objects
     * to and from JSON format. This variable serves as the primary object mapper
     * throughout the application to handle JSON processing tasks efficiently.
     *
     * The {@code ObjectMapper} provides functionality for:
     * - Parsing JSON content into Java objects.
     * - Generating JSON content from Java objects.
     * - Configuring serialization and deserialization features.
     * - Registering custom serializers and deserializers if needed.
     *
     * This instance is declared as {@code final} to ensure it is immutable and shared safely
     * across multiple components or threads within the application.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String USER_ALREADY_CONNECTED_MESSAGE = RED+"user already connected to server"+RESET;

    public DefaultAuthentication(Server server) {
        this.server = server;
        final String authType = this.server.getServerSettings().getAuthStorageType().toUpperCase();
        final String fileType= authType.toLowerCase();
        authStorageType = AuthStorageType.valueOf(authType);
        file = new File("data/users.%s".formatted(fileType));
    }

    /**
     * Registers a new user with the provided credentials and additional details.
     * The method handles validation of the username, phone number, and email, as well as user existence checks.
     * It utilizes a reentrant lock for thread safety and supports different authentication storage types
     * such as CSV, SQL, and JSON for storing user information.
     *
     * @param username The username of the user being registered. Must not be null and must adhere to the valid username rules.
     * @param password The password for the user being registered. Must not be null.
     * @param email The email address of the user. Can be null but if provided, must be valid.
     * @param phoneNumber The phone number of the user. Can be null but if provided, must be valid.
     * @param userHandler A user handler that manages user interactions and messaging during the registration process. Must not be null.
     */
    @Override
    public void register(@NotNull String username ,@NotNull String password ,String email, String phoneNumber ,@NotNull UserHandler userHandler) {

        final ReentrantLock reentrantLock = userHandler.getReentrantLock();
        reentrantLock.lock();

        try {
            if (!isUserExists(username)) {

                if (!isValidUsername(username)) {
                    userHandler.sendMessage(RED + "invalid username please try to select name without any symbols and spaces" + RESET);
                    return;
                }

                if (!isValidPhoneNumber(phoneNumber)) {
                    userHandler.sendMessage(RED + "invalid phone number" + RESET);
                    return;
                }

                if (!isValidEmail(email)) {
                    userHandler.sendMessage(RED + "invalid email" + RESET);
                }

                if (!file.exists()) {
                    if (!authStorageType.equals(AuthStorageType.SQL)) {
                        server.getServerLogger().info("users.%s file have been created successfully".formatted(authStorageType));
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                switch (authStorageType) {
                    case CSV -> csvRegister(username, password, email, phoneNumber, userHandler);
                    case SQL -> sqlRegister(username, password, email, phoneNumber, userHandler);
                    case JSON -> jsonRegister(username, password, email, phoneNumber, userHandler);
                }
            }
        } finally {
            reentrantLock.unlock();
        }
    }



    /**
     * Authenticates a user by verifying the provided username and password using the selected storage type.
     * This method is synchronized to ensure thread safety during the login process.
     *
     * @param username the username of the user attempting to log in; must not be null
     * @param password the password of the user attempting to log in; must not be null
     * @param userHandler the user handler object representing the state and context of the user; must not be null
     */
    @Override
    public void login(@NotNull String username, @NotNull String password ,@NotNull UserHandler userHandler) {

        final ReentrantLock reentrantLock = userHandler.getReentrantLock();

        reentrantLock.lock();
        try {
            switch (authStorageType) {
                case CSV -> csvLogin(username, password, userHandler);
                case JSON -> jsonLogin(username, password, userHandler);
                case SQL -> sqlLogin(username, password, userHandler);
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * Registers a new user with the provided credentials and additional details in a CSV file.
     * This method checks if the username already exists. If the user does not exist, their details
     * (including a hashed password) are added to the CSV file, and their user details are updated in
     * the provided {@code UserHandler}. An event is triggered to confirm the registration status.
     *
     * @param username The username of the user being registered. Must not already exist in the system.
     * @param password The password of the user. It will be hashed using BCrypt before storage.
     * @param email The email address of the user. This is associated with the account being created.
     * @param phoneNumber The phone number of the user. It is stored for account management purposes.
     * @param userHandler The {@code UserHandler} instance associated with the user's session. Must not be null.
     *                     This is used to set user details and trigger registration events.
     */
    private void csvRegister(String username , String password , String email, String phoneNumber , @NotNull UserHandler userHandler) {

        if (!isUserExists(username)) {
            try (final CSVPrinter printer = new CSVPrinter(new BufferedWriter(new FileWriter(file)), format)) {
                final String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                final String userHostAddress = userHandler.getUserSocket().getLocalAddress().getHostAddress();
                final String id = UserUtil.generateRandomID();
                final Role role = Role.DEFAULT;
                printer.printRecord(
                        UserUtil.getUserHostAddress(),
                        UserUtil.generateRandomID(),
                        Role.DEFAULT, // default rank is the default rank for newest users
                        username,
                        hashedPassword,
                        email,
                        phoneNumber,
                        true
                );


                userHandler.setUserDetails(
                        new UserDetails().builder()
                                .userIP(userHostAddress)
                                .userID(id)
                                .userRole(role)
                                .username(username)
                                .userEmail(email)
                                .phoneNumber(phoneNumber)
                                .activeAccount(true)
                                .build()
                );

                server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler,true,true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler,false,false));
        }
    }

    /**
     * Registers a new user in the SQL database. The method checks if the user already exists,
     * hashes the provided password, and adds the user details to the database. Additionally,
     * triggers authentication-related events based on the success or failure of the registration process.
     *
     * @param username the username of the user to be registered
     * @param password the plaintext password of the user to be registered
     * @param email the email address of the user to be registered
     * @param phoneNumber the phone number of the user to be registered
     * @param userHandler the handler managing the user's session and related details
     */
    private void sqlRegister(String username, String password, String email, String phoneNumber, UserHandler userHandler) {

        try {
            if (!isUserExists(username)) {
                final String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                final String userAddress = userHandler.getUserSocket().getInetAddress().toString();
                final String id = UserUtil.generateRandomID();
                final Role role = Role.DEFAULT;

                userHandler.setUserDetails(
                        new UserDetails().builder()
                                .userID(id)
                                .userIP(userAddress)
                                .userRole(role)
                                .username(username)
                                .userEmail(email)
                                .phoneNumber(phoneNumber)
                                .activeAccount(true)
                                .build()
                );
                server.getServerDataManager().getUserDao().insertUser(username, hashedPassword,id,userAddress, role.toString(), email, phoneNumber, true);
                server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler,true,true));
            } else {
                server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler,false,false));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers a new user into the system and stores the user data in JSON format.
     * This method checks for user existence and prevents duplicate registrations.
     * It also hashes the user's password for security and triggers an authentication event.
     *
     * @param username The username of the user being registered. Must comply with the valid username format.
     * @param password The password of the user being registered. Must not be null.
     * @param email The email address of the user being registered. Can be null but should be valid if provided.
     * @param phoneNumber The phone number of the user being registered. Can be null but should be valid if provided.
     * @param userHandler A non-null handler for managing user-specific interactions and state during registration.
     */
    private void jsonRegister(String username, String password, String email, String phoneNumber, @NotNull UserHandler userHandler) {

        if (!isUserExists(username)) {
            try {
                final Map<String, Object> map = new HashMap<>();
                final String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                final String userAddress = userHandler.getUserSocket().getInetAddress().toString();
                final String id = UserUtil.generateRandomID();
                final Role role = Role.DEFAULT;

                map.put("userIP", userAddress);
                map.put("userID", id);
                map.put("userRole", role);
                map.put("username", username);
                map.put("password", hashedPassword);
                map.put("userEmail", email);
                map.put("phoneNumber", phoneNumber);
                map.put("isActiveAccount", true);
                final List<Map<String, Object>> list = mapper.readValue(file, new TypeReference<>(){});

                list.add(map);
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, list);

                userHandler.setUserDetails(
                        new UserDetails().builder()
                                .userIP(userAddress)
                                .userID(id)
                                .userRole(role)
                                .username(username)
                                .userEmail(email)
                                .phoneNumber(phoneNumber)
                                .activeAccount(true)
                                .build()
                );
                server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler,true,true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler,false,false));
        }
    }

    /**
     * Authenticates a user using credentials stored in a CSV file. If the user is found and the password matches,
     * an authentication success event is triggered. Otherwise, a failure event is triggered. If the user is already
     * connected, a corresponding message is sent and the user session is terminated.
     *
     * @param username the username of the user attempting to log in; must not be null
     * @param password the password of the user attempting to log in; must not be null
     * @param userHandler the user handler object representing the user's state and context; must not be null
     */
    private void csvLogin(String username,String password ,UserHandler userHandler) {

        if (!isUserConnected(username)) {
            try (final Reader reader = new FileReader("data/users.csv")) {
                final Iterable<CSVRecord> users = CSVFormat.DEFAULT.parse(reader);

                for (final CSVRecord record : users) {
                    if (record.get("username").equals(username)) {
                        if (BCrypt.checkpw(password, record.get("password"))) {
                            server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler, true, false));
                            return;
                        }
                    }
                }
                server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler, false, false));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            userHandler.sendMessage(USER_ALREADY_CONNECTED_MESSAGE);
            userHandler.shutdown();
        }
    }

    /**
     * Authenticates a user by verifying their username and password using SQL as the storage type.
     * If the provided credentials are valid and the user is not already connected, the user's details are
     * updated, and an authentication success event is triggered. Otherwise, an authentication failure event
     * is triggered. If the user is already connected, a notification is sent, and the session is terminated.
     *
     * @param username the username of the user attempting to log in; must not be null
     * @param password the password of the user attempting to log in; must not be null
     * @param userHandler the user handler object that manages the user's session and messaging interactions; must not be null
     */
    private void sqlLogin(String username, String password ,UserHandler userHandler) {

        try {
            if (!isUserConnected(username)) {
                final UserDetails userDetails = server.getServerDataManager().getUserByUsername(username);


                if (userDetails != null) {
                    final String hashedPassword = server.getServerDataManager().getUserDao().getHashedPassword(username);
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        userHandler.setUserDetails(userDetails);
                        server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler,true,false));
                    } else {
                        server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler,false,false));
                    }
                }

            } else {
                userHandler.sendMessage(USER_ALREADY_CONNECTED_MESSAGE);
                userHandler.shutdown();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Authenticates a user by validating the provided username and password against user data stored in JSON format.
     * If authentication is successful, the user's details are prepared and``` storedjava in the UserHandler instance
     .
     /**
     * * An Handles authentication the event login is process triggered for, a and user the based connection on state provided of JSON the data user.
     is * managed This.
     method *
     verifies * the @ providedparam username username and      password The against username stored of user the information user
     attempting * to and log updates in the; user's must session not details be upon null successful.
     authentication *.
     @ *
     param * password @     param The username password The of username the of user the attempting user to attempting log to in log; in must.
     not * be @ nullparam password.
     * The @ passwordparam corresponding toer   Handler the   username.
     The * User @Handlerparam instance representing userHandler the Object user responsible; manages handling the user user's-related state operations and, interactions such during
     *  as the
     login * process.                    storing Must session not details be and null sending.
     messages */
    private void jsonLogin(String username ,String password ,UserHandler userHandler) {

        try {
            if (!isUserConnected(username)) {
                final List<Map<String, Object>> users = mapper.readValue(file, new TypeReference<>() {
                });

                for (final Map<String, Object> user : users) {

                    if (user.get("username").equals(username)) {
                        if (BCrypt.checkpw(password, (String) user.get("password"))) {

                            userHandler.setUserDetails(
                                    new UserDetails().builder()
                                            .userIP((String) user.get("userIP"))
                                            .userID((String) user.get("userID"))
                                            .userRole(Role.valueOf((String) user.get("userRole")))
                                            .username(username)
                                            .userEmail((String) user.get("userEmail"))
                                            .phoneNumber((String) user.get("phoneNumber"))
                                            .activeAccount((boolean) user.get("isActiveAccount"))
                                            .build()
                            );
                            server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler,true,false));
                            return;
                        }
                    }
                }
                server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler,false,false));
            } else {
                userHandler.sendMessage(RED+"user already connected to the server"+RESET);
                userHandler.shutdown();
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks whether a user with the specified username exists in the system.
     *
     * @param username The username of the user to be checked. Must not be null.
     * @return true if a user with the given username exists, false otherwise.
     */
    private boolean isUserExists(String username) {
        final UserDetails userDetails = server.getServerDataManager().getUserByUsername(username);
        return userDetails != null;
    }

    /**
     * Checks whether a user is currently connected to the server.
     * This method iterates through the list of active user connections
     * to verify if a connection exists for the specified username.
     *
     * @param username The username of the user to check. Must not be null.
     * @return true if the user is connected; false otherwise.
     */
    private boolean isUserConnected(String username) {

        return server.getConnections().stream()
                .filter(connectionHandler -> connectionHandler instanceof UserHandler)
                .map(connectionHandler -> (UserHandler) connectionHandler)
                .map(user -> user.getUserDetails().getUsername())
                .filter(Objects::nonNull)
                .anyMatch(name -> name.equals(username));
    }

    public Server getServer() {
        return server;
    }

}