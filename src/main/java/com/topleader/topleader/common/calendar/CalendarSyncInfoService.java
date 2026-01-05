package com.topleader.topleader.common.calendar;


import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;



@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarSyncInfoService {

    private final CalendarSyncInfoRepository repository;

    public Optional<CalendarSyncInfo> getInfo(String username, CalendarSyncInfo.SyncType type) {
        return repository.findById(new CalendarSyncInfo.CalendarInfoId(username, type));
    }

    public CalendarSyncInfo save(CalendarSyncInfo info) {
        return repository.save(info);
    }


    public void disconnect(String username, CalendarSyncInfo.SyncType syncType) {
        repository.deleteById(new CalendarSyncInfo.CalendarInfoId(username, syncType));
        log.info("{} disconnected", syncType);
    }
}
