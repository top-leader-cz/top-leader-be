package com.topleader.topleader.common.calendar.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table("calendar_sync_info")
public class CalendarSyncInfo {

    @Id
    private Long id;

    private String username;

    private String refreshToken;

    private String accessToken;

    private Status status;

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

