package com.habbashx.tcpserver.security.auth.storage;


import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.user.UserDetails;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public final class SqlUserStorage implements UserStorage {

    private final Server server;

    @Contract(pure = true)
    public SqlUserStorage(Server server) {
        this.server = server;
    }

    @Override
    public boolean isUserExists(String username) {
        return server.getServerDataManager().getUserByUsername(username) != null;
    }

    @Override
    public void registerUser(@NotNull UserDetails details, String hashedPassword) throws SQLException {
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
