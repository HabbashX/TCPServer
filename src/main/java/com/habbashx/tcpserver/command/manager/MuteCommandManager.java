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

// Warning do not modify any line of code in this class,
// only if you understand what`s going on right here ok ?,
// if you want to add anything do you want additional features optimizing code feel free to modify it :D .
// see LICENCE arguments.
public final class MuteCommandManager {

    private static final String USER_ALREADY_MUTED_MESSAGE = RED+"user already muted !."+RESET;
    private static final String USER_MUTED_SUCCESSFULLY_MESSAGE = LIME_GREEN+"user muted successfully !."+RESET;
    private static final String USER_UNMUTED_SUCCESSFULLY_MESSAGE = LIME_GREEN+"user un muted successfully !."+RESET;
    private static final String USER_IS_NOT_MUTED_MESSAGE = RED+"user is not muted"+RESET;

    private final Set<String> mutedUsers = new HashSet<>();

    private final File file = new File("data/mutedUsers.csv");

    private static final String HEADER = "username";

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
    public boolean isUserMuted(String username) {

        for (final String user : getMutedUsers()) {
            if (user.equals(username)) {
                return true;
            }
        }
        return false;
    }


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
