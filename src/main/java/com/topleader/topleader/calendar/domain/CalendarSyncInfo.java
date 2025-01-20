package com.topleader.topleader.calendar.domain;

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

    @EmbeddedId
    private CalendarInfoId id;

    private String username;

    private String refreshToken;

    private String accessToken;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private SyncType syncType;

    private LocalDateTime lastSync;

    private String ownerUrl;

    public enum Status {
        OK, WARN, ERROR
    }

    public enum SyncType {
        GOOGLE, CALENDLY
    }

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarInfoId implements Serializable {

        @Column( insertable = false, updatable = false)
        private String username;

        @Enumerated(EnumType.STRING)
        @Column(insertable = false, updatable = false)
        private SyncType syncType;

    }

}

