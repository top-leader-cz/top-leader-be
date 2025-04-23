package com.topleader.topleader.calendar;

import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarSyncInfoRepository extends JpaRepository<CalendarSyncInfo, CalendarSyncInfo.CalendarInfoId> {

    List<CalendarSyncInfo> findBySyncType(CalendarSyncInfo.SyncType syncType);

    void deleteByUsername(String username);

}
