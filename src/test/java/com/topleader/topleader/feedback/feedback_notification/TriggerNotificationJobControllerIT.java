package com.topleader.topleader.feedback.feedback_notification;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.topleader.topleader.feedback.feedback_notification.FeedbackNotification;
import com.topleader.topleader.feedback.feedback_notification.FeedbackNotificationRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.TemporalUnitWithinOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/feedback/sql/feedback-notification.sql")
class TriggerNotificationJobControllerIT extends IntegrationTest {

    @Autowired
    FeedbackNotificationRepository feedbackNotificationRepository;

    @Test
    @WithMockUser(authorities = "JOB")
    void processNotificationsTest() throws Exception {
        mvc.perform(post("/api/protected/jobs/feedback-notification"))
            .andExpect(status().isOk())
        ;

        final var feedbackNotification1 = feedbackNotificationRepository.findById(1L).orElseThrow();
        final var feedbackNotification2 = feedbackNotificationRepository.findById(2L).orElseThrow();

        assertThat(feedbackNotification1.getStatus()).isEqualTo(FeedbackNotification.Status.PROCESSED);
        assertThat(feedbackNotification2.getStatus()).isEqualTo(FeedbackNotification.Status.PROCESSED);

        assertThat(feedbackNotification1.getProcessedTime()).isCloseToUtcNow(new TemporalUnitWithinOffset(5, ChronoUnit.MINUTES));
        assertThat(feedbackNotification2.getManualAvailableAfter()).isCloseTo(LocalDateTime.now().plusDays(3), new TemporalUnitWithinOffset(2, ChronoUnit.HOURS));

        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);

        var receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("pepa@cerny.cz");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Friendly Reminder: Please Share Your Feedback for Jakub Svezi");
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(body).contains("http://app-test-url/#/feedback/1/pepa@cerny.cz/");

    }

}
