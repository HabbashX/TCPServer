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

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;
import static com.habbashx.tcpserver.util.UserUtil.isValidEmail;
import static com.habbashx.tcpserver.util.UserUtil.isValidPhoneNumber;
import static com.habbashx.tcpserver.util.UserUtil.isValidUsername;

@SuppressWarnings("deprecation")
public final class DefaultAuthentication extends Authentication {

    private final Server server;

    private static final String[] HEADER = {"userIP","userID","userRole","username","password","userEmail","phoneNumber","isActiveAccount"};

    private final AuthStorageType authStorageType;
    private final File file;
    private final CSVFormat format = CSVFormat.DEFAULT.withHeader(HEADER);
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String USER_ALREADY_CONNECTED_MESSAGE = RED+"user already connected to server"+RESET;

    public DefaultAuthentication(Server server) {
        this.server = server;
        String authType = this.server.getServerSettings().getAuthStorageType().toUpperCase();
        String fileType= authType.toLowerCase();
        authStorageType = AuthStorageType.valueOf(authType);
        file = new File("data/users.%s".formatted(fileType));
    }

    @Override
    public synchronized void register(@NotNull String username ,@NotNull String password ,String email, String phoneNumber ,@NotNull UserHandler userHandler) {

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
                userHandler.sendMessage(RED+"invalid email"+RESET);
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
    }



    @Override
    public synchronized void login(@NotNull String username, @NotNull String password ,@NotNull UserHandler userHandler) {

        switch (authStorageType) {
            case CSV -> csvLogin(username,password,userHandler);
            case JSON ->  jsonLogin(username,password,userHandler);
            case SQL -> sqlLogin(username,password,userHandler);
        }
    }

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

    private void csvLogin(String username,String password ,UserHandler userHandler) {

        if (!isUserConnected(username)) {
            try (final Reader reader = new FileReader("data/users.csv")) {
                Iterable<CSVRecord> users = CSVFormat.DEFAULT.parse(reader);

                for (CSVRecord record : users) {
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

    private void jsonLogin(String username ,String password ,UserHandler userHandler) {

        try {
            if (!isUserConnected(username)) {
                final List<Map<String, Object>> users = mapper.readValue(file, new TypeReference<>() {
                });

                for (Map<String, Object> user : users) {

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

    private boolean isUserExists(String username) {
        final UserDetails userDetails = server.getServerDataManager().getUserByUsername(username);
        return userDetails != null;
    }

    private boolean isUserConnected(String username) {

        for (final UserHandler user : server.getConnections()) {
            final String name = user.getUserDetails().getUsername();
            if (name != null) {
                if (name.equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Server getServer() {
        return server;
    }

}