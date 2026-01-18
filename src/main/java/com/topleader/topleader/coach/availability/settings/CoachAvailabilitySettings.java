package com.topleader.topleader.coach.availability.settings;

import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Entity
@Accessors(chain = true)
@EqualsAndHashCode(exclude = "id")
public class CoachAvailabilitySettings {

    @Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String coach;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    private CalendarSyncInfo.SyncType type;

    private String resource;

    boolean active;



}
