package com.habbashx.tcpserver.security.auth.storage;

import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.user.UserDetails;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;

public final class CsvUserStorage implements UserStorage {
    private static final File CSV_FILE = new File("server/data/users.csv");
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.withHeader(
            "userIP", "userID", "userRole", "username", "password", "userEmail", "phoneNumber", "isActiveAccount"
    );

    @Override
    public boolean isUserExists(String username) throws IOException {
        if (!CSV_FILE.exists()) return false;
        try (Reader reader = new FileReader(CSV_FILE)) {
            return FORMAT.parse(reader).stream().anyMatch(r -> r.get("username").equals(username));
        }
    }

    @Override
    public void registerUser(UserDetails details, String hashedPassword) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(CSV_FILE, true), FORMAT)) {
            printer.printRecord(
                    details.getUserIP(),
                    details.getUserID(),
                    details.getUserRole(),
                    details.getUsername(),
                    hashedPassword,
                    details.getUserEmail(),
                    details.getPhoneNumber(),
                    details.isActiveAccount()
            );
        }
    }

    @Override
    public UserDetails getUser(String username) throws IOException {
        if (!CSV_FILE.exists()) return null;
        try (Reader reader = new FileReader(CSV_FILE)) {
            for (var record : FORMAT.parse(reader)) {
                if (record.get("username").equals(username)) {
                    return UserDetails.builder()
                            .userIP(record.get("userIP"))
                            .userID(record.get("userID"))
                            .userRole(Role.valueOf(record.get("userRole")))
                            .username(username)
                            .userEmail(record.get("userEmail"))
                            .phoneNumber(record.get("phoneNumber"))
                            .activeAccount(Boolean.parseBoolean(record.get("isActiveAccount")))
                            .build();
                }
            }
        }
        return null;
    }

    @Override
    public String getHashedPassword(String username) throws IOException {
        if (!CSV_FILE.exists()) return null;
        try (Reader reader = new FileReader(CSV_FILE)) {
            for (var record : FORMAT.parse(reader)) {
                if (record.get("username").equals(username)) return record.get("password");
            }
        }
        return null;
    }
}