package com.topleader.topleader.calendar;

import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CalendarSyncInfoRepository extends JpaRepository<CalendarSyncInfo, CalendarSyncInfo.CalendarInfoId> {

    List<CalendarSyncInfo> findBySyncType(CalendarSyncInfo.SyncType syncType);
}
