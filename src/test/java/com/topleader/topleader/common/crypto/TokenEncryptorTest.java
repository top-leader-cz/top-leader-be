package com.topleader.topleader.common.crypto;

import java.util.Base64;

import com.topleader.topleader.common.util.crypto.TokenEncryptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenEncryptorTest {

    private static final String TEST_KEY = Base64.getEncoder().encodeToString(new byte[32]);

    private final TokenEncryptor encryptor = new TokenEncryptor(TEST_KEY);

    @Test
    void encryptAndDecrypt() {
        var token = "ya29.a0AfH6SMBx_some_google_access_token";

        var encrypted = encryptor.encrypt(token);
        var decrypted = encryptor.decrypt(encrypted);

        assertThat(encrypted).isNotEqualTo(token);
        assertThat(decrypted).isEqualTo(token);
    }

    @Test
    void encryptProducesDifferentOutputEachTime() {
        var token = "refresh_token_value";

        var encrypted1 = encryptor.encrypt(token);
        var encrypted2 = encryptor.encrypt(token);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
        assertThat(encryptor.decrypt(encrypted1)).isEqualTo(token);
        assertThat(encryptor.decrypt(encrypted2)).isEqualTo(token);
    }

    @Test
    void nullInputReturnsNull() {
        assertThat(encryptor.encrypt(null)).isNull();
        assertThat(encryptor.decrypt(null)).isNull();
    }

    @Test
    void disabledWhenNoKey() {
        var disabled = new TokenEncryptor("");

        assertThat(disabled.isEnabled()).isFalse();
        assertThat(disabled.encrypt("plaintext")).isEqualTo("plaintext");
        assertThat(disabled.decrypt("plaintext")).isEqualTo("plaintext");
    }

    @Test
    void decryptPlaintextReturnAsIs() {
        var plaintext = "not-a-valid-encrypted-value";
        assertThat(encryptor.decrypt(plaintext)).isEqualTo(plaintext);
    }
}
