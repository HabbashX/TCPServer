package com.habbashx.tcpserver.data.database;

import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.user.UserDetails;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;

/**
 * The UserDao class provides data access functionality for managing user records in a database.
 * It supports operations such as user insertion, updating, retrieval, and deletion.
 * Instances of this class are initialized with a server instance to establish database connections.
 * This class utilizes the `java.sql` package for database interactions and assumes a table named "users"
 * exists with appropriate schema.
 */
public final class UserDao {

    private final Server server;

    /**
     * Represents a database connection used by the UserDao to perform various
     * user-related operations such as inserting, updating, retrieving, and
     * deleting user data. This connection is established and maintained
     * internally by the class.
     * <p>
     * The connection is designed to interact with a database instance to
     * execute SQL queries and is immutable to ensure thread safety and consistency
     * within the UserDao class.
     * <p>
     * This field is initialized when a UserDao object is created and should not
     * be directly exposed or modified externally.
     */
    private final Connection connection;

    @Contract(pure = true)
    public UserDao(@NotNull Server server) {
        this.server = server;
        try {
            final ConnectionPool connectionPool = new ConnectionPool(server);
            connection = connectionPool.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Establishes a connection to the database using the server's configuration settings.
     * <p>
     * This method retrieves the database URL, username, and password from the server's settings
     * and uses them to create a connection via the {@link DriverManager#getConnection(String, String, String)} method.
     * <p>
     * Preconditions:
     * - The database URL must not be null.
     * <p>
     * Exceptions:
     * - Throws {@link SQLException} if a database access error occurs.
     *
     * @return a {@link Connection} object representing the established database connection
     * @throws SQLException if a database access error occurs
     */
    @Contract(pure = true)
    private Connection getConnection() {
        return connection;
    }

    /**
     * Inserts a new user into the database with the specified details.
     *
     * @param username        the username of the user
     * @param password        the plaintext password of the user
     * @param userID          the unique identifier for the user
     * @param userIP          the IP address associated with the user
     * @param userRole        the role assigned to the user (e.g., admin, user)
     * @param userEmail       the email address of the user
     * @param phoneNumber     the phone number of the user
     * @param isActiveAccount the status of the user account (true if active, false otherwise)
     * @throws SQLException if a database access error occurs or the SQL statement fails
     */
    public void insertUser(String username,
                           String password,
                           String userID,
                           String userIP,
                           String userRole,
                           String userEmail,
                           String phoneNumber,
                           boolean isActiveAccount) throws SQLException {

        @Language("SQL") final String sql = "INSERT INTO users (userID, userIP, userRole, username, password, userEmail, phoneNumber, isActiveAccount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userID);
            stmt.setString(2, userIP);
            stmt.setString(3, userRole);
            stmt.setString(4, username);
            stmt.setString(5, password);
            stmt.setString(6, userEmail);
            stmt.setString(7, phoneNumber);
            stmt.setBoolean(8, isActiveAccount);
            stmt.executeUpdate();
        }
    }

    /**
     * Updates a specific user's information in the database by modifying a specified column's value.
     * The update is performed based on the column to match and the specified value.
     *
     * @param column       the column name to be used for identifying the specific user
     * @param targetColumn the column name that needs to be updated
     * @param specific     the specific value in the column used to identify the user
     * @param newValue     the new value to be assigned to the target column
     * @throws SQLException if a database access error occurs or the SQL statement fails
     */
    public void updateUser(String column, String targetColumn, String specific, String newValue) throws SQLException {

        @Language("SQL") final String sql = "UPDATE users SET " + targetColumn + " = ? WHERE " + column + " = ?";
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newValue);
            stmt.setString(2, specific);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a user from the database based on the specified column and value.
     *
     * @param element  the column name that serves as the criteria for deletion
     * @param specific the value corresponding to the specified column to identify the user for deletion
     * @throws SQLException if a database access error occurs or the SQL execution fails
     */
    public void deleteUser(String element, String specific) throws SQLException {
        final String sql = "DELETE FROM users WHERE " + element + " = ?";
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, specific);
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves user details from the database based on a specified column and value.
     *
     * @param column   the database column to be used for filtering the query
     * @param specific the specific value to match in the specified column
     * @return a UserDetails object containing the user's details if a match is found, or null if no match is found
     * @throws SQLException if a database access error occurs
     */
    public @Nullable UserDetails getUser(String column, String specific) throws SQLException {

        long start = System.currentTimeMillis();
        @Language("SQL") final String sql = "SELECT * FROM users WHERE " + column + " = ?";
        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, specific);
            final ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return UserDetails.builder()
                        .userID(rs.getString("userID"))
                        .userIP(rs.getString("userIP"))
                        .userRole(Role.valueOf(rs.getString("userRole")))
                        .username(rs.getString("username"))
                        .userEmail(rs.getString("userEmail"))
                        .phoneNumber(rs.getString("phoneNumber"))
                        .activeAccount(Boolean.parseBoolean(rs.getString("isActiveAccount")))
                        .build();
            }
        }
        long end = System.currentTimeMillis();
        server.getServerLogger().info("query took: " + (end - start) + "ms");
        return null;
    }

    /**
     * Retrieves the hashed password of a user from the database based on the provided username.
     * If the username exists in the database, the hashed password is returned; otherwise, null is returned.
     *
     * @param username the username of the user whose hashed password is to be retrieved; must not be null
     * @return the hashed password associated with the username, or null if the username does not exist in the database
     * @throws SQLException if a database access error occurs
     */
    public @Nullable String getHashedPassword(String username) throws SQLException {

        @Language("SQL") final String sql = "SELECT * FROM users WHERE username = ?";

        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            final ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
        }
        return null;
    }

    /**
     * Closes the database connection if it is open.
     * <p>
     * This method checks whether the database connection is still open and, if so,
     * closes it to release the associated resources. It also logs a message indicating
     * that the connection has been successfully closed. If an SQL exception occurs during
     * the process, the error is logged.
     */
    public void closeConnection() {
        try {
            if (!connection.isClosed()) {
                connection.close();
                server.getServerLogger().info("database connection is closed");
            }
        } catch (SQLException e) {
            server.getServerLogger().error(e);
        }
    }
}
