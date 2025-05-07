package com.habbashx.tcpserver.settings;

import com.habbashx.annotation.InjectProperty;

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
