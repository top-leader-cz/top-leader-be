package com.topleader.topleader.hr.company;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql("/sql/companies/base-companies-test.sql")
class CompanyControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(authorities = "ADMIN")
    void listCompanies() throws Exception {
        var result = mvc.perform(get("/api/latest/companies"))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
            [
              {"id": 1, "name": "company1", "defaultAllowedCoachRate": ["$$$"], "programsEnabled": false},
              {"id": 2, "name": "company2", "defaultAllowedCoachRate": ["$$"], "programsEnabled": false}
            ]
            """);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createCompanyTest() throws Exception {
        var result = mvc.perform(post("/api/latest/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "company3"}
                    """)
            )
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
            {"id": 3, "name": "company3", "programsEnabled": false}
            """);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createCompany_nameAlreadyUsedTest() throws Exception {
        mvc.perform(post("/api/latest/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "company1"}
                    """)
            )
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().json(
                """
                    [{"errorCode": "already.existing", "fields": [{"name": "name", "value": "company1"}], "errorMessage": "Company with this name already exists"}]
                    """
            ));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateCompanyTest() throws Exception {
        var result = mvc.perform(post("/api/latest/companies/company1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"defaultAllowedCoachRate": ["$"]}
                    """)
            )
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
            {"id": 1, "name": "company1", "defaultAllowedCoachRate": ["$"], "programsEnabled": false}
            """);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateCompanyProgramsEnabledTest() throws Exception {
        var result = mvc.perform(post("/api/latest/companies/company1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"defaultAllowedCoachRate": ["$$$"], "programsEnabled": true}
                    """)
            )
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
            {"id": 1, "name": "company1", "defaultAllowedCoachRate": ["$$$"], "programsEnabled": true}
            """);
    }
}
