package com.habbashx.tcpserver.settings;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import static com.habbashx.tcpserver.util.ServerUtils.SERVER_SETTINGS_PATH;

public final class Settings {

    private static final File file = new File(SERVER_SETTINGS_PATH);

    private final Properties properties = new Properties();

    public Settings() {
        loadSettings();
    }

    public void loadSettings() {

        try (final Reader reader = new FileReader(file)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getValue(String property) {
        return properties.getProperty(property);
    }

}
