package com.topleader.topleader.common.calendar;

import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.topleader.topleader.common.calendar.domain.CalendarSyncInfo.Status.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarToErrorHandler {

    private final CalendarSyncInfoRepository repository;

    public void handleError(CalendarSyncInfo info, Throwable e) {
        var previousStatus = info.getStatus();
        info.setStatus(info.getStatus() == OK ? WARN : ERROR);
        if(ERROR != previousStatus) {
            logError(info, e);
        }
        repository.save(info.setLastSync(LocalDateTime.now()));
        if(WARN == previousStatus && ERROR == info.getStatus()) {
            //send email
        }
    }

    public void logError(CalendarSyncInfo info, Throwable e) {
        if(WARN == info.getStatus()) {
            log.warn("{} token refresh job completed with warnings", info.getId().getSyncType());
        } else if(ERROR == info.getStatus()) {
            log.error("{} token refresh job completed with errors", info.getId().getSyncType(), e);
        }
    }
}
