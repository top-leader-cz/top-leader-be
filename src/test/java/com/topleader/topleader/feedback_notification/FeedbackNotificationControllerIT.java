package com.topleader.topleader.feedback_notification;

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
                      "notificationTime": "2023-10-12T02:00:00",
                      "feedbackFormId": 1,
                      "processedTime": "2023-10-12T02:00:00",
                      "status": "PROCESSED"
                    }
                    """
                )
            )
        ;

    }
}