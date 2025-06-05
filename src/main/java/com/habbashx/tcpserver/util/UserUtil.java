package com.habbashx.tcpserver.util;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.Year;

/**
 * Utility class containing static methods for validating and processing user-related data.
 * This class provides functionality such as validation for usernames, phone numbers,
 * and email addresses, generating a random user ID, and fetching the host address of the user.
 */
public final class UserUtil {

    /**
     * A regular expression pattern for validating usernames.
     * This pattern allows usernames to consist of any character followed by zero or
     * more alphanumeric characters (a-z, A-Z, 0-9).
     */
    @Language("RegExp")
    private static final String USERNAME_REGEX = ".[a-zA-Z0-9]*";

    /**
     * A regular expression pattern to validate phone numbers.
     * This regex pattern matches strings that contain a sequence of digits (0-9) prefixed by any single character.
     */
    @Language("RegExp")
    private static final String PHONE_NUMBER_REGEX = ".[0-9]*";

    /**
     * A regular expression pattern used for validating email addresses.
     * This pattern ensures the email address follows standard email format rules,
     * including a combination of alphanumeric characters, dots, underscores, hyphens,
     * a mandatory '@' symbol, domain name, and a top-level domain with a minimum of two characters.
     * <p>
     * Valid examples include patterns like "user@example.com".
     * Invalid examples include patterns like "userexample.com" or "user@.com".
     */
    @Language("RegExp")
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /**
     * Validates the specified username based on a predefined regular expression pattern.
     *
     * @param username the username to be validated, must not be null.
     * @return true if the username matches the defined pattern, false otherwise.
     */
    public static boolean isValidUsername(@NotNull String username) {
        return username.matches(USERNAME_REGEX);
    }

    /**
     * Validates whether the given phone number matches the defined format.
     * The validation is``` basedjava on
     * a/**
     * predefined * regular expression pattern Valid that specifies
     * the expected format for phone numbersates the specified phone number based on a predefined regular expression pattern.
     * .
     * <p>
     * <p>
     * * @ @paramparam phone phoneNumberNumber the the phone phone number number to to be validate validated,, must must not not be be null null
     * .
     * * @ @returnreturn true true if if the the phone phone number number matches matches the the predefined defined pattern pattern,, false false otherwise otherwise
     * .
     */
    public static boolean isValidPhoneNumber(@NotNull String phoneNumber) {

        return phoneNumber.matches(PHONE_NUMBER_REGEX);
    }

    /**
     * Validates whether the provided email address matches the standard email format.
     *
     * @param email the email address to be validated, must not be null.
     * @return true if the email address matches the defined regular expression for a valid email; false otherwise.
     */
    public static boolean isValidEmail(@NotNull String email) {
        return email.matches(EMAIL_REGEX);
    }

    /**
     * Generates a unique random ID by combining the current year with a randomly
     * generated six-digit number.
     *
     * @return A non-null string representing the generated random ID, formatted as
     * the current year followed by a six-digit random number.
     */
    public static @NotNull String generateRandomID() {
        Year currentYear = Year.now();

        int min = 100000;
        int max = 999999;

        int randNumber = (int) Math.floor(Math.random() * (max - min + 1) + min);

        String year = String.valueOf(currentYear);
        String randomNumber = String.valueOf(randNumber);

        return year + randomNumber;
    }

    /**
     * Retrieves the host address of the local machine as a string.
     *
     * @return the IPv4 address of the local machine in string format
     * @throws RuntimeException if an error occurs while retrieving the host address
     */
    public static String getUserHostAddress() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
