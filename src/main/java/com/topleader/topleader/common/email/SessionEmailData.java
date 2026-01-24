package com.topleader.topleader.common.email;

import java.time.LocalDateTime;

/**
 * DTO for passing session data to email services without creating module dependencies.
 * This allows common.email to send emails about sessions without depending on the session module.
 */
public record SessionEmailData(
    Long id,
    String username,
    String coachUsername,
    LocalDateTime time
) {
}
