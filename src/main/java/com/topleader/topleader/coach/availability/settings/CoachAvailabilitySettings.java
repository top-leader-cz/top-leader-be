package com.topleader.topleader.coach.availability.settings;

import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("coach_availability_settings")
@Accessors(chain = true)
public class CoachAvailabilitySettings {

    @Id
    private String coach;

    private String type;

    private String resource;

    boolean active;

    public CalendarSyncInfo.SyncType getTypeEnum() {
        return type != null ? CalendarSyncInfo.SyncType.valueOf(type) : null;
    }

    public CoachAvailabilitySettings setTypeEnum(CalendarSyncInfo.SyncType type) {
        this.type = type != null ? type.name() : null;
        return this;
    }

    public CoachAvailabilitySettings setType(CalendarSyncInfo.SyncType type) {
        return setTypeEnum(type);
    }

}
