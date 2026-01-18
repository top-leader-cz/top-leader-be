/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.feedback.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

class FeedbackFormRequestTest {

    @Test
    void shouldGenerateTokensForNewRecipients() {
        // Given new recipients without IDs
        var recipients = List.of(
                new RecipientDto(null, "user1@gmail.com", false),
                new RecipientDto(null, "user2@gmail.com", false)
        );

        // When converting to Recipient entities
        var result = FeedbackFormRequest.toRecipients(recipients, 1L);

        // Then all should have generated tokens
        assertThat("Should convert 2 recipients", result.size(), is(2));
        assertThat("First recipient should have token", result.get(0).getToken(), notNullValue());
        assertThat("Second recipient should have token", result.get(1).getToken(), notNullValue());
        assertThat("Tokens should be different",
                result.get(0).getToken().equals(result.get(1).getToken()), is(false));
    }

    @Test
    void shouldSetNullTokenForExistingRecipients() {
        // Given existing recipients with IDs
        var recipients = List.of(
                new RecipientDto(100L, "user1@gmail.com", false),
                new RecipientDto(200L, "user2@gmail.com", false)
        );

        // When converting to Recipient entities
        var result = FeedbackFormRequest.toRecipients(recipients, 1L);

        // Then all should have NULL tokens (because they should not be saved)
        assertThat("Should convert 2 recipients", result.size(), is(2));
        assertThat("First recipient should have NULL token", result.get(0).getToken(), nullValue());
        assertThat("Second recipient should have NULL token", result.get(1).getToken(), nullValue());
    }

    @Test
    void shouldHandleMixOfNewAndExistingRecipients() {
        // Given mix of new and existing recipients
        var recipients = List.of(
                new RecipientDto(null, "newuser@gmail.com", false),
                new RecipientDto(100L, "existinguser@gmail.com", false)
        );

        // When converting to Recipient entities
        var result = FeedbackFormRequest.toRecipients(recipients, 1L);

        // Then new recipient should have token, existing should have NULL
        assertThat("Should convert 2 recipients", result.size(), is(2));
        assertThat("New recipient should have token", result.get(0).getToken(), notNullValue());
        assertThat("Existing recipient should have NULL token", result.get(1).getToken(), nullValue());
    }
}
