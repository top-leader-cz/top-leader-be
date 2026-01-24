package com.topleader.topleader.feedback.feedback_notification;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/feedback/sql/feedback.sql")
class FeedbackNotificationControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(username = "jakub.svezi@dummy.com", authorities = "USER")
    void getForms() throws Exception {
        mvc.perform(get("/api/latest/feedback-notification/1"))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json(
                """
                    {
                      "automaticReminderTime": "2023-10-12T00:00:00",
                      "manualReminderAllowedAfter": null,
                      "feedbackFormId": 1,
                      "automaticReminderSentTime": "2023-10-12T00:00:00",
                      "manualReminderAllowed": true,
                      "status": "AUTOMATIC_REMINDER_SENT"
                    }
                    """
                )
            )
        ;

    }
}
