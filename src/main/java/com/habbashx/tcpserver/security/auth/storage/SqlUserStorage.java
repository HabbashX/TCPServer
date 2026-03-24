package com.habbashx.tcpserver.security.auth.storage;


import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.user.UserDetails;

import java.sql.SQLException;

public final class SqlUserStorage implements UserStorage {

    private final Server server;

    public SqlUserStorage(Server server) {
        this.server = server;
    }

    @Override
    public boolean isUserExists(String username) throws SQLException {
        return server.getServerDataManager().getUserByUsername(username) != null;
    }

    @Override
    public void registerUser(UserDetails details, String hashedPassword) throws SQLException {
        server.getServerDataManager().getUserDao().insertUser(
                details.getUsername(),
                hashedPassword,
                details.getUserID(),
                details.getUserIP(),
                details.getUserRole().toString(),
                details.getUserEmail(),
                details.getPhoneNumber(),
                details.isActiveAccount()
        );
    }

    @Override
    public UserDetails getUser(String username) throws SQLException {
        return server.getServerDataManager().getUserByUsername(username);
    }

    @Override
    public String getHashedPassword(String username) throws SQLException {
        return server.getServerDataManager().getUserDao().getHashedPassword(username);
    }
}
