package com.topleader.topleader.coach.availability.settings;

import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AvailabilitySettingRepository extends JpaRepository<CoachAvailabilitySettings, String> {

    @Query("select a.active from  CoachAvailabilitySettings a where a.coach= :coach and a.type= :syncType and a.resource = :resource")
    Boolean isActive(String coach, CalendarSyncInfo.SyncType syncType, String resource);

    @Query("select a from  CoachAvailabilitySettings a where a.active = true and a.coach = :coach")
    Optional<CoachAvailabilitySettings> findByActive(String coach);
}
