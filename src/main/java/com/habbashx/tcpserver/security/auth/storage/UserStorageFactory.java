package com.habbashx.tcpserver.security.auth.storage;

import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Factory responsible for creating {@link UserStorage} implementations
 * based on a specified storage type.
 * <p>
 * This class follows the Factory Pattern and provides a centralized
 * registry of supported storage backends such as CSV, SQL, and JSON.
 * <p>
 * It allows decoupling storage creation logic from the rest of the system,
 * making it easier to extend or replace storage implementations.
 */
public final class UserStorageFactory {

    /**
     * Registry of available user storage implementations.
     * <p>
     * Key: storage type name (uppercase string)
     * Value: function that creates a {@link UserStorage} instance
     * from a {@link Server} context.
     * <p>
     * This map defines all supported storage backends.
     */
    private static final Map<String, Function<Server, UserStorage>> REGISTRY = Map.of(
            "CSV", s -> new CsvUserStorage(),
            "SQL", SqlUserStorage::new,
            "JSON", s -> new JsonUserStorage()
    );

    /**
     * Private constructor to prevent instantiation.
     * <p>
     * This class is intended to be used in a static context only.
     */
    private UserStorageFactory() {
    }

    /**
     * Creates a {@link UserStorage} implementation based on the given type.
     * <p>
     * The type is case-insensitive and matched against the internal registry.
     * Supported types include:
     * <ul>
     *     <li>CSV</li>
     *     <li>SQL</li>
     *     <li>JSON</li>
     * </ul>
     *
     * @param type   the storage type identifier (case-insensitive)
     * @param server the server context required by some storage implementations
     * @return a concrete {@link UserStorage} instance
     * @throws IllegalArgumentException if the storage type is not supported
     */
    public static @NotNull UserStorage create(@NotNull final String type,
                                              final Server server) {

        String key = type.toUpperCase(Locale.ROOT);

        Function<Server, UserStorage> creator = REGISTRY.get(key);

        if (creator == null) {
            throw new IllegalArgumentException("Unknown storage type: " + type);
        }

        return creator.apply(server);
    }
}
