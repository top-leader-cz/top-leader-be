package com.topleader.topleader.coach.availability.settings;

import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Entity
@Accessors(chain = true)
public class CoachAvailabilitySettings {

    @Id
    private String coach;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    private CalendarSyncInfo.SyncType type;

    private String resource;

    boolean active;



}
