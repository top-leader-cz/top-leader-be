package com.topleader.topleader.common.meeting.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Accessors(chain = true)
@Table("meeting_info")
public class MeetingInfo {

    @Id
    private Long id;

    private String username;

    private Provider provider;

    private String refreshToken;

    private String accessToken;

    private String email;

    private boolean autoGenerate;

    private Status status;

    private LocalDateTime createdAt;

    public enum Provider {
        GOOGLE, TEAMS, ZOOM
    }

    public enum Status {
        OK, WARN, ERROR
    }
}
