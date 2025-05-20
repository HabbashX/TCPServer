package com.habbashx.tcpserver.command.manager;

import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.user.UserDetails;
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
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import static com.habbashx.tcpserver.logger.ConsoleColor.LIME_GREEN;
import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * Manages the banning and unbanning of users using a file-based storage system.
 * This class is responsible for enforcing bans, maintaining a record of banned users,
 * and interacting with command senders to notify them of outcomes.
 * <p>
 * The banned user data is stored in a CSV file named "bannedUsers.csv".
 * The file contains a single column with the header "username".
 *
 * Key operations include:
 * - Banning a user by adding their username to the file.
 * - Unbanning a user by removing their username from the file.
 * - Querying the list of all banned users.
 * - Checking if a specific user is banned.
 * - Sending notifications to command senders and affected users about the results of commands.
 */
public final class BanCommandManager {

    private final File file = new File("data/bannedUsers.csv");

    private static final String HEADER = "username";

    /**
     * Bans a specified user from the server by adding their username to the ban list.
     * The method checks if the user is already banned before proceeding to ban them.
     * If the user is successfully banned, a confirmation message is sent to both the
     * command sender and the user being banned. The banned user is then shut down.
     * If the user is already banned, an appropriate message is sent to the command sender.
     *
     * @param user the target user who is to be banned
     * @param commandSender the person or system issuing the ban command
     * @throws RuntimeException if an I/O error occurs during file operations
     */
    @SuppressWarnings("deprecation")
    public synchronized void banUser(@NotNull UserHandler user , @NotNull CommandSender commandSender) {

        try (final Writer writer = new FileWriter(file)) {

             CSVFormat format = CSVFormat.DEFAULT.withHeader(HEADER);
             CSVPrinter printer = new CSVPrinter(writer,format);

            final String username = user.getUserDetails().getUsername();

            if (!isUserBanned(username)) {
                printer.printRecord(username);
                sendMessage(commandSender,LIME_GREEN+"user have been banned successfully"+RESET);
                user.sendMessage(RED+"you got banned"+RESET);
                user.shutdown();
            } else {
                sendMessage(commandSender,RED + "user already banned" + RESET);
            }

            printer.flush();
            printer.close();

        } catch (IOException e) {
            throw new RuntimeException();
        }

    }

    /**
     * Unbans a user by removing their username from the banned users list and updating the related data source.
     * Sends a confirmation message to the command sender upon successful unbanning.
     *
     * @param userDetails The details of the user to be unbanned, must not be null.
     * @param commandSender The sender of the command requesting the unban operation.
     */
    @SuppressWarnings("deprecation")
    public synchronized void unBanUser(@NotNull UserDetails userDetails, CommandSender commandSender) {

        final Set<String> bannedUsers = getBannedUsers();

        try (final Writer writer = new BufferedWriter(new FileWriter(file))) {

            CSVFormat format = CSVFormat.DEFAULT.withHeader(HEADER);
            CSVPrinter printer = new CSVPrinter(writer,format);

            final String username = userDetails.getUsername();

            for (String user : bannedUsers) {
                if (user.equals(username)) {
                    bannedUsers.remove(username);
                    sendMessage(commandSender, LIME_GREEN+"user un banned successfully."+RESET);
                    for (String u : bannedUsers) {
                        if (u != null) {
                            if (!u.equals(username)) {
                                printer.printRecord(u);
                            }
                        }
                    }
                }
            }
            printer.close(true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(CommandSender sender , String message) {
        if (sender instanceof UserHandler userHandler) {
            userHandler.sendMessage(message);
        } else {
            System.out.println(message);
        }
    }
    /**
     * Checks if a user is banned by verifying if their username exists in the banned users list.
     *
     * @param username the username of the user to check; must not be null
     * @return true if the username is found in the list of banned users, false otherwise
     */
    public boolean isUserBanned(String username) {

        for (String user : getBannedUsers()) {
            if (user != null) {
                if (username.equals(user)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves the list of banned users from the data source.
     * The method reads a file containing banned user information
     * and extracts the usernames into a``` setjava.

     /**
     * Retrieves the list *
     * @return a set of usernames representing of the users who banned are currently users banned
    .
     * @ * Thisthrows method RuntimeException if reads an from I/O error occurs a while accessing file the file
     */
    @SuppressWarnings("deprecation")
    public @NotNull Set<String> getBannedUsers() {

        final Set<String> bannedUsers = new HashSet<>();

        try (final Reader reader = new FileReader(file)){

            final Iterable<CSVRecord> userIterable = CSVFormat.DEFAULT.withHeader(HEADER).parse(reader);

            for (final CSVRecord record : userIterable) {
                if (record != null) {
                    bannedUsers.add(record.get("username"));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bannedUsers;
    }
}
