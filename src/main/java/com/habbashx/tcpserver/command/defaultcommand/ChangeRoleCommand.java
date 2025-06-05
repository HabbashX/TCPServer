package com.habbashx.tcpserver.command.defaultcommand;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habbashx.tcpserver.annotation.MayBeEmpty;
import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.security.auth.storage.AuthStorageType;
import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.socket.Server;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.habbashx.tcpserver.logger.ConsoleColor.LIME_GREEN;
import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;
import static com.habbashx.tcpserver.security.Permission.CHANGE_RANK_PERMISSION;

/**
 * The ChangeRoleCommand class handles the execution of commands to change the role of a user.
 * It provides functionality to interact with the server's user and role management system.
 * This command requires the proper permissions and has a cooldown period to prevent abuse.
 *
 * Command Details:
 * - Name: setrole
 * - Aliases: changerole
 * - Permission: CHANGE_RANK_PERMISSION
 * - Cooldown: 30 seconds
 *
 * Features:
 * - Allows authorized users to modify the role of other users.
 * - Supports multiple storage backends (CSV, JSON, and SQL) to persist role changes.
 * - Provides feedback to the command sender about the success or failure of the operation.
 * - Prevents users from changing their own roles for security purposes.
 *
 * Restrictions:
 * - The specified role must exist within the pre-defined set of roles.
 * - Command execution requires specific permissions.
 *
 * Storage Backend Support:
 * - CSV: Reads and updates the role for users in a CSV file.
 * - JSON: Reads and updates the role for users in a JSON file.
 * - SQL: Updates the role directly in the database.
 *
 * Core Components:
 * - Server: Manages server state and user data.
 * - CommandContext: Encapsulates the context in which the command is executed.
 * - UserHandler: Handles the interaction with individual users.
 *
 * Constants:
 * - DEFAULT_FORMAT: Defines the CSV format for parsing and writing user data.
 * - COMMAND_USAGE_MESSAGE: Message prompting the proper usage of the command.
 * - AVAILABLE_ROLES_MESSAGE: Message displaying all available roles.
 * - AVAILABLE_ROLES: List of valid roles.
 * - RANK_CHANGED_MESSAGE: Success message for role changes.
 * - CANNOT_CHANGE_ROLE_MESSAGE: Error message for attempts to change one's own role.
 *
 * Core Methods:
 * - execute(CommandContext commandContext): Main method to handle the command execution.
 * - changeRankInUsersFile(String username, Role role): Updates the user's role based on the storage backend.
 * - isRoleExists(String role): Verifies if the provided role exists in the system.
 * - sendMessage(CommandSender commandSender, String message): Sends feedback messages to the command sender.
 *
 * Helper Methods:
 * - readAllUsersFromCsvFile(): Reads all user data from a CSV file.
 * - rewriteAllUsersToCsvFile(List<Map<String, Object>> users): Rewrites user data to a CSV file.
 * - changeRankFromCsvFile(String username, Role role): Changes the user's role in a CSV file.
 * - changeRankFromJsonFile(String username, Role role): Changes the user's role in a JSON file.
 * - changeRankFromDatabase(String username, String newRole): Changes the user's role in an SQL database.
 */
@Command(
        name = "setrole",
        aliases = "changerole",
        permission =  CHANGE_RANK_PERMISSION,
        cooldownTimeUnit = TimeUnit.SECONDS,
        cooldownTime = 30L,
        executionLog = true
)
@SuppressWarnings("deprecation")
public final class ChangeRoleCommand extends CommandExecutor {

    /**
     * The default CSV format used within the {@code ChangeRoleCommand} class for handling
     * user-related data in CSV files. This format includes headers corresponding to user
     * attributes such as "userIP", "userID", "userRole", "username", "password", "userEmail",
     * "phoneNumber", and "isActiveAccount".
     *
     * The {@code DEFAULT_FORMAT} is a pre-configured instance of {@code CSVFormat}, designed
     * to ensure consistency in CSV file reading and writing operations performed by various
     * methods in the class.
     */
    private static final CSVFormat DEFAULT_FORMAT = CSVFormat.DEFAULT.withHeader("userIP","userID","userRole","username","password","userEmail","phoneNumber","isActiveAccount");

    private static final String COMMAND_USAGE_MESSAGE = "usage: /setrole <username> <rank>";
    private static final String AVAILABLE_ROLES_MESSAGE = "available roles DEFAULT , MODERATOR , OPERATOR , ADMINISTRATOR , SUPER_ADMINISTRATOR";
    private static final String[] AVAILABLE_ROLES = {"DEFAULT","MODERATOR","OPERATOR","ADMINISTRATOR","SUPER_ADMINISTRATOR"};
    private static final String RANK_CHANGED_MESSAGE = LIME_GREEN+"role have been changed successfully."+RESET;
    private static final String CANNOT_CHANGE_ROLE_MESSAGE = RED+"you cannot change your role"+RESET;
    private final Server server;

    public ChangeRoleCommand(Server server) {
        this.server = server;
    }

    /**
     * Executes the role change command. This method is responsible for processing the command
     * context provided by the sender to change the role of a specified user to the role provided
     * in the command arguments. It performs necessary validations such as ensuring the role exists,
     * the sender does not try to modify their own role, and that the target user is online.
     *
     * @param commandContext The context of the command execution, containing the sender's details,
     *                       command arguments, and other execution-related data. The arguments must
     *                       include at least two elements: the target username and the desired role.
     *                       If insufficient arguments are provided, an error message is sent to the sender.
     */
    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().size() < 2) {
            sendMessage(commandContext.getSender(),COMMAND_USAGE_MESSAGE);
            return;
        }

        @MayBeEmpty
        String targetUsername = commandContext.getArgs().get(0);
        @MayBeEmpty
        String specifiedRole = commandContext.getArgs().get(1).toUpperCase();

        if (isRoleExists(specifiedRole)) {

            if (commandContext.getSender() instanceof UserHandler userHandler) {
                String senderUsername = userHandler.getUserDetails().getUsername();

                if (senderUsername.equals(targetUsername)) {
                    sendMessage(commandContext.getSender(),CANNOT_CHANGE_ROLE_MESSAGE);
                    return;
                }
            }
            Role role = Role.valueOf(specifiedRole);
            Objects.requireNonNull(server.getServerDataManager().getOnlineUserByUsername(targetUsername)).getUserDetails().setUserRole(role);
            changeRankInUsersFile(targetUsername,role);
            sendMessage(commandContext.getSender(),RANK_CHANGED_MESSAGE);
        } else {
            sendMessage(commandContext.getSender(),AVAILABLE_ROLES_MESSAGE);
        }
    }

    /**
     * Updates the rank of a user in the appropriate storage file or database based on the server's authentication storage type.
     *
     * @param username the username of the user whose rank is to be changed
     * @param role the new role to be assigned to the user
     */
    private void changeRankInUsersFile(String username,Role role) {

        String authType = server.getServerSettings().getAuthStorageType().toUpperCase();

        AuthStorageType authStorageType = AuthStorageType.valueOf(authType);

        switch (authStorageType) {
            case CSV -> changeRankFromCsvFile(username,role);
            case JSON -> changeRankFromJsonFile(username,role);
            case SQL -> changeRankFromDatabase(username,role.toString());
        }
    }

    /**
     * Reads and parses all users from a CSV file and returns a list of users as maps.
     * Each user is represented as a map containing key-value pairs corresponding to the columns in the CSV file.
     * The CSV file should have columns such as userIP, userID, userRole, username, password, userEmail, phoneNumber, and isActiveAccount.
     *
     * @return A list of maps where each map represents a user and contains their data extracted from the CSV file.
     *         If the file cannot be read, an empty list is returned.
     */
    private @NotNull List<Map<String,Object>> readAllUsersFromCsvFile() {

        List<Map<String,Object>> userList = new ArrayList<>();

        try (Reader reader = new FileReader("data/users.csv")) {

            Iterable<CSVRecord> users = DEFAULT_FORMAT.parse(reader);

            for (CSVRecord record : users) {
                Map<String,Object> map = new HashMap<>();
                map.put("userIP",record.get("userIP"));
                map.put("userID",record.get("userID"));
                map.put("userRole",record.get("userRole"));
                map.put("username",record.get("username"));
                map.put("password",record.get("password"));
                map.put("userEmail",record.get("userEmail"));
                map.put("phoneNumber",record.get("phoneNumber"));
                map.put("isActiveAccount",record.get("isActiveAccount"));
                userList.add(map);
            }
        } catch (IOException e) {
            server.getServerLogger().error(e);
        }
        return userList;
    }

    /**
     * Overwrites an existing CSV file or creates a new file to store a list of users.
     * Each user's details are written as a record in the CSV file.
     * The file is written to "data/users.csv" and includes user properties such as IP, ID, role, username, password, email, phone number, and activity status.
     *
     * @param users A non-null list of maps where each map represents a user's details.
     *              Each map should contain the following keys: "userIP", "userID", "userRole", "username",
     *              "password", "userEmail", "phoneNumber", "isActiveAccount".
     */
    private void rewriteAllUsersToCsvFile(@NotNull List<Map<String,Object>> users) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/users.csv"))) {

            CSVPrinter printer = new CSVPrinter(writer,DEFAULT_FORMAT);

            for (Map<String,Object> user : users) {

                printer.printRecord(
                        user.get("userIP"),
                        user.get("userID"),
                        user.get("userRole"),
                        user.get("username"),
                        user.get("password"),
                        user.get("userEmail"),
                        user.get("phoneNumber"),
                        user.get("isActiveAccount")
                );
            }
            printer.close(true);
        } catch (IOException e) {
            server.getServerLogger().error(e);
        }
    }
    /**
     * Updates the rank (role) of a specific user in a CSV file. It searches for a user by their username
     * and changes their role to the provided value, rewriting the updated user list back to the CSV file.
     *
     * @param username The username of the user whose role is to be updated.
     * @param role The new role to be assigned to the specified user.
     */
    private void changeRankFromCsvFile(String username,Role role) {

        List<Map<String,Object>> users = readAllUsersFromCsvFile();

        for (Map<String, Object> user : users) {

            if (user.get("username").equals(username)) {
                user.replace("userRole",role.toString());
                rewriteAllUsersToCsvFile(users);
                return;
            }
        }
    }
    /**
     * Updates the role of a specified user in the users JSON file.
     *
     * @param username the username of the user whose role needs to be updated
     * @param role the new role to be assigned to the user
     */
    private void changeRankFromJsonFile(String username,Role role) {

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final File usersDataFile = new File("data/users.json");
            List<Map<String, Object>> users = mapper.readValue(usersDataFile, new TypeReference<>() {
            });

            users.stream().filter(user -> user.get("username").equals(username)).forEach(user -> user.replace("userRole", role.toString()));
            mapper.writerWithDefaultPrettyPrinter().writeValue(usersDataFile, users);
        } catch (IOException e) {
            server.getServerLogger().error(e);
        }


    }

    /**
     * Updates the role of a user in the database based on the provided username and new role.
     *
     * @param username the username of the user whose role needs to be updated
     * @param newRole  the new role to assign to the user
     */
    private void changeRankFromDatabase(String username , String newRole) {
        try {
            server.getServerDataManager().getUserDao().updateUser("username", "userRole", username,newRole);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a message to the specified command sender. If the command sender is an instance
     * of UserHandler, the message will be sent using the UserHandler's sendMessage method.
     * Otherwise, the message will be printed to the system output.
     *
     * @param commandSender the sender of the command, which could be an instance of
     *                      UserHandler or another type
     * @param message       the message to be sent or printed
     */
    private void sendMessage(CommandSender commandSender ,String message) {

        if (commandSender instanceof UserHandler userHandler) {
            userHandler.sendMessage(message);
        } else {
            System.out.println(message);
        }
    }

    /**
     * Checks if the specified role exists in the list of available roles.
     *
     * @param role The role to check for existence. This should be a non-null string
     *             representing the role name.
     * @return true if the role exists in the available roles list, false otherwise.
     */
    private boolean isRoleExists(String role) {

        for (String r : AVAILABLE_ROLES) {
            if (r.equals(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CooldownManager getCooldownManager() {
        return super.getCooldownManager();
    }
}
