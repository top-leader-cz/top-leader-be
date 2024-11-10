package com.topleader.topleader.user.session;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class SessionReminderControllerIT extends IntegrationTest {

    @Test
    @WithMockUser("user2")
    void getUserSetessionData() throws Exception {
        mvc.perform(get("/api/protected/jobs/unscheduled-sessions"))
                .andExpect(status().isOk())

        ;
    }

}