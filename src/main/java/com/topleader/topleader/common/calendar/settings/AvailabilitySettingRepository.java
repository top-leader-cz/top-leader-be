package com.topleader.topleader.common.calendar.settings;

import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface AvailabilitySettingRepository extends CrudRepository<CoachAvailabilitySettings, Long>, PagingAndSortingRepository<CoachAvailabilitySettings, Long> {

    @Query("SELECT active FROM coach_availability_settings WHERE coach = :coach AND type = :syncType AND resource = :resource")
    Boolean isActiveNative(String coach, String syncType, String resource);

    default Boolean isActive(String coach, CalendarSyncInfo.SyncType syncType, String resource) {
        return isActiveNative(coach, syncType != null ? syncType.name() : null, resource);
    }

    @Query("SELECT * FROM coach_availability_settings WHERE active = true AND coach = :coach")
    Optional<CoachAvailabilitySettings> findByActive(String coach);

    @Query("SELECT * FROM coach_availability_settings WHERE coach = :coach")
    Optional<CoachAvailabilitySettings> findByCoach(String coach);

    @Modifying
    @Query("DELETE FROM coach_availability_settings WHERE coach = :coach")
    void deleteByCoach(String coach);
}
