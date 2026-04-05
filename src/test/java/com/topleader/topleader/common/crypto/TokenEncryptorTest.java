package com.topleader.topleader.common.crypto;

import java.util.Base64;

import com.topleader.topleader.common.util.crypto.TokenEncryptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void failsWithEmptyKey() {
        assertThatThrownBy(() -> new TokenEncryptor(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void decryptPlaintextReturnAsIs() {
        var plaintext = "not-a-valid-encrypted-value";
        assertThat(encryptor.decrypt(plaintext)).isEqualTo(plaintext);
    }

    @Test
    void decryptPlaintextGoogleRefreshToken() {
        var plaintext = "1//0e2G5EXAMPLE_REFRESH_TOKEN_from_google";
        assertThat(encryptor.decrypt(plaintext)).isEqualTo(plaintext);
    }

    @Test
    void decryptPlaintextGoogleAccessToken() {
        var plaintext = "ya29.a0AfH6SMBx_long_access_token_value";
        assertThat(encryptor.decrypt(plaintext)).isEqualTo(plaintext);
    }

    @Test
    void migrationFromPlaintextToEncrypted() {
        var originalToken = "1//0e2G5_plaintext_refresh_token_in_db";

        // plaintext token can be decrypted (returns as-is)
        assertThat(encryptor.decrypt(originalToken)).isEqualTo(originalToken);

        // after re-encryption, token is different but decrypts to original
        var encrypted = encryptor.encrypt(originalToken);
        assertThat(encrypted).isNotEqualTo(originalToken);
        assertThat(encryptor.decrypt(encrypted)).isEqualTo(originalToken);
    }
}
