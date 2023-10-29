/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.credit.history;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Entity
@Accessors(chain = true)
@NoArgsConstructor
public class CreditHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "credit_history_seq")
    @SequenceGenerator(name = "credit_history_seq", sequenceName = "credit_history_seq", allocationSize = 1)
    private Long id;

    private LocalDateTime time;

    @Enumerated(EnumType.STRING)
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
