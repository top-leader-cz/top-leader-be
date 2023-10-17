package com.topleader.topleader.feedback;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PublicFeedbackControllerTest extends IntegrationTest {

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql"})
    void createForm() throws Exception {
         mvc.perform(post("/public/api/latest/feedback/1/token/pepa@cerny.cz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.readFileAsString("feedback/json/submit-form.json")))
                .andExpect(status().isOk());


//        var expected = TestUtils.readFileAsString("feedback/json/new-form-response.json");
//
//        TestUtils.assertJsonEquals(result, expected);
    }
}
