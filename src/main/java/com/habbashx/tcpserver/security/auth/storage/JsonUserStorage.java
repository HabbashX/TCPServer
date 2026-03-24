package com.habbashx.tcpserver.security.auth.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.user.UserDetails;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

        Map<String, Object> map = new HashMap<>();
        map.put("userIP", details.getUserIP());
        map.put("userID", details.getUserID());
        map.put("userRole", details.getUserRole());
        map.put("username", details.getUsername());
        map.put("password", hashedPassword);
        map.put("userEmail", details.getUserEmail());
        map.put("phoneNumber", details.getPhoneNumber());
        map.put("isActiveAccount", details.isActiveAccount());

        users.add(map);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(JSON_FILE, users);
    }

    @Override
    public UserDetails getUser(String username) throws IOException {
        if (!JSON_FILE.exists()) return null;
        List<Map<String, Object>> users = MAPPER.readValue(JSON_FILE, new TypeReference<>() {
        });
        for (var user : users) {
            if (user.get("username").equals(username)) {
                return new UserDetails(
                        (String) user.get("userIP"),
                        (String) user.get("userID"),
                        (Role) user.get("userRole"),
                        username,
                        (String) user.get("userEmail"),
                        (String) user.get("phoneNumber"),
                        (boolean) user.get("isActiveAccount")
                );
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
}
