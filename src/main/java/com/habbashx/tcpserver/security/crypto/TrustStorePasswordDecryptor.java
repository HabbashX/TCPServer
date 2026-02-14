package com.habbashx.tcpserver.security.crypto;

import com.habbashx.decryptor.PropertyDecryptor;
import org.jetbrains.annotations.NotNull;

public class TrustStorePasswordDecryptor extends Decryptor implements PropertyDecryptor {

    private static final String KEY = "U6XIlMterhEJuYKZUwdTnQ==";

    /**
     * Decrypts the given encrypted text using a predefined key.
     *
     * @param encryptedText The text to be decrypted.
     * @return The decrypted text.
     * @throws FailedDecryptionOperationException If an error occurs during decryption.
     */
    @Override
    public String decrypt(@NotNull String encryptedText) {
        try {
            return decrypt(encryptedText, KEY);
        } catch (Exception e) {
            throw new FailedDecryptionOperationException(e.getMessage());
        }
    }
}
