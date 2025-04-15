package com.habbashx.tcpserver.version;

import com.habbashx.tcpserver.socket.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class VersionChecker {

    public static void checkProjectVersion(Server server) {

        try {
            URL url = new URL("https://raw.githubusercontent.com/HabbashX/TCPServer/main/src/main/java/com/habbashx/tcpserver/version/version.txt");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                BufferedReader currentVersionReader = new BufferedReader(new FileReader("src/main/java/com/habbashx/tcpserver/version/version.txt"));
                BufferedReader newestVersionReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String currentVersion = currentVersionReader.readLine();
                String newestVersion = newestVersionReader.readLine().trim();

                if (currentVersion.contentEquals(newestVersion)) {
                    server.getServerLogger().info("project version: " + currentVersion);
                } else {
                    server.getServerLogger().warning("project is running on old version " + currentVersion + " update it to the new version " + newestVersion);
                }
                currentVersionReader.close();
                newestVersionReader.close();
            }
        } catch (IOException e) {
           server.getServerLogger().error("cannot check project version please check your internet connection");
        }
    }
}
