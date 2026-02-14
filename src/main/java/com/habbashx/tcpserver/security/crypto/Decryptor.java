package com.habbashx.tcpserver.security.crypto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public abstract class Decryptor {

    /**
     * The encryption algorithm used for decryption.
     */
    private static final String ALGORITHM = "AES";

    @Nullable
    public abstract String decrypt(@NotNull String encryptedText) throws Exception;

    /**
     * Decrypts the given encrypted text using the specified key.
     *
     * @param encryptedText The text to be decrypted.
     * @param key           The base64-encoded key used for decryption.
     * @return The decrypted text, or null if decryption fails.
     * @throws Exception If an error occurs during the decryption process.
     */
    @Nullable
    protected String decrypt(@NotNull String encryptedText, String key) throws Exception {

        final byte[] decodedKey = Base64.getDecoder().decode(key);
        final SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKey, ALGORITHM);

        final Cipher anotherCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        anotherCipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        final byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText.getBytes());
        final byte[] decryptedBytes = anotherCipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }
}
