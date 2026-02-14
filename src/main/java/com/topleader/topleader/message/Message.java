package com.topleader.topleader.message;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "user_message")
public record Message(
        @Id Long id,
        Long chatId,
        String userFrom,
        String userTo,
        String messageData,
        Boolean displayed,
        LocalDateTime createdAt,
        boolean notified
) {
}
