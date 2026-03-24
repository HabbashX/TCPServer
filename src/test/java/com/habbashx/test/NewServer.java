package com.habbashx.test;

import com.habbashx.tcpserver.socket.server.foundation.ServerFoundation;

import java.io.IOException;

public class NewServer extends ServerFoundation {


    @Override
    public void run() {
        super.run();
    }

    @Override
    public void shutdown() throws IOException, InterruptedException {
        super.shutdown();
    }

    public static void main(String[] args) {
        new NewServer().run();
    }
}
