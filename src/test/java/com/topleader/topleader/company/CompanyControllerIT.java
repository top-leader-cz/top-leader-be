package com.topleader.topleader.company;

import com.topleader.topleader.IntegrationTest;
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
        mvc.perform(get("/api/latest/companies"))
            .andExpect(status().isOk())
            .andExpect(content().json(
                """
                    [
                      {
                        "id": 1,
                        "name": "company1"
                      },
                      {
                        "id": 2,
                        "name": "company2"
                      }
                    ]
                    """
            ))
            .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createCompanyTest() throws Exception {
        mvc.perform(post("/api/latest/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "company3"
                    }
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(
                """
                    {
                        "id": 1,
                        "name": "company3"
                      }
                    """
            ));
    }
    @Test
    @WithMockUser(authorities = "ADMIN")
    void createCompany_nameAlreadyUsedTest() throws Exception {
        mvc.perform(post("/api/latest/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "company1"
                    }
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().json(
                """
                    [
                      {
                        "errorCode": "already.existing",
                        "fields": [
                          {
                            "name": "name",
                            "value": "company1"
                          }
                        ],
                        "errorMessage": "Company with this name already exists"
                      }
                    ]
                    """
            ));
    }
}