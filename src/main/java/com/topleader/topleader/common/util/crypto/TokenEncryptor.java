package com.topleader.topleader.common.util.crypto;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenEncryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final SecretKeySpec secretKey;
    private final SecureRandom random = new SecureRandom();

    public TokenEncryptor(@Value("${token.encryption-key}") String base64Key) {
        this.secretKey = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
    }

    public String encrypt(String plaintext) {
        return Optional.ofNullable(plaintext)
                .map(this::doEncrypt)
                .orElse(null);
    }

    public String decrypt(String encrypted) {
        return Optional.ofNullable(encrypted)
                .map(this::doDecrypt)
                .orElse(null);
    }

    private String doEncrypt(String plaintext) {
        try {
            var iv = new byte[IV_BYTES];
            random.nextBytes(iv);

            var cipher = initCipher(Cipher.ENCRYPT_MODE, iv);
            var ciphertext = cipher.doFinal(plaintext.getBytes());

            return Base64.getEncoder().encodeToString(
                    ByteBuffer.allocate(IV_BYTES + ciphertext.length).put(iv).put(ciphertext).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt token", e);
        }
    }

    private String doDecrypt(String encrypted) {
        try {
            var buffer = ByteBuffer.wrap(Base64.getDecoder().decode(encrypted));

            var iv = new byte[IV_BYTES];
            buffer.get(iv);
            var ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            return new String(initCipher(Cipher.DECRYPT_MODE, iv).doFinal(ciphertext));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            log.warn("Failed to decrypt token — returning as-is (might be plaintext from before encryption was enabled)");
            return encrypted;
        }
    }

    private Cipher initCipher(int mode, byte[] iv) throws GeneralSecurityException {
        var cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(mode, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
        return cipher;
    }
}
