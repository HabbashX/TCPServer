package com.habbashx.tcpserver.handler.console;

import java.io.IOException;

public class DefaultServerConsoleHandler extends ConsoleHandler {

    @Override
    public void run() {

        try (this) {
            String message;
            while ((message = getInput().readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
