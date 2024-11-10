package com.topleader.topleader.user.session.reminder;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class SessionReminderControllerIT extends IntegrationTest {

    @Test
    void processNotDisplayedMessages() throws Exception {
        mvc.perform(get("/api/protected/jobs/displayedMessages"))
                .andExpect(status().isOk());

        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("top-leader");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("user1");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Nová zpráva na platformě TopLeader");
        Assertions.assertThat(body).contains("Cool user1,").contains("http://app-test-url");

    }

}