package com.topleader.topleader.common.calendar.google;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.common.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleCalendarServiceTest extends IntegrationTest {

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private CalendarSyncInfoRepository calendarSyncInfoRepository;

    @Test
    @Sql("/sql/calendar/calendar-sync-info.sql")
    void storeTokenInfo_shouldReplaceExistingRecord() {
        var username = "test@example.com";
        var tokenResponse = new GoogleCalendarApiClientFactory.TokenResponse(
                "new-access-token", "new-refresh-token", "Bearer", 3600);

        googleCalendarService.storeTokenInfo(username, tokenResponse);

        var saved = calendarSyncInfoRepository.findByUsernameAndSyncType(username, CalendarSyncInfo.SyncType.GOOGLE);

        assertThat(saved).isPresent();
        assertThat(saved.get().getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(saved.get().getAccessToken()).isEqualTo("new-access-token");
        assertThat(saved.get().getStatus()).isEqualTo(CalendarSyncInfo.Status.OK);
    }

    @Test
    @Sql("/sql/calendar/calendar-new-user.sql")
    void storeTokenInfo_shouldCreateNewRecord() {
        var username = "newuser@example.com";
        var tokenResponse = new GoogleCalendarApiClientFactory.TokenResponse(
                "access-token", "refresh-token", "Bearer", 3600);

        googleCalendarService.storeTokenInfo(username, tokenResponse);

        var saved = calendarSyncInfoRepository.findByUsernameAndSyncType(username, CalendarSyncInfo.SyncType.GOOGLE);

        assertThat(saved).isPresent();
        assertThat(saved.get().getRefreshToken()).isEqualTo("refresh-token");
    }
}
