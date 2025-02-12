package com.topleader.topleader.calendar;


import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/latest/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarSyncInfoRepository repository;


    @PostMapping
    public void createCalendarInfo(@AuthenticationPrincipal UserDetails user, @RequestBody InfoRequest request) {
        repository.save(new CalendarSyncInfo()
                .setId(new CalendarSyncInfo.CalendarInfoId(user.getUsername(), request.syncType()))
                .setEmail(request.email())
                .setStatus(CalendarSyncInfo.Status.NEW)
        );
    }

    public record InfoRequest(String email, CalendarSyncInfo.SyncType syncType) {}
}
