package com.topleader.topleader.user.badge;

import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Month;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Data
@Accessors(chain = true)
public class Badge {

    @EmbeddedId
    private BadgeId  badgeId;

    @Column(insertable=false, updatable=false)
    private String username;

    @Enumerated(value = STRING)
    @Column(insertable=false, updatable=false)
    private AchievementType achievementType;

    @Enumerated(value = STRING)
    @Column(insertable=false, updatable=false)
    private Month month;

    @Column(insertable=false, updatable=false)
    private Integer year;

    public enum AchievementType {
        COMPLETE_SESSION,
        WATCHED_VIDEO,
        COMPLETED_SHORT_GOAL

    }



    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BadgeId implements Serializable {

        private String username;

        @Enumerated(value = STRING)
        private AchievementType achievementType;

        @Enumerated(value = STRING)
        private Month month;

        private Integer year;

    }
}
