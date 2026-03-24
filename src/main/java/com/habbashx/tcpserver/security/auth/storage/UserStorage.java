package com.habbashx.tcpserver.security.auth.storage;

import com.habbashx.tcpserver.user.UserDetails;

import java.io.IOException;
import java.sql.SQLException;

public interface UserStorage {
    
    boolean isUserExists(String username) throws IOException, SQLException;

    void registerUser(UserDetails details, String hashedPassword) throws IOException, SQLException;

    UserDetails getUser(String username) throws IOException, SQLException;

    String getHashedPassword(String username) throws IOException, SQLException;
}