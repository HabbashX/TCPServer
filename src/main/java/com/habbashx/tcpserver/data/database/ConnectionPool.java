package com.habbashx.tcpserver.data.database;

import com.habbashx.tcpserver.socket.server.Server;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static java.lang.String.valueOf;


/**
 * <h1>HikariCP Connection Pool Manager</h1>
 * <p>
 * This class initializes and manages the lifecycle of the database connection pool.
 * It acts as a wrapper around {@link HikariDataSource}, translating the settings provided
 * by {@link DatabaseConnectionSettings} into active pool configurations.
 * </p>
 * * <h3>Key Responsibilities:</h3>
 * <ul>
 * <li>Maps application-specific properties to HikariCP's internal configuration.</li>
 * <li>Optimizes JDBC driver performance (Prepared Statement caching, batching).</li>
 * <li>Provides thread-safe access to SQL {@link Connection} instances.</li>
 * </ul>
 */
public class ConnectionPool {

    /**
     * Internal configuration object for HikariCP parameters.
     */
    private final HikariConfig hikariConfig = new HikariConfig();

    /**
     * The active data source used to fetch connections.
     */
    private HikariDataSource hikariDataSource;

    /**
     * Source of truth for database and pool properties.
     */
    private final DatabaseConnectionSettings dbcs;

    /**
     * Initializes a new connection pool using the server's context.
     * <p>
     * This constructor triggers the loading of properties and immediately configures
     * the underlying HikariCP infrastructure.
     * </p>
     * * @param server The server instance used to initialize settings and logging.
     */
    public ConnectionPool(final Server server) {
        this.dbcs = new DatabaseConnectionSettings(server);
        setupHikariConnectionConfiguration();
    }

    /**
     * Configures the {@link HikariConfig} with credentials, pool sizing, and
     * advanced driver-level optimizations.
     * <p>
     * <b>Optimizations applied:</b>
     * <ul>
     * <li>{@code cachePrepStmts}: Improves performance by reusing compiled SQL.</li>
     * <li>{@code rewriteBatchedStatements}: Combines multiple inserts into one network packet.</li>
     * <li>{@code useLocalSessionState}: Minimizes round-trips for session metadata.</li>
     * </ul>
     */
    private void setupHikariConnectionConfiguration() {
        // Basic Connectivity
        hikariConfig.setJdbcUrl(dbcs.getUrl());
        hikariConfig.setUsername(dbcs.getUsername());
        hikariConfig.setPassword(dbcs.getPassword());

        // Performance Tuning Properties
        hikariConfig.addDataSourceProperty(
                "cachePrepStmts", valueOf(dbcs.isCachePrepStmt())
        );
        hikariConfig.addDataSourceProperty(
                "prepStmtCacheSize", valueOf(dbcs.getPrepStmtCacheSize())
        );
        hikariConfig.addDataSourceProperty(
                "prepStmtCacheSqlLimit", valueOf(dbcs.getPrepStmtCacheSqlLimit())
        );
        hikariConfig.addDataSourceProperty(
                "useServerPrepStmts", valueOf(dbcs.isUseServerPrepStmts())
        );
        hikariConfig.addDataSourceProperty(
                "useLocalSessionState", valueOf(dbcs.isUseLocalSessionState())
        );
        hikariConfig.addDataSourceProperty(
                "rewriteBatchedStatements", valueOf(dbcs.isRewriteBatchedStatements())
        );

        // Pool Sizing
        hikariConfig.setMaximumPoolSize(dbcs.getConnectionPoolSize());
        hikariConfig.setMinimumIdle(dbcs.getMinimumIdle());

        // Finalize DataSource creation
        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    /**
     * Retrieves a connection from the pool.
     * * @return A valid {@link Connection} object.
     *
     * @throws SQLException If the pool is exhausted, the database is unreachable,
     *                      or the timeout is exceeded.
     */
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    /**
     * @return The underlying {@link HikariDataSource} for advanced management
     * or monitoring metrics.
     */
    public HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }
}