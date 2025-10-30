package com.topleader.topleader.message;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Sql(scripts = "/sql/messages/messages-test.sql")
public class MessageJobControllerIT extends IntegrationTest {

    @Autowired
    MessageRepository messageRepository;

    @Test
    @WithMockUser(authorities = "JOB")
    void processNotDisplayedMessages() throws Exception {

        mvc.perform(get("/api/protected/jobs/displayedMessages"))
                .andExpect(status().isOk());

        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("user1");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Nová zpráva na platformě TopLeader");
        Assertions.assertThat(body).contains("Cool user1,").contains("http://app-test-url");

        Assertions.assertThat(messageRepository.findAll().stream()
                .filter(m -> m.getUserTo().equals("user1"))
                        .map(Message::isNotified)
                .collect(Collectors.toList()))
                .containsExactly(true, true, true);
    }
}
