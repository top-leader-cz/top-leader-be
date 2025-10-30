package com.topleader.topleader.user.session.reminder;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import io.vavr.control.Try;
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



class SessionReminderControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(authorities = "JOB")
    @Sql(scripts = "/sql/user_info/session/reminder/session-reminder.sql")
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
        testEmail(emails.get("user2"), "Your Journey to Excellence Awaits, first last!", "<b>action 2</b>");
        testEmail(emails.get("user3"), "Your Journey to Excellence Awaits, first last!", "Every step counts");

    }

    private void testEmail(MimeMessage receivedMessage, String subject, String message) throws MessagingException {
        final var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo(subject);
        Assertions.assertThat(body).contains(message).contains("http://app-test-url");

    }

    @Test
    @WithMockUser(authorities = "JOB")
    @Sql(scripts = "/sql/user_info/session/reminder/session-reminder-view-test.sql")
    void viewTest() throws Exception {
        mvc.perform(get("/api/protected/jobs/remind-sessions"))
                .andExpect(status().isOk());

        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(5);
        final var emails = Arrays.stream(greenMail.getReceivedMessages())
                .collect(Collectors.toMap(m -> Try.of(() -> GreenMailUtil.getAddressList(m.getAllRecipients()))
                                .getOrElseThrow(() -> new RuntimeException("Failed to get recipients")), Function.identity()));

        testEmail(emails.get("user-3-days"), "Vaše cesta k rozvoji čeká, user-3-days last!", "user-3-days last");
        testEmail(emails.get("user-10-days"), "Vaše cesta k rozvoji čeká, user-10-days last!", "user-10-days last");
        testEmail(emails.get("user-24-days"), "Vaše cesta k rozvoji čeká, user-24-days last!", "user-24-days last");
        testEmail(emails.get("user-no-session"), "Vaše cesta k rozvoji čeká, user-no-session last!", "user-no-session last");
        testEmail(emails.get("user-3-days-manager-hr"), "Vaše cesta k rozvoji čeká, user-3-days-manager-hr last!", "user-3-days-manager-hr last");
    }

}