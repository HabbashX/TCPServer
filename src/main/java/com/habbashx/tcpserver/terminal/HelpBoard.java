package com.habbashx.tcpserver.terminal;

import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * The HelpBoard class is responsible for reading and displaying the help board content
 * from a predefined file. It formats the output with a header and ensures that the content
 * is displayed correctly in the console.
 * <p>
 * The help board file is expected to be located in the "data" directory relative to the server's root.
 */
public class HelpBoard {

    /**
     * The file that contains the help board content.
     * It is expected to be located in the "data" directory relative to the server's root.
     */
    private static final File HELP_BOARD_FILE = new File("data/helpBoard.txt");


    /**
     * Prints the help board content to the console.
     * It reads the content from the help board file, formats it with a header,
     * and prints it to the console.
     */
    public static @Nullable String getHelpBoard() {
        try {
            final int longestLineLength = getLongestLineLength();
            final int halfLineLength = longestLineLength / 2;
            return getHelpBoard(halfLineLength);
        } catch (IOException e) {
            Server.getInstance().getServerLogger().error(e);
            return null;
        }
    }

    /**
     * Reads the help board content from the file and formats it with a header.
     *
     * @param halfLineLength The length of half the longest line, used for formatting the header.
     * @return A formatted string containing the help board content.
     * @throws IOException If an error occurs while reading the help board file.
     */
    private static @NotNull String getHelpBoard(int halfLineLength) throws IOException {

        final String helpLine = "-".repeat(Math.max(0, halfLineLength)) +
                " " + "[HELP]" + " " +
                "-".repeat(Math.max(0, halfLineLength));
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(helpLine);

        final BufferedReader reader = new BufferedReader(new FileReader(HELP_BOARD_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append(System.lineSeparator());
        }
        reader.close();

        return stringBuilder.toString();
    }

    /**
     * Calculates the length of the longest line in the help board file.
     *
     * @return The length of the longest line.
     * @throws IOException If an error occurs while reading the help board file.
     */
    private static int getLongestLineLength() throws IOException {
        int longestLineLength = 0;

        final BufferedReader reader = new BufferedReader(new FileReader(HELP_BOARD_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            int lineLength = line.length();

            if (longestLineLength < lineLength) {
                longestLineLength = lineLength;
            }
        }
        reader.close();
        return longestLineLength;

    }
}
