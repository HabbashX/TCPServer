package com.habbashx.tcpserver.settings;

import com.habbashx.annotation.InjectProperty;

/**
 * The ServerSettings class encapsulates configuration properties required for server setup,
 * security settings, user management, and database connectivity.
 *
 * Properties are injected via the {@code @InjectProperty} annotation, allowing external
 * configuration to be defined and automatically loaded. This class functions as a central
 * repository for the server's settings, supporting ease of access and maintainability.
 *
 * The injected properties include:
 *  - Network settings like port and address reuse preferences.
 *  - SSL/TLS security configurations for keystore and truststore paths and passwords.
 *  - User interaction settings such as chat cooldown duration.
 *  - Authentication storage type for user management.
 *  - Database configurations including URL, username, and password.
 *
 * This class is immutable, ensuring thread safety for concurrent access in a multi-threaded
 * server environment. The provided getter methods allow controlled access to the configuration
 * values after they are injected during initialization.
 */
public final class ServerSettings {

    @InjectProperty("port")
    private int port;

    @InjectProperty("reusableAddress")
    private boolean isReusableAddress;

    @InjectProperty("security.keystore.path")
    private String keyStorePath;

    @InjectProperty("security.keystore.password")
    private String keyStorePassword;

    @InjectProperty("security.truststore.path")
    private String trustStorePath;

    @InjectProperty("security.truststore.password")
    private String trustStorePassword;

    @InjectProperty("configuration.type")
    private String getConfigurationType;

    @InjectProperty("chat.cooldown.second")
    private String userChatCooldown;

    @InjectProperty("security.authentication.datastorage.type")
    private String authStorageType;

    @InjectProperty("database.url")
    private String databaseURL;

    @InjectProperty("database.username")
    private String databaseUsername;

    @InjectProperty("database.password")
    private String databasePassword;

    public ServerSettings() {
    }

    public int getPort() {
        return port;
    }

    public boolean isReusableAddress() {
        return isReusableAddress;
    }

    public String getKeystorePassword() {
        return keyStorePassword;
    }

    public String getKeystorePath() {
        return keyStorePath;
    }

    public String getTruststorePath() {
        return trustStorePath;
    }

    public String getTruststorePassword() {
        return trustStorePassword;
    }

    public String getConfigurationType() {
        return getConfigurationType;
    }

    public String getUserChatCooldown() {
        return userChatCooldown;
    }

    public String getAuthStorageType() {
        return authStorageType;
    }

    public String getDatabaseURL() {
        return databaseURL;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }
}
