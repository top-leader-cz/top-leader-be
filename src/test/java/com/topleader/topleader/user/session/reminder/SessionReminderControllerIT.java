package com.topleader.topleader.user.session.reminder;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Sql(scripts = "/sql/user_info/session/reminder/session-reminder.sql")
class SessionReminderControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(authorities = "JOB")
    void processNotDisplayedMessages() throws Exception {
        mvc.perform(get("/api/protected/jobs/remind-sessions"))
                .andExpect(status().isOk());

        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("top-leader");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("user2");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Udržujte svůj pokrok na správné cestě!");
        Assertions.assertThat(body).contains("first last,").contains("http://app-test-url");

    }

}