package com.topleader.topleader.coach.availability.settings;

import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;
import com.topleader.topleader.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper=false)
@Table("coach_availability_settings")
@Accessors(chain = true)
public class CoachAvailabilitySettings extends BaseEntity {
    private String coach;

    private CalendarSyncInfo.SyncType type;

    private String resource;

    boolean active;

}
