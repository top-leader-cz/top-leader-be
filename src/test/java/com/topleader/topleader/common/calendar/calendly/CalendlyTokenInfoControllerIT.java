package com.topleader.topleader.common.calendar.calendly;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.common.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import static com.topleader.topleader.common.calendar.domain.CalendarSyncInfo.SyncType.CALENDLY;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalendlyTokenInfoControllerIT extends IntegrationTest {


    @Autowired
    CalendarSyncInfoRepository syncInfoRepository;

    @Test
    @WithMockUser(username = "coach1", authorities = {"COACH"})
    void getRecurringSettingActive() throws Exception {
        mvc.perform(delete("/api/latest/calendly-disconnect"))
                .andExpect(status().isOk());

        Assertions.assertThat(syncInfoRepository.findByUsernameAndSyncType("coach1", CALENDLY)
                .isEmpty());
    }

}