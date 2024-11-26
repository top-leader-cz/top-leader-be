package com.topleader.topleader.user.session.reminder;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
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

        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(3);
        final var emails = Arrays.stream(greenMail.getReceivedMessages())
                .collect(Collectors.toMap(m -> {
                    try {
                        return GreenMailUtil.getAddressList(m.getAllRecipients());
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }, Function.identity()));
        testEmail(emails.get("user2"), "Udržujte svůj pokrok na správné cestě!");
        testEmail(emails.get("user3"), "Připraveni na další krok ve svém rozvoji?");
        testEmail(emails.get("user4"), "Neztrácejte tempo – naplánujte si další lekci!");

    }

    private void testEmail(MimeMessage receivedMessage, String message) throws MessagingException {
        final var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("top-leader");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo(message);
        Assertions.assertThat(body).contains("first last,").contains("http://app-test-url");

    }

}