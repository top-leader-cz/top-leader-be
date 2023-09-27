/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/admin/admin-view-data.sql")
class AdminViewControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void adminListTest() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("size", "1")
                .param("sort", "username,asc")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "content": [
                    {
                      "username": "coach1",
                      "firstName": "Jane",
                      "lastName": "Smith",
                      "authorities": [
                        "COACH",
                        "USER"
                      ],
                      "timeZone": "GMT",
                      "status": "PENDING",
                      "companyId": 2,
                      "companyName": "Company 2",
                      "coach": "coach1",
                      "coachFirstName": "Jane",
                      "coachLastName": "Smith",
                      "credit": 150,
                      "requestedCredit": 75,
                      "isTrial": true
                    }
                  ],
                  "pageable": {
                    "sort": {
                      "empty": false,
                      "sorted": true,
                      "unsorted": false
                    },
                    "offset": 0,
                    "pageNumber": 0,
                    "pageSize": 1,
                    "unpaged": false,
                    "paged": true
                  },
                  "totalPages": 7,
                  "totalElements": 7,
                  "last": false,
                  "size": 1,
                  "number": 0,
                  "sort": {
                    "empty": false,
                    "sorted": true,
                    "unsorted": false
                  },
                  "numberOfElements": 1,
                  "first": true,
                  "empty": false
                }
                """))
            ;
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByLastName() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("lastName", "Doe"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].lastName").value("Doe"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByTimeZone() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("timeZone", "UTC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].timeZone").value("UTC"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByStatus() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("status", "AUTHORIZED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].status").value("AUTHORIZED"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByCompanyId() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("companyId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].companyId").value(1));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByCredit() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("credit", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].credit").value(100));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByRequestedCredit() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("requestedCredit", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].requestedCredit").value(50));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByIsTrial() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("isTrial", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].isTrial").value(true));
    }
}
