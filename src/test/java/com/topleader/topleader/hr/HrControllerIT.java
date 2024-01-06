/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/hr/hr-users.sql")
class HrControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(username = "hrUser", authorities = "HR")
    void testListUsersEndpoint() throws Exception {

        mvc.perform(get("/api/latest/hr-users"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json(
                """
                    [
                      {
                        "firstName": "John",
                        "lastName": "Doe",
                        "username": "hrUser",
                        "coach": "Coach1",
                        "credit": 100,
                        "requestedCredit": 0,
                        "scheduledCredit": 10,
                        "paidCredit": 111,
                        "state": "PENDING"
                      },
                      {
                        "firstName": "Alice",
                        "lastName": "Smith",
                        "username": "user1",
                        "coach": "Coach2",
                        "credit": 50,
                        "requestedCredit": 10,
                        "scheduledCredit": 20,
                        "paidCredit": 222,
                        "state": "AUTHORIZED"
                      }
                    ]
                    """
            ))
        ;
    }
    @Test
    @WithMockUser(username = "user1", authorities = "USER")
    void testListUsersNoHrEndpoint() throws Exception {

        mvc.perform(get("/api/latest/hr-users"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json(
                """
                    [
                      {
                        "firstName": "Alice",
                        "lastName": "Smith",
                        "username": "user1",
                        "coach": "Coach2",
                        "credit": 50,
                        "requestedCredit": 10,
                        "scheduledCredit": 20,
                        "state": "AUTHORIZED"
                      }
                    ]
                    """))
        ;
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = "HR")
    void testInviteUserEndpoint() throws Exception {

        mvc.perform(post("/api/latest/hr-users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "user4",
                        "firstName": "Dan",
                        "lastName": "Aaa",
                        "isTrial": true,
                        "locale": "en"
                    }
                    """
                ))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("{\"username\":\"user4\",\"coach\":null,\"credit\":0,\"requestedCredit\":null,\"state\":\"PENDING\"}"))
        ;
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = "HR")
    void testRequestCredits() throws Exception {

        mvc.perform(post("/api/latest/hr-users/user1/credit-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "credit": 1000
                    }
                    """
                ))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("{\"username\":\"user1\",\"coach\":\"Coach2\",\"credit\":50,\"requestedCredit\":1000,\"state\":\"AUTHORIZED\"}"))
        ;
    }

    @Test
    @WithMockUser(username = "user2", authorities = "USER")
    void testRequestCreditsNoRights() throws Exception {

        mvc.perform(post("/api/latest/hr-users/user1/credit-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "credit": 1000
                    }
                    """
                ))
            .andExpect(status().isForbidden())
        ;
    }

    @Test
    @WithMockUser(username = "user1", authorities = "USER")
    void testRequestCreditsToHimself() throws Exception {

        mvc.perform(post("/api/latest/hr-users/user1/credit-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "credit": 1000
                    }
                    """
                ))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("{\"username\":\"user1\",\"coach\":\"Coach2\",\"credit\":50,\"requestedCredit\":1000,\"state\":\"AUTHORIZED\"}"))
        ;
    }
}
