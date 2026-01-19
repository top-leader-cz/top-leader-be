package com.topleader.topleader.coach.availability.settings;

import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("coach_availability_settings")
@Accessors(chain = true)
public class CoachAvailabilitySettings  {

    @Id
    private Long id;

    private String coach;

    private CalendarSyncInfo.SyncType type;

    private String resource;

    boolean active;

}
