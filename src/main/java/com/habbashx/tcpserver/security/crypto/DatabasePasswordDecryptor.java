package com.habbashx.tcpserver.security.crypto;

import com.habbashx.decryptor.PropertyDecryptor;
import org.jetbrains.annotations.NotNull;

public class DatabasePasswordDecryptor extends Decryptor implements PropertyDecryptor {

    private static final String KEY = "wh4q41iOJLXr0W7JaSH0VQ==";

    @Override
    public String decrypt(@NotNull String encryptedText) {
        try {
            return decrypt(encryptedText, KEY);
        } catch (Exception e) {
            throw new FailedDecryptionOperationException(e.getMessage());
        }
    }
}
