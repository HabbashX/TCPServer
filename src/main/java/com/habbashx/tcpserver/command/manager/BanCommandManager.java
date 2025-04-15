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

// Warning do not modify any line of code in this class,
// only if you understand what`s going on right here,
// if you want to add anything do you want features optimizing code feel free to modify it :D .
// see LICENCE arguments.
public final class BanCommandManager {

    private final File file = new File("data/bannedUsers.csv");

    private static final String HEADER = "username";

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
