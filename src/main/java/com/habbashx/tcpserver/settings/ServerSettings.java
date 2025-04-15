package com.habbashx.tcpserver.settings;


public final class ServerSettings {

    private final Settings settings = new Settings();

    public int getPort() {
        return Integer.parseInt(settings.getValue("server.setting.port"));
    }

    public boolean isReusableAddress() {
        return Boolean.parseBoolean(settings.getValue("server.setting.reusableAddress"));
    }

    public String getKeystorePath() {
        return settings.getValue("server.security.setting.keystore.path");
    }

    public String getKeystorePassword() {
        return settings.getValue("server.security.setting.keystore.password");
    }

    public String getTruststorePath() {
        return settings.getValue("server.security.setting.truststore.path");
    }

    public String getTruststorePassword() {
        return settings.getValue("server.security.setting.truststore.password");
    }

    public String getConfigurationType() {
        return settings.getValue("server.setting.configuration.type");
    }

    public String getUserChatCooldown() {
        return settings.getValue("user.setting.chat.cooldown.second");
    }

    public String getAuthStorageType() {
        return settings.getValue("server.security.setting.authentication.datastorage.type");
    }

    public String getDatabaseURL() {
        return settings.getValue("server.setting.database.url");
    }

    public String getDatabaseUsername() {
        return settings.getValue("server.setting.database.username");
    }

    public String getDatabasePassword() {
        return settings.getValue("server.setting.database.password");
    }
}
