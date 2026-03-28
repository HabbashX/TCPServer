package com.habbashx.tcpserver.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.data.database.UserDao;
import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.security.auth.storage.AuthStorageType;
import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.user.UserDetails;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The ServerDataManager class is a utility class designed to manage and interact with user data
 * for a given server instance. It provides methods to fetch user information based on various
 * criteria such as username, user ID, or email. User data can be retrieved from multiple storage
 * types including CSV files, JSON files, or an SQL database.
 * <p>
 * This class also offers functionality to access online user information, such as retrieving
 * connected users via their username or ID.
 * <p>
 * The behavior of data retrieval methods is determined by the server's configured
 * authentication storage type.
 */
public final class ServerDataManager {

    private final Server server;
    /**
     * Represents the type of storage mechanism used for authentication data
     * within the server.
     * <p>
     * This variable determines the method of storing and managing user
     * authentication information, such as usernames, passwords, and roles.
     * It is utilized by the server to ensure appropriate interaction with
     * the specified storage medium.
     * <p>
     * The available storage types are defined in the {@link AuthStorageType}
     * enum, which includes options like CSV files, JSON files, or SQL databases.
     */
    private final AuthStorageType authStorageType;
    /**
     * Represents a reference to the {@link UserDao} instance, which provides data access operations
     * for user-related queries and manipulations on the underlying data source.
     * <p>
     * This variable is used to perform CRUD operations such as inserting, updating, deleting,
     * and retrieving user details from the data storage system through the {@link UserDao} class.
     * It serves as a bridge between the {@link ServerDataManager} class and the data source.
     * <p>
     * The {@link UserDao} instance encapsulates the logic for interacting with the database or other
     * data storage mechanisms, allowing high-level methods in the {@link ServerDataManager} class
     * to retrieve or manipulate user-related data seamlessly.
     * <p>
     * Designed as a final field to ensure immutability and preserve the integrity of the data access layer within
     * the {@link ServerDataManager} lifecycle.
     */
    private final UserDao userDao;

    public ServerDataManager(@NotNull Server server) {
        this.server = server;
        assert server.getServerSettings().getAuthStorageType() != null;
        @Nullable final String authType = server.getServerSettings().getAuthStorageType().toUpperCase();
        authStorageType = AuthStorageType.valueOf(authType);
        userDao = new UserDao(server);
    }

    /**
     * Retrieves an online user by their username.
     * This method checks the server's active connections to find a user whose username matches the provided input.
     *
     * @param username the username of the user to search for; must not be null.
     *                 If no username is provided or if it is null, the method will return null.
     * @return the {@link UserHandler} instance representing the online user with the specified username,
     * or null if no such user is currently online.
     */
    public @Nullable UserHandler getOnlineUserByUsername(String username) {

        return server.getAuthenticatedUsers().values()
                .stream()
                .filter(userHandler -> userHandler.getUserDetails().getUsername()
                        .equals(username)).findFirst().orElse(null);
    }

    /**
     * Retrieves an online user by their unique user ID.
     * This method iterates through the server's active connections to locate a user
     * whose user ID matches the provided input.
     *
     * @param id the unique identifier of the user to search for; must not be null.
     *           If no user ID is provided, or if the provided ID does not match
     *           any online user, the method will return null.
     * @return the {@link UserHandler} instance representing the online user with the specified user ID,
     * or null if no such user is currently online.
     */
    public @Nullable UserHandler getOnlineUserById(String id) {
        return server.getAuthenticatedUsers().values()
                .stream()
                .filter(userHandler -> userHandler.getUserDetails().getUserID().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves user details using the provided username.
     * This method queries the user storage system based on the configured authentication storage type
     * (e.g., CSV, JSON, or SQL) to fetch the corresponding user information.
     *
     * @param username the username of the user to look up; must not be null
     * @return a {@link UserDetails} object containing the user's information if the username matches an existing record,
     * or null if no matching user is found
     */
    public @Nullable UserDetails getUserByUsername(String username) {
        return getUserDetails("username", username);
    }

    /**
     * Retrieves user details based on the provided user ID.
     *
     * @param id the unique identifier of the user to fetch details for
     * @return a UserDetails object if the user is found, or null if no user is associated with the given ID
     */
    public @Nullable UserDetails getUserById(String id) {
        return getUserDetails("userID", id);
    }

    /**
     * Retrieves user details by their email.
     *
     * @param email the email address of the user to look up; must not be null or empty
     * @return the UserDetails associated with the provided email, or null if no user is found
     */
    public @Nullable UserDetails getUserByEmail(String email) {
        return getUserDetails("userEmail", email);
    }

    /**
     * Retrieves user details based on the specified element and value.
     * This method determines the authentication storage type (CSV, JSON, or SQL)
     * and fetches the user details accordingly.
     *
     * @param element  the field/column name to query against; must not be null
     * @param specific the value of the element to match; must not be null
     * @return a {@link UserDetails} object containing the user's information if a match is found,
     * or null if no matching record exists
     */
    private @Nullable UserDetails getUserDetails(String element, String specific) {

        try {
            return switch (authStorageType) {
                case CSV -> getUserDetailsFromCsvFile(element, specific);
                case JSON -> getUserDetailsFromJsonFile(element, specific);
                case SQL -> getUserDetailsFromDatabase(element, specific);
            };
        } catch (SQLException e) {
            server.getServerLogger().error(e);
            return null;
        }
    }

    /**
     * Retrieves the user details from a CSV file based on a specific field and its value.
     * This method parses the `data/users.csv` file and checks for a record where the value
     * of the specified element matches the provided specific value.
     *
     * @param element  the name of the CSV column to query; must not be null
     * @param specific the value to match against in the specified column; must not be null
     * @return a {@link UserDetails} object containing the details of the matching user if found,
     * or null if no matching record exists
     */
    private @Nullable UserDetails getUserDetailsFromCsvFile(String element, String specific) {
        try (final Reader reader = new FileReader("data/users.csv")) {
            final Iterable<CSVRecord> userIterable = CSVFormat.DEFAULT.parse(reader);

            for (final CSVRecord record : userIterable) {
                if (record.get(element).equals(specific)) {
                    return UserDetails.builder()
                            .userIP(record.get("userIP"))
                            .userID(record.get("userID"))
                            .userRole(Role.valueOf(record.get("userRole")))
                            .username(record.get("username"))
                            .userEmail(record.get("userEmail"))
                            .phoneNumber(record.get("phoneNumber"))
                            .activeAccount(Boolean.parseBoolean(record.get("isActiveAccount")))
                            .build();
                }
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves user details from a JSON file based on the specified element and value.
     * This method reads a JSON file, iterates through its entries, and returns a {@link UserDetails}
     * object if a match is found between the provided element and the specified value.
     *
     * @param element  the JSON field name to query against; must not be null
     * @param specific the value that the specified element should match; must not be null
     * @return a {@link UserDetails} object containing the matched user's information if found,
     * or null if no matching user is identified
     */
    private @Nullable UserDetails getUserDetailsFromJsonFile(String element, String specific) {
        final ObjectMapper mapper = new ObjectMapper();

        try {
            final List<Map<String, Object>> users = mapper.readValue(new File("data/users.json"), new TypeReference<>() {
            });

            if (users != null) {
                return users.stream()
                        .filter(Objects::nonNull)
                        .filter(user -> user.get(element).equals(specific)).findFirst()
                        .map(user -> UserDetails.builder()
                                .userIP((String) user.get("userIP"))
                                .userID((String) user.get("userID"))
                                .userRole(Role.valueOf((String) user.get("userRole")))
                                .userEmail((String) user.get("userEmail"))
                                .username((String) user.get(("username")))
                                .phoneNumber((String) user.get("phoneNumber"))
                                .activeAccount((boolean) user.get("isActiveAccount"))
                                .build()).orElse(null);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Retrieves user details from the database based on the specified column and value.
     *
     * @param column   the database column to be used for the query (e.g., "username", "email")
     * @param specific the specific value to match in the specified column
     * @return the UserDetails object representing the user, or null if no matching user is found
     * @throws SQLException if a database access error occurs
     */
    private @Nullable UserDetails getUserDetailsFromDatabase(String column, String specific) throws SQLException {
        return userDao.getUser(column, specific);
    }

    public UserDao getUserDao() {
        return userDao;
    }
}
