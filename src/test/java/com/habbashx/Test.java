package com.habbashx;


import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.IO.println;

public class Test {

    static void main(String[] args) {
        try {
            String file = Files.readString(Path.of("server/settings/settings.properties"));


            println(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
