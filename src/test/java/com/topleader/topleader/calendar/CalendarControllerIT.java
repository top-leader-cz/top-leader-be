package com.topleader.topleader.calendar;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/calendly/calendly-token.sql")
class CalendarControllerIT extends IntegrationTest {

    @Autowired
    private CalendarSyncInfoRepository repository;

    @SneakyThrows
    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void createCalendarInfo() {
        mvc.perform(MockMvcRequestBuilders.post("/api/latest/calendar")
                        .contentType("application/json")
                        .content("""    
                                                      {
                                                          "email": "matched@email.com",
                                                          "syncType": "CALENDLY"
                                                      }
                                """))
                .andExpect(status().isOk());

        var calendarSyncInfo = repository.findByEmailOrUsername("user", CalendarSyncInfo.SyncType.CALENDLY).orElseThrow();

        assertThat(calendarSyncInfo.getAccessToken()).isNull();
        assertThat(calendarSyncInfo.getRefreshToken()).isNull();
        assertThat(calendarSyncInfo.getOwnerUrl()).isNull();
        assertThat(calendarSyncInfo.getStatus()).isEqualTo(CalendarSyncInfo.Status.NEW);
        assertThat(calendarSyncInfo.getId().getUsername()).isEqualTo("user");
        assertThat(calendarSyncInfo.getEmail()).isEqualTo("matched@email.com");
    }


}