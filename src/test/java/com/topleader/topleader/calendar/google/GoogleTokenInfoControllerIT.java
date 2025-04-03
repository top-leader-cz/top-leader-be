package com.topleader.topleader.calendar.google;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static com.topleader.topleader.calendar.domain.CalendarSyncInfo.SyncType.GOOGLE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/coach/coach-list-test.sql")
class GoogleTokenInfoControllerIT extends IntegrationTest {

    @Autowired
    CalendarSyncInfoRepository syncInfoRepository;

    @Test
    @WithMockUser(username = "coach1", authorities = {"COACH"})
    void getRecurringSettingActive() throws Exception {
        mvc.perform(delete("/api/latest/google-disconnect"))
                .andExpect(status().isOk());

        Assertions.assertThat(syncInfoRepository.findById(new CalendarSyncInfo.CalendarInfoId("coach1", GOOGLE))
                .isEmpty());
    }

}