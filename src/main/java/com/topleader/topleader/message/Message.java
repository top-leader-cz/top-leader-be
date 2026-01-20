/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter
@ToString
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "user_message")
public class Message {

    @Id
    private Long id;

    private Long chatId;

    private String userFrom;

    private String userTo;

    private String messageData;

    private Boolean displayed;

    private LocalDateTime createdAt;

    private boolean notified;

}
