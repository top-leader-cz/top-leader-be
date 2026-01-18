package com.topleader.topleader.feedback;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.feedback.repository.FeedbackFormAnswerRepository;
import com.topleader.topleader.feedback.repository.FeedbackFormRepository;
import com.topleader.topleader.user.UserRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.Set;

import static com.topleader.topleader.user.User.Authority.RESPONDENT;
import static com.topleader.topleader.user.User.Status.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class PublicFeedbackControllerIT extends IntegrationTest {

    private static final String PROMPT_QUERY = "test {\"What is your name?\":[\"answer test\"],\"What is your name?2\":[\"scale.2\"]} English";


    @Autowired
    FeedbackFormAnswerRepository feedbackFormAnswerRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedbackFormRepository feedbackFormRepository;

    @Autowired
    ChatModel chatModel;

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    void getOptions() throws Exception {
        var result = mvc.perform(get("/api/public/latest/feedback/options"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/feedback-options-response.json");

        TestUtils.assertJsonEquals(result, expected);
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql", "/feedback/sql/submit-feedback.sql"})
    void getFrom() throws Exception {
        var result = mvc.perform(get("/api/public/latest/feedback/1/pepa@cerny.cz/token"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/public-get-form-response.json");

        TestUtils.assertJsonEquals(result, expected);

        Assertions.assertThat(userRepository.findAll()).hasSize(2);
        Assertions.assertThat(userRepository.findByUsername("pepa@cerny.cz").get())
                .extracting("username", "lastName", "firstName", "authorities", "status")
                .containsExactly("pepa@cerny.cz", "pepa", "pepa", Set.of(RESPONDENT), VIEWED);
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql", "/feedback/sql/submit-feedback.sql", "/user_insight/ai-prompt.sql"})
    void submitForm() throws Exception {
        Mockito.when(chatModel.call(Mockito.anyString())).thenReturn("""
                 {
                       "strongAreas" : "strong areas",
                        "areasOfImprovement": "areas of improvement"
                }
                """);

         mvc.perform(post("/api/public/latest/feedback/1/pepa@cerny.cz/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.readFileAsString("feedback/json/submit-form.json")))
                .andExpect(status().isOk());

        feedbackFormRepository.findById(1L).ifPresent(f -> {
            var summary = f.getSummary();
            Assertions.assertThat(summary).isNotNull();
            Assertions.assertThat(summary.getStrongAreas()).isEqualTo("strong areas");
            Assertions.assertThat(summary.getAreasOfImprovement()).isEqualTo("areas of improvement");
        });

        var answers = feedbackFormAnswerRepository.findAll();

        Assertions.assertThat(answers).extracting( "form.id", "question.key", "answer", "recipient.recipient",  "recipient.submitted")
                .containsExactly(new Tuple(1L, "question.key.1", "answer test", "pepa@cerny.cz", true),
                        new Tuple(1L , "question.key.2", "scale.2", "pepa@cerny.cz", true));

        Assertions.assertThat(userRepository.findAll()).hasSize(2);
        Assertions.assertThat(userRepository.findByUsername("pepa@cerny.cz").get())
                .extracting("username", "lastName", "firstName", "authorities", "status")
                .containsExactly("pepa@cerny.cz", "pepa", "pepa", Set.of(RESPONDENT), SUBMITTED);
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql", "/feedback/sql/submit-feedback.sql", "/feedback/sql/feedback-set-subbmitted.sql"})
    void requestAccess() throws Exception {
        mvc.perform(post("/api/public/latest/feedback/request-access/1/pepa@cerny.cz/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.readFileAsString("feedback/json/request-access-request.json")))
                .andExpect(status().isOk());

        Assertions.assertThat(userRepository.findAll()).hasSize(2);
        Assertions.assertThat(userRepository.findByUsername("pepa@cerny.cz").get())
                .extracting("username", "firstName", "lastName", "authorities", "status",  "hrEmail")
                .containsExactly("pepa@cerny.cz", "Pepa", "Cerny", Set.of(RESPONDENT), PENDING, "test.hr@email.com");

        var receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("info@topleader.io");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("New Pending user in the TopLeader platform");
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(body).contains("Username: pepa@cerny.cz");
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql", "/feedback/sql/submit-feedback.sql", "/feedback/sql/feedback-set-subbmitted.sql"})
    void requestAccessWithEmail() throws Exception {
        mvc.perform(post("/api/public/latest/feedback/request-access/1/pepa@cerny.cz/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Pepa",
                                    "lastName": "Cerny",
                                    "email": "pepa.cerny@newemail.com",
                                    "hrEmail": "test.hr@email.com"
                                }
                                """))
                .andExpect(status().isOk());

        var user = userRepository.findByUsername("pepa@cerny.cz").orElseThrow();
        Assertions.assertThat(user.getEmail()).isEqualTo("pepa.cerny@newemail.com");
        Assertions.assertThat(user.getFirstName()).isEqualTo("Pepa");
        Assertions.assertThat(user.getLastName()).isEqualTo("Cerny");
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql", "/feedback/sql/submit-feedback.sql", "/feedback/sql/feedback-set-subbmitted.sql"})
    void requestAccessWithDuplicateEmailFails() throws Exception {
        // First create a user with the target email
        userRepository.save(new com.topleader.topleader.user.User()
                .setUsername("another.user@test.com")
                .setEmail("existing@email.com")
                .setFirstName("Existing")
                .setLastName("User")
                .setStatus(PENDING)
                .setAuthorities(Set.of(RESPONDENT)));

        // Now try to use that email for pepa@cerny.cz - should fail
        mvc.perform(post("/api/public/latest/feedback/request-access/1/pepa@cerny.cz/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Pepa",
                                    "lastName": "Cerny",
                                    "email": "existing@email.com",
                                    "hrEmail": "test.hr@email.com"
                                }
                                """))
                .andExpect(status().is5xxServerError());

        // Verify pepa's email was NOT changed
        var pepa = userRepository.findByUsername("pepa@cerny.cz").orElseThrow();
        Assertions.assertThat(pepa.getEmail()).isNotEqualTo("existing@email.com");
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @Transactional
    void requestAccessCreatesNewUserWithEmail() throws Exception {
        // Create a recipient who doesn't exist yet
        var form = feedbackFormRepository.findById(1L).orElseThrow();
        var recipient = new com.topleader.topleader.feedback.entity.Recipient()
                .setForm(form)
                .setRecipient("newuser@test.com")
                .setToken("newtoken")
                .setSubmitted(true);
        form.getRecipients().add(recipient);
        feedbackFormRepository.save(form);

        mvc.perform(post("/api/public/latest/feedback/request-access/1/newuser@test.com/newtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "New",
                                    "lastName": "User",
                                    "email": "newuser@test.com",
                                    "hrEmail": "hr@email.com"
                                }
                                """))
                .andExpect(status().isOk());

        var newUser = userRepository.findByUsername("newuser@test.com").orElseThrow();
        Assertions.assertThat(newUser.getEmail()).isEqualTo("newuser@test.com");
        Assertions.assertThat(newUser.getFirstName()).isEqualTo("New");
        Assertions.assertThat(newUser.getLastName()).isEqualTo("User");
        Assertions.assertThat(newUser.getStatus()).isEqualTo(PENDING);
    }


}
