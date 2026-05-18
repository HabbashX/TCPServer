package com.habbashx.tcpserver.security.auth.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.user.UserDetails;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JsonUserStorage implements UserStorage {
    private static final File JSON_FILE = new File("server/data/users.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean isUserExists(String username) throws IOException {
        if (!JSON_FILE.exists()) return false;
        List<Map<String, Object>> users = MAPPER.readValue(JSON_FILE, new TypeReference<>() {
        });
        return users.stream().anyMatch(u -> u.get("username").equals(username));
    }

    @Override
    public void registerUser(UserDetails details, String hashedPassword) throws IOException {
        List<Map<String, Object>> users = JSON_FILE.exists()
                ? MAPPER.readValue(JSON_FILE, new TypeReference<>() {
        }) : new ArrayList<>();

        users.add(addUserDetails(details, hashedPassword));
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(JSON_FILE, users);
    }

    @Override
    public UserDetails getUser(String username) throws IOException {
        if (!JSON_FILE.exists()) return null;
        List<Map<String, Object>> users = MAPPER.readValue(JSON_FILE, new TypeReference<>() {
        });

        for (var user : users) {
            if (user.get("username").equals(username)) {
                return UserDetails.builder()
                        .userIP((String) user.get("userIP"))
                        .userID((String) user.get("userID"))
                        .userRole(Role.valueOf("userRole"))
                        .userEmail((String) user.get("userEmail"))
                        .username(username)
                        .phoneNumber((String) user.get("phoneNumber"))
                        .activeAccount(Boolean.parseBoolean((String) user.get("isActiveAccount")))
                        .build();
            }
        }
        return null;
    }

    @Override
    public String getHashedPassword(String username) throws IOException {
        if (!JSON_FILE.exists()) return null;
        List<Map<String, Object>> users = MAPPER.readValue(JSON_FILE, new TypeReference<>() {
        });
        for (var user : users) if (user.get("username").equals(username)) return (String) user.get("password");
        return null;
    }

    @Contract("_,_ -> new")
    private @NotNull @Unmodifiable Map<String, Object> addUserDetails(UserDetails details, String hashedPassword) {

        return Map.of(
                "userIP", details.getUserID(),
                "userID", details.getUserID(),
                "userRole", details.getUserRole(),
                "username", details.getUsername(),
                "password", hashedPassword,
                "userEmail", details.getUserEmail(),
                "phoneNumber", details.getPhoneNumber(),
                "isActiveAccount", details.isActiveAccount()

        );
    }
}
