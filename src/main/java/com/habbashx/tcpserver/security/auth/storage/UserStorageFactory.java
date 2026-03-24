package com.habbashx.tcpserver.security.auth.storage;

import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.NotNull;

public final class UserStorageFactory {

    private UserStorageFactory() {
    }

    public static @NotNull UserStorage create(@NotNull final String type, final Server server) {
        return switch (type.toUpperCase()) {
            case "CSV" -> new CsvUserStorage();
            case "SQL" -> new SqlUserStorage(server);
            case "JSON" -> new JsonUserStorage();
            default -> throw new IllegalArgumentException("Unknown auth storage type: " + type);
        };
    }
}