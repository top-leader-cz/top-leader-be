/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.google;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
public class GoogleCalendarSyncInfo {

    @Id
    private String username;

    private String refreshToken;

    private String syncToken;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime lastSync;

    private LocalDateTime enforceFullSync;

    public enum Status {
        IN_PROGRESS, OK, ERROR
    }
}
