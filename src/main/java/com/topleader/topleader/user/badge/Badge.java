package com.topleader.topleader.user.badge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Month;
import com.topleader.topleader.common.entity.BaseEntity;

@Table("badge")
@Data
@EqualsAndHashCode(callSuper=false)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Badge extends BaseEntity {
    private String username;

    private AchievementType achievementType;

    private Month month;

    private Integer year;

    public enum AchievementType {
        COMPLETE_SESSION,
        WATCHED_VIDEO,
        COMPLETED_SHORT_GOAL
    }
}
