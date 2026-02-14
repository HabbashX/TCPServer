package com.habbashx.tcpserver.security.crypto;

import com.habbashx.decryptor.PropertyDecryptor;
import org.jetbrains.annotations.NotNull;

public class KeyStorePasswordDecryptor extends Decryptor implements PropertyDecryptor {

    private static final String KEY = "4JN8hgAQWQo2GaWf8mS8og==";

    @Override
    public String decrypt(@NotNull String encryptedText) {
        try {
            return decrypt(encryptedText, KEY);
        } catch (Exception e) {
            throw new FailedDecryptionOperationException(e.getMessage());
        }
    }
}
