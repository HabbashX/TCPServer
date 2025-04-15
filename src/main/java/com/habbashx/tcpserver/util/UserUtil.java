package com.habbashx.tcpserver.util;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.Year;

public class UserUtil {

    @Language("RegExp")
    private static final String USERNAME_REGEX = ".[a-zA-Z0-9]*";

    @Language("RegExp")
    private static final String PHONE_NUMBER_REGEX = ".[0-9]*";

    @Language("RegExp")
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static boolean isValidUsername(@NotNull String username) {
        return username.matches(USERNAME_REGEX);
    }

    public static boolean isValidPhoneNumber(@NotNull String phoneNumber) {

        return phoneNumber.matches(PHONE_NUMBER_REGEX);
    }

    public static boolean isValidEmail(@NotNull String email) {
        return email.matches(EMAIL_REGEX);
    }

    public static @NotNull String generateRandomID(){
        Year currentYear = Year.now();

        int min = 100000;
        int max = 999999;

        int randNumber = (int) Math.floor(Math.random() * (max - min +1)+min);

        String year = String.valueOf(currentYear);
        String randomNumber = String.valueOf(randNumber);

        return year + randomNumber;
    }

    public static String getUserHostAddress() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
