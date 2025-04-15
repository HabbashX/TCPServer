package com.habbashx.tcpserver.command.defaultcommand;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habbashx.tcpserver.annotation.PossibleEmpty;
import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.security.AuthStorageType;
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

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getArgs().size() < 2) {
            sendMessage(commandContext.getSender(),COMMAND_USAGE_MESSAGE);
            return;
        }

        @PossibleEmpty
        String targetUsername = commandContext.getArgs().get(0);
        @PossibleEmpty
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

    private void changeRankInUsersFile(String username,Role role) {

        String authType = server.getServerSettings().getAuthStorageType().toUpperCase();

        AuthStorageType authStorageType = AuthStorageType.valueOf(authType);

        switch (authStorageType) {
            case CSV -> changeRankFromCsvFile(username,role);
            case JSON -> changeRankFromJsonFile(username,role);
            case SQL -> changeRankFromDatabase(username,role.toString());
        }
    }

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
            server.getServerLogger().error(e.getMessage());
        }
        return userList;
    }

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
            server.getServerLogger().error(e.getMessage());
        }
    }
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
    private void changeRankFromJsonFile(String username,Role role) {

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final File usersDataFile = new File("data/users.json");
            List<Map<String, Object>> users = mapper.readValue(usersDataFile, new TypeReference<>() {
            });

            for (Map<String,Object> user : users) {
                if (user.get("username").equals(username)) {
                    user.replace("userRole",role.toString());
                }
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(usersDataFile, users);
        } catch (IOException e) {
            server.getServerLogger().error(e.getMessage());
        }


    }

    private void changeRankFromDatabase(String username , String newRole) {
        try {
            server.getServerDataManager().getUserDao().updateUser("username", "userRole", username,newRole);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(CommandSender commandSender ,String message) {

        if (commandSender instanceof UserHandler userHandler) {
            userHandler.sendMessage(message);
        } else {
            System.out.println(message);
        }
    }

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
