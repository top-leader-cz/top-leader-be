package com.topleader.topleader.feedback;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.feedback.repository.FeedbackFormQuestionRepository;
import com.topleader.topleader.feedback.repository.FeedbackFormRepository;
import com.topleader.topleader.feedback.repository.QuestionRepository;
import com.topleader.topleader.feedback_notification.FeedbackNotificationRepository;
import com.topleader.topleader.user.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.TemporalUnitWithinOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

import java.util.Set;

import static com.topleader.topleader.feedback_notification.FeedbackNotification.Status.NEW;
import static com.topleader.topleader.user.User.Authority.RESPONDENT;
import static com.topleader.topleader.user.User.Status.REQUESTED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedbackControllerIT extends IntegrationTest {

    @Autowired
    FeedbackFormRepository repository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    FeedbackFormQuestionRepository feedackFormQuestionRepository;

    @Autowired
    FeedbackNotificationRepository feedbackNotificationRepository;

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql", "/feedback/sql/feedback-answers.sql"})
    @WithUserDetails("jakub.svezi@dummy.com")
    void getForm() throws Exception {
        var result = mvc.perform(get("/api/latest/feedback/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/get-form-response.json");

        TestUtils.assertJsonEquals(result, expected);
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql", "/feedback/sql/feedback-answers.sql"})
    @WithMockUser(authorities = "USER")
    void getFormAuthorizationFailed() throws Exception {
       mvc.perform(get("/api/latest/feedback/1"))
                .andExpect(status().is4xxClientError());

   }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithUserDetails("jakub.svezi@dummy.com")
    void getForms() throws Exception {
        var result = mvc.perform(get("/api/latest/feedback/user/jakub.svezi@dummy.com"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/forms-response.json");

        TestUtils.assertJsonEquals(result, expected);
    }


    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithUserDetails("jakub.svezi@dummy.com")
        void createForm() throws Exception {
        var result = mvc.perform(post("/api/latest/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.readFileAsString("feedback/json/new-form-request.json")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/new-form-response.json");
//        Assertions.assertThat(repository.findById(2L).orElseThrow().getQuestions().stream().findFirst().orElseThrow().getId().getFormId()).isNotNull();

        TestUtils.assertJsonEquals(result, expected);

        Assertions.assertThat(questionRepository.getDefaultOptions()).hasSize(2);

        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(2);

        var receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("pepa@cerny.cz");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Your Valuable Feedback Requested for Jakub Svezi Growth on TopLeader");
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(body).contains("http://app-test-url/#/feedback/2/pepa@cerny.cz/");


        receivedMessage = greenMail.getReceivedMessages()[1];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("ilja@bily.cz");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Your Valuable Feedback Requested for Jakub Svezi Growth on TopLeader");
        Assertions.assertThat(receivedMessage.getContent().toString()).contains("Jakub Svezi");


        Assertions.assertThat(userRepository.findAll()).hasSize(3);
        Assertions.assertThat(userRepository.findById("pepa@cerny.cz").orElseThrow())
                .extracting("username", "lastName", "firstName", "authorities", "status")
                .containsExactly("pepa@cerny.cz", "pepa", "pepa", Set.of(RESPONDENT), REQUESTED);

        Assertions.assertThat(userRepository.findById("ilja@bily.cz").orElseThrow())
                .extracting("username", "lastName", "firstName", "authorities", "status")
                .containsExactly("ilja@bily.cz", "ilja", "ilja", Set.of(RESPONDENT), REQUESTED);

        final var notification = feedbackNotificationRepository.findByFeedbackFormId(2L).orElseThrow();

        Assertions.assertThat(notification)
                .extracting("id", "username", "status", "processedTime")
                .containsExactly(2L, "jakub.svezi@dummy.com", NEW, null);

        Assertions.assertThat(notification.getNotificationTime()).isCloseTo(LocalDateTime.now().plusDays(5), new TemporalUnitWithinOffset(1, ChronoUnit.HOURS));

    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithUserDetails("jakub.svezi@dummy.com")
    void createFormNewQuestion() throws Exception {
        var result = mvc.perform(post("/api/latest/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.readFileAsString("feedback/json/new-form-new-question-request.json")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/new-form-new-question-response.json");

        Assertions.assertThat(questionRepository.getDefaultOptions()).hasSize(2);
        TestUtils.assertJsonEquals(result, expected);
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql", "/feedback/sql/custom-questions.sql"})
    @WithUserDetails("jakub.svezi@dummy.com")
    void createFormNewQuestionAlreadyExits() throws Exception {
        var result = mvc.perform(post("/api/latest/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.readFileAsString("feedback/json/new-form-new-question-request.json")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertThat(questionRepository.getDefaultOptions()).hasSize(2);
        var expected = TestUtils.readFileAsString("feedback/json/new-form-new-question-response.json");

        TestUtils.assertJsonEquals(result, expected);
    }


    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithUserDetails("jakub.svezi@dummy.com")
    void updateForm() throws Exception {
        var result = mvc.perform(put("/api/latest/feedback/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.readFileAsString("feedback/json/update-form-request.json")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/update-form-response.json");

        TestUtils.assertJsonEquals(result, expected);
        Assertions.assertThat(questionRepository.getDefaultOptions()).hasSize(2);

        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(2);

        var receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("mala@mela.cz");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Your Valuable Feedback Requested for Jakub Svezi Growth on TopLeader");
        Assertions.assertThat(receivedMessage.getContent().toString()).contains("Jakub Svezi");

        receivedMessage = greenMail.getReceivedMessages()[1];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("kuku@kuku.cz");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Your Valuable Feedback Requested for Jakub Svezi Growth on TopLeader");

        Assertions.assertThat(userRepository.findAll()).hasSize(3);
        Assertions.assertThat(userRepository.findById("mala@mela.cz").orElseThrow())
                .extracting("username", "lastName", "firstName", "authorities", "status")
                .containsExactly("mala@mela.cz", "mala", "mala", Set.of(RESPONDENT), REQUESTED);
        Assertions.assertThat(receivedMessage.getContent().toString()).contains("Jakub Svezi");

        Assertions.assertThat(userRepository.findById("kuku@kuku.cz").orElseThrow())
                .extracting("username", "lastName", "firstName", "authorities", "status")
                .containsExactly("kuku@kuku.cz", "kuku", "kuku", Set.of(RESPONDENT), REQUESTED);

        final var notification = feedbackNotificationRepository.findByFeedbackFormId(1L).orElseThrow();

        Assertions.assertThat(notification)
                .extracting("id", "username", "status", "processedTime")
                .containsExactly(1L, "jakub.svezi@dummy.com", NEW, null);

        Assertions.assertThat(notification.getNotificationTime()).isCloseTo(LocalDateTime.now().plusDays(5), new TemporalUnitWithinOffset(1, ChronoUnit.HOURS));

    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithUserDetails("jakub.svezi@dummy.com")
    void removeQuestion() throws Exception {
        var result = mvc.perform(put("/api/latest/feedback/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.readFileAsString("feedback/json/remove-question-request.json")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/remove-question-response.json");

        TestUtils.assertJsonEquals(result, expected);

        Assertions.assertThat(feedackFormQuestionRepository.findAll()).hasSize(1);
        Assertions.assertThat(greenMail.getReceivedMessages()).isEmpty();
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithUserDetails("jakub.svezi@dummy.com")
    void customQuestion() throws Exception {
        var result = mvc.perform(put("/api/latest/feedback/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.readFileAsString("feedback/json/custom-question-request.json")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/custom-question-response.json");

        TestUtils.assertJsonEquals(result, expected);

        Assertions.assertThat(feedackFormQuestionRepository.findAll()).hasSize(2);
        Assertions.assertThat(greenMail.getReceivedMessages()).isEmpty();
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithUserDetails("jakub.svezi@dummy.com")
    void deleteForm() throws Exception {
        mvc.perform(delete("/api/latest/feedback/1"))
                .andExpect(status().isOk());

        Assertions.assertThat(repository.findById(1L)).isEmpty();

    }
}
