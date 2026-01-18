package com.topleader.topleader.common.calendar;

import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CalendarSyncInfoRepository extends JpaRepository<CalendarSyncInfo, Long> {

    @Query("select i from CalendarSyncInfo i where i.syncType = :syncType and i.status != 'ERROR'")
    List<CalendarSyncInfo> findBySyncTypeAndStatus(CalendarSyncInfo.SyncType syncType);

    void deleteByUsername(String username);

    Optional<CalendarSyncInfo> findByUsernameAndSyncType(String username, CalendarSyncInfo.SyncType syncType);

}
