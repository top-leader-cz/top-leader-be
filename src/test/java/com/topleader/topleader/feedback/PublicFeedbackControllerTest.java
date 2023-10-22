package com.topleader.topleader.feedback;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.feedback.repository.FeedbackFormAnswerRepository;
import com.topleader.topleader.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.Set;

import static com.topleader.topleader.user.User.Authority.RESPONDENT;
import static com.topleader.topleader.user.User.Authority.USER;
import static com.topleader.topleader.user.User.Status.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PublicFeedbackControllerTest extends IntegrationTest {

    @Autowired
    FeedbackFormAnswerRepository feedbackFormAnswerRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql", "/feedback/sql/submit-feedback.sql"})
    void getFrom() throws Exception {
        var result = mvc.perform(get("/api/public/latest/feedback/1/pepa@cerny.cz/token"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/get-form-response.json");

        TestUtils.assertJsonEquals(result, expected);

        Assertions.assertThat(userRepository.findAll()).hasSize(2);
        Assertions.assertThat(userRepository.findById("pepa@cerny.cz").get())
                .extracting("username", "lastName", "firstName", "authorities", "status")
                .containsExactly("pepa@cerny.cz", "pepa", "pepa", Set.of(RESPONDENT), VIEWED);
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql", "/feedback/sql/submit-feedback.sql"})
    void createForm() throws Exception {
         mvc.perform(post("/api/public/latest/feedback/1/pepa@cerny.cz/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.readFileAsString("feedback/json/submit-form.json")))
                .andExpect(status().isOk());

        var answers = feedbackFormAnswerRepository.findAll();

        Assertions.assertThat(answers).extracting( "form.id", "question.key", "answer", "recipient.recipient",  "recipient.submitted")
                .containsExactly(new Tuple(1L, "question.key.1", "answer test", "pepa@cerny.cz", true),
                        new Tuple(1L , "question.key.2", "scale.2", "pepa@cerny.cz", true));

        Assertions.assertThat(userRepository.findAll()).hasSize(2);
        Assertions.assertThat(userRepository.findById("pepa@cerny.cz").get())
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
        Assertions.assertThat(userRepository.findById("pepa.cerny@seznam.cz").get())
                .extracting("username", "firstName", "lastName", "authorities", "status", "company", "hrEmail")
                .containsExactly("pepa.cerny@seznam.cz", "Pepa", "Cerny", Set.of(RESPONDENT), PENDING, "test company", "test.hr@email.com");
    }


}
