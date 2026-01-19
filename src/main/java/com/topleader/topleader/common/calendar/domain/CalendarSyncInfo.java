package com.topleader.topleader.common.calendar.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import com.topleader.topleader.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper=false)
@Accessors(chain = true)
@Table("calendar_sync_info")
public class CalendarSyncInfo extends BaseEntity {
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

