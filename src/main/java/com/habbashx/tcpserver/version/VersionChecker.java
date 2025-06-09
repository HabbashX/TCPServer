package com.habbashx.tcpserver.version;

import com.habbashx.tcpserver.socket.server.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * The VersionChecker class is responsible for verifying if the current version
 * of the project matches the latest version available online.
 */
public class VersionChecker {

    /**
     * Checks the current project version against the latest version available online.
     * Logs appropriate messages indicating whether the project is up-to-date or outdated.
     * In case of an error, logs an appropriate error message.
     *
     * @param server An instance of the Server class that provides logger functionality.
     */
    public static void checkProjectVersion(Server server) {

        try {
            final var url = new URL("https://raw.githubusercontent.com/HabbashX/TCPServer/main/src/main/java/com/habbashx/tcpserver/version/version.txt");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {

                try (final var currentVersionReader = new BufferedReader(new FileReader("src/main/java/com/habbashx/tcpserver/version/version.txt"));
                     final var newestVersionReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                ) {
                    String currentVersion = currentVersionReader.readLine();
                    String newestVersion = newestVersionReader.readLine().trim();

                    if (currentVersion.contentEquals(newestVersion)) {
                        server.getServerLogger().info("project version: " + currentVersion);
                    } else {
                        server.getServerLogger().warning("project is running on old version " + currentVersion + " update it to the new version " + newestVersion);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

}
