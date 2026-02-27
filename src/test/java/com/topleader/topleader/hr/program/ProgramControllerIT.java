package com.topleader.topleader.hr.program;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/hr/program-test.sql")
class ProgramControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void listPrograms() throws Exception {
        var result = mvc.perform(get("/api/latest/hr/programs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, TestUtils.readFileAsString("hr/json/program-list-response.json"));
    }

    @Test
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getProgramDetail() throws Exception {
        var result = mvc.perform(get("/api/latest/hr/programs/1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, TestUtils.readFileAsString("hr/json/program-detail-response.json"));
    }

    @Test
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getProgramDetail_notFound() throws Exception {
        mvc.perform(get("/api/latest/hr/programs/999"))
                .andExpect(status().isNotFound());
    }
}
