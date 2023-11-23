package com.topleader.topleader.feedback;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.feedback.repository.FeedbackFormRepository;
import com.topleader.topleader.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import java.util.Set;

import static com.topleader.topleader.user.User.Authority.RESPONDENT;
import static com.topleader.topleader.user.User.Status.REQUESTED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedbackControllerTest extends IntegrationTest {

    @Autowired
    FeedbackFormRepository repository;

    @Autowired
    UserRepository userRepository;



    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithMockUser(username = "user", authorities = "USER")
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
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithMockUser(username = "user", authorities = "USER")
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
    @WithMockUser(username = "user", authorities = "USER")
    void createForm() throws Exception {
        var result = mvc.perform(post("/api/latest/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.readFileAsString("feedback/json/new-form-request.json")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/new-form-response.json");

        TestUtils.assertJsonEquals(result, expected);

        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(2);

        var receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("top-leader");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("pepa@cerny.cz");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Your Valuable Feedback Requested for pepa@cerny.cz Growth on TopLeader");
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(body).contains("http://app-test-url/#/feedback/50/pepa@cerny.cz/");


        receivedMessage = greenMail.getReceivedMessages()[1];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("ilja@bily.cz");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Your Valuable Feedback Requested for ilja@bily.cz Growth on TopLeader");


        Assertions.assertThat(userRepository.findAll()).hasSize(3);
        Assertions.assertThat(userRepository.findById("pepa@cerny.cz").orElseThrow())
               .extracting("username", "lastName", "firstName", "authorities", "status")
               .containsExactly("pepa@cerny.cz", "pepa", "pepa", Set.of(RESPONDENT), REQUESTED);

        Assertions.assertThat(userRepository.findById("ilja@bily.cz").orElseThrow())
                .extracting("username", "lastName", "firstName", "authorities", "status")
                .containsExactly("ilja@bily.cz", "ilja", "ilja", Set.of(RESPONDENT), REQUESTED);
   }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithMockUser(username = "user", authorities = "USER")
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

        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(2);

        var receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("top-leader");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("mala@mela.cz");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Your Valuable Feedback Requested for mala@mela.cz Growth on TopLeader");

        receivedMessage = greenMail.getReceivedMessages()[1];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("kuku@kuku.cz");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Your Valuable Feedback Requested for kuku@kuku.cz Growth on TopLeader");

        Assertions.assertThat(userRepository.findAll()).hasSize(3);
        Assertions.assertThat(userRepository.findById("mala@mela.cz").orElseThrow())
                .extracting("username", "lastName", "firstName", "authorities", "status")
                .containsExactly("mala@mela.cz", "mala", "mala", Set.of(RESPONDENT), REQUESTED);

        Assertions.assertThat(userRepository.findById("kuku@kuku.cz").orElseThrow())
                .extracting("username", "lastName", "firstName", "authorities", "status")
                .containsExactly("kuku@kuku.cz", "kuku", "kuku", Set.of(RESPONDENT), REQUESTED);
    }

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithMockUser(username = "user", authorities = "USER")
    void deleteForm() throws Exception {
        mvc.perform(delete("/api/latest/feedback/1"))
                .andExpect(status().isOk());

        Assertions.assertThat(repository.findById(1L)).isEmpty();

    }
}
