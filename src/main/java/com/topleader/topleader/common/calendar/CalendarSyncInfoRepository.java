package com.topleader.topleader.common.calendar;

import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CalendarSyncInfoRepository extends CrudRepository<CalendarSyncInfo, Long> {

    @Query("SELECT * FROM calendar_sync_info WHERE sync_type = :syncType AND status != 'ERROR'")
    List<CalendarSyncInfo> findBySyncTypeAndStatus(String syncType);

    List<CalendarSyncInfo> findByUsername(String username);

    Optional<CalendarSyncInfo> findByUsernameAndSyncType(String username, CalendarSyncInfo.SyncType syncType);

    void deleteByUsername(String username);
}
