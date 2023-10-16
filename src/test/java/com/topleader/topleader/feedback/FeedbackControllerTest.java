package com.topleader.topleader.feedback;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FeedbackControllerTest extends IntegrationTest {

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    @WithMockUser(username = "user", authorities = "USER")
    void getOptions() throws Exception {
        var result = mvc.perform(get("/api/latest/feedback/options"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("feedback/json/feedback-options-response.json");

        TestUtils.assertJsonEquals(result, expected);
    }

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
    }
}
