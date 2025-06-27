package com.habbashx.tcpserver.handler.console;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class ConsoleHandler implements Runnable, Closeable {

    private final BufferedReader input;

    public ConsoleHandler() {
        this.input = new BufferedReader(new InputStreamReader(System.in));
    }

    public BufferedReader getInput() {
        return input;
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
