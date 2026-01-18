package com.topleader.topleader.common.calendar.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity
public class CalendarSyncInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String refreshToken;

    private String accessToken;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private SyncType syncType;

    private LocalDateTime lastSync = LocalDateTime.now();

    private String ownerUrl;

    public enum Status {
        OK, WARN, ERROR, NEW
    }

    public enum SyncType {
        GOOGLE, CALENDLY, CUSTOM
    }
}

