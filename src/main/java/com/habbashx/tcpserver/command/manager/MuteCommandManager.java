package com.habbashx.tcpserver.command.manager;

import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.handler.UserHandler;
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
 * The MuteCommandManager is responsible for managing and executing operations
 * related to muting and unmuting users, maintaining a persistent record of
 * muted users, and sending appropriate responses to the command senders.
 */
public final class MuteCommandManager {

    private static final String USER_ALREADY_MUTED_MESSAGE = RED+"user already muted !."+RESET;
    private static final String USER_MUTED_SUCCESSFULLY_MESSAGE = LIME_GREEN+"user muted successfully !."+RESET;
    private static final String USER_UNMUTED_SUCCESSFULLY_MESSAGE = LIME_GREEN+"user un muted successfully !."+RESET;
    private static final String USER_IS_NOT_MUTED_MESSAGE = RED+"user is not muted"+RESET;

    /**
     * A collection that maintains the usernames of users who are currently muted in the system.
     *
     * This set is used to store and manage the usernames of muted users and ensures that duplicate
     * entries are avoided. It is primarily utilized in the context of user management commands
     * to determine whether a user is muted, mute a user, or unmute a user.
     *
     * The collection is backed by a {@link HashSet} to provide efficient operations for
     * additions, removals, and lookups.
     *
     * This variable is intended for internal use by the {@code MuteCommandManager} class
     * and is populated from an external data source through the `getMutedUsers()` method.
     */
    private final Set<String> mutedUsers = new HashSet<>();

    private final File file = new File("data/mutedUsers.csv");

    /**
     * A constant representing the header key used to refer to the username field in data parsing operations,
     * particularly when dealing with muted user records. This key is utilized when referencing or extracting
     * the "username" column from structured data sources.
     */
    private static final String HEADER = "username";

    /**
     * Mutes a user by adding their username to the muted users list. This method ensures the user is not
     * already muted before performing the operation and notifies the command sender about the result.
     *
     * @param user the UserHandler instance representing the user to be muted. It must not be null.
     * @param commandSender the CommandSender instance issuing the mute command. It must not be null.
     * @throws RuntimeException if there is an I/O error while writing to the muted users file.
     */
    @SuppressWarnings("deprecation")
    public void muteUser(@NotNull UserHandler user , CommandSender commandSender) {

        try (final FileWriter fileWriter = new FileWriter(file)) {
            final CSVFormat format = CSVFormat.DEFAULT.withHeader(HEADER);
            final CSVPrinter csvPrinter = new CSVPrinter(fileWriter,format);

            final String username = user.getUserDetails().getUsername();
            if (!isUserMuted(username)) {
                csvPrinter.printRecord(username);
                sendMessage(commandSender,USER_MUTED_SUCCESSFULLY_MESSAGE);
            } else {
               sendMessage(commandSender,USER_ALREADY_MUTED_MESSAGE);
            }
            csvPrinter.close(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unmutes a previously muted user by removing them from the list of muted users.
     * If the user is not muted, a message will be sent to the command sender indicating this.
     *
     * @param user the user to be unmuted, provided as a {@link UserHandler} instance; must not be null
     * @param commandSender the sender performing the command (e.g., an administrator) provided as a {@link CommandSender} instance
     */
    @SuppressWarnings("deprecation")
    public void unMuteUser(@NotNull UserHandler user , CommandSender commandSender) {

        final String username = user.getUserDetails().getUsername();
        final Set<String> mutedUsers = getMutedUsers();

        if (isUserMuted(username)) {
            mutedUsers.remove(username);

            try (final Writer writer = new BufferedWriter(new FileWriter(file))) {

                CSVFormat format = CSVFormat.DEFAULT.withHeader(HEADER);
                CSVPrinter csvPrinter = new CSVPrinter(writer,format);

                for (String u : mutedUsers) {
                    csvPrinter.printRecord(u);
                }
                csvPrinter.close(true);
                sendMessage(commandSender,USER_UNMUTED_SUCCESSFULLY_MESSAGE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
        } else {
            sendMessage(commandSender,USER_IS_NOT_MUTED_MESSAGE);
        }
    }

    private void sendMessage(CommandSender commandSender ,String message) {

        if (commandSender instanceof UserHandler userHandler){
            userHandler.sendMessage(message);
        } else {
            System.out.println(message);
        }
    }
    /**
     * Checks if a user is muted based on their username.
     *
     * @param username the username of the user to check
     * @return true if the user is muted, false otherwise
     */
    public boolean isUserMuted(String username) {

        return getMutedUsers().stream().anyMatch(user -> user.equals(username));
    }


    /**
     * Retrieves the set of usernames that are currently muted.
     *
     * This method reads a predefined file``` andjava parses
     /**
     its content * to Retrieves extract a and set store of usernames
     of * users the who muted are currently usernames into muted a.
     set *
     . * If The the method file reads cannot from be a read specified file or, accessed parses, its an content
     in * CSV format exception, is
     * thrown.
     and *
     * collects @ thereturn usernames under a the set " ofusername strings" containing column the into usernames a of set.
     muted *
     users *
     @ *return @ athrows set Runtime ofException strings if containing an the I usernames/O of error muted occurs users while
     reading * the @ filethrows
     Runtime */
    @SuppressWarnings("deprecation")
    public Set<String> getMutedUsers() {

        try (final Reader reader = new FileReader(file)) {

            final Iterable<CSVRecord> userIterable = CSVFormat.DEFAULT.withHeader(HEADER).parse(reader);

            for (final CSVRecord record : userIterable) {
                mutedUsers.add(record.get("username"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return mutedUsers;
    }
}
