/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.credit.history;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Table("credit_history")
@Accessors(chain = true)
@NoArgsConstructor
public class CreditHistory {
    @Id
    private Long id;

    private LocalDateTime time;

    private Type type;

    private String username;

    private String context;

    private Integer credit;

    public enum Type {
        PAID,
        RECEIVED,
        SCHEDULED,
        ADDED,
        CANCELED
    }
}
