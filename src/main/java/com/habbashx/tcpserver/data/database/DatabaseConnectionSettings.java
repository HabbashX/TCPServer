package com.habbashx.tcpserver.data.database;

import com.habbashx.annotation.DecryptWith;
import com.habbashx.annotation.InjectProperty;
import com.habbashx.injector.PropertyInjector;
import com.habbashx.tcpserver.security.crypto.DatabasePasswordDecryptor;
import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.util.ServerUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * <p>Centralized configuration provider for database and connection pool settings.</p>
 * * <p>This class handles the automated injection of properties from the server's
 * configuration file and manages the sensitive decryption of database credentials.
 * It primarily configures <b>HikariCP</b> for high-performance connection pooling.</p>
 */
public final class DatabaseConnectionSettings {

    /**
     * The JDBC connection URL (e.g., jdbc:mysql://localhost:3306/db)
     */
    @InjectProperty("server.database.url")
    private String url;

    /**
     * The database administrative username
     */
    @InjectProperty("server.database.username")
    private String username;

    /**
     * The encrypted database password.
     * <p>Processed via {@link DatabasePasswordDecryptor} during injection.</p>
     */
    @InjectProperty("server.database.password")
    @DecryptWith(DatabasePasswordDecryptor.class)
    private String password;

    /**
     * Maximum number of connections allowed in the pool
     */
    @InjectProperty("server.hikari.datasource.pool-size")
    private int connectionPoolSize;

    /**
     * Minimum number of idle connections HikariCP tries to maintain
     */
    @InjectProperty("server.hikari.datasource.minimum-idle")
    private int minimumIdle;

    /**
     * Whether to enable client-side caching of Prepared Statements
     */
    @InjectProperty("server.hikari.config.cache-prep-stmt")
    private boolean cachePrepStmt;

    /**
     * The number of prepared statements that the driver will cache per connection
     */
    @InjectProperty("server.hikari.config.prep-stmt-cache-size")
    private int prepStmtCacheSize;

    /**
     * The maximum length of a prepared SQL statement that will be cached
     */
    @InjectProperty("server.hikari.config.prep-stmt-cache-sql-limit")
    private int prepStmtCacheSqlLimit;

    /**
     * Maximum time (ms) to wait for a connection from the pool before timing out
     */
    @InjectProperty("server.hikari.config.connection-timeout")
    private long connectionTimeout;

    /**
     * Time (ms) a connection can be out of the pool before a leak is suspected
     */
    @InjectProperty("server.hikari.config.leak-detection-thres-hold")
    private long leakDetectionThreshold;

    /**
     * If true, uses server-side prepared statements instead of client-side emulation
     */
    @InjectProperty("server.hikari.config.use-server-prep-stmt")
    private boolean useServerPrepStmts;

    /**
     * If true, the driver will use internal state to avoid unnecessary queries
     */
    @InjectProperty("server.hikari.config.use-local-session-state")
    private boolean useLocalSessionState;

    /**
     * If true, combines multiple statements into a single batch to reduce round-trips
     */
    @InjectProperty("server.hikari.config.rewrite-batched-statement")
    private boolean rewriteBatchedStatements;

    /**
     * Constructs settings and triggers immediate property injection.
     * * @param server The server instance providing the logger for initialization status.
     *
     * @throws RuntimeException If the configuration file at {@code SERVER_SETTINGS_PATH}
     *                          is inaccessible or injection fails.
     */
    public DatabaseConnectionSettings(@NotNull final Server server) {
        final PropertyInjector propertyInjector = new PropertyInjector(new File(ServerUtils.SERVER_SETTINGS_PATH));
        propertyInjector.inject(this);
        server.getServerLogger().info("initialized server database connection settings");
    }

    /**
     * @return The JDBC connection string.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return The database user authorized for connection.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return The decrypted plain-text password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return The maximum size of the connection pool.
     */
    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    /**
     * @return The minimum number of idle connections to maintain.
     */
    public int getMinimumIdle() {
        return minimumIdle;
    }

    /**
     * @return The size of the prepared statement cache.
     */
    public int getPrepStmtCacheSize() {
        return prepStmtCacheSize;
    }

    /**
     * @return The character limit for SQL statements allowed in the cache.
     */
    public int getPrepStmtCacheSqlLimit() {
        return prepStmtCacheSqlLimit;
    }

    /**
     * @return {@code true} if statement caching is enabled.
     */
    public boolean isCachePrepStmt() {
        return cachePrepStmt;
    }

    /**
     * @return {@code true} if batch rewriting is enabled for performance.
     */
    public boolean isRewriteBatchedStatements() {
        return rewriteBatchedStatements;
    }

    /**
     * @return {@code true} if local session state tracking is enabled.
     */
    public boolean isUseLocalSessionState() {
        return useLocalSessionState;
    }

    /**
     * @return {@code true} if using server-side prepared statements.
     */
    public boolean isUseServerPrepStmts() {
        return useServerPrepStmts;
    }
}
