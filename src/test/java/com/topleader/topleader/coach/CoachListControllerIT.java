/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.IntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


/**
 * @author Daniel Slavik
 */
@Sql("/sql/coach/coach-list-test.sql")
class CoachListControllerIT extends IntegrationTest {

    @Test
    @WithMockUser
    void searchByFirstNameTest() throws Exception {

        mvc.perform(post("/api/latest/coaches")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "page": {},
                    "name": "Mich"
                }
                """)
        )
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username", is("coach3")))
        ;
    }

    @Test
    @WithMockUser
    void searchByLastNameTest() throws Exception {

        mvc.perform(post("/api/latest/coaches")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "page": {},
                    "name": "Sm"
                }
                """)
        )
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username", is("coach2")))
        ;
    }

    @Test
    @WithMockUser
    void searchByLanguagesTest() throws Exception {

        mvc.perform(post("/api/latest/coaches")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "page": {},
                    "languages": ["French", "Unknown"]
                }
                """)
        )
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username", is("coach1")))
        ;
    }

    @Test
    @WithMockUser
    void searchByFieldsTest() throws Exception {

        mvc.perform(post("/api/latest/coaches")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "page": {},
                    "fields": ["Yoga", "Unknown"]
                }
                """)
        )
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username", is("coach2")))
        ;
    }

    @Test
    @WithMockUser
    void searchByPricesTest() throws Exception {

        mvc.perform(post("/api/latest/coaches")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "page": {},
                    "prices": ["$", "Unknown"]
                }
                """)
        )
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username", is("coach1")))
        ;
    }

    @Test
    @WithMockUser
    void searchByExpFromTest() throws Exception {

        final var expFrom = LocalDate.now().getYear() - 2018;

        mvc.perform(post("/api/latest/coaches")
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("""
                {
                    "page": {},
                    "experienceFrom": %s
                }
                """, expFrom))
        )
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username", is("coach2")))
        ;
    }

    @Test
    @WithMockUser
    void searchByExpToTest() throws Exception {

        final var expTo = LocalDate.now().getYear() - 2021;

        mvc.perform(post("/api/latest/coaches")
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("""
                {
                    "page": {},
                    "experienceTo": %s
                }
                """, expTo))
        )
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username", is("coach1")))
        ;
    }
    @Test
    @WithMockUser
    void searchByExpBetweenTest() throws Exception {

        final var expFrom = LocalDate.now().getYear() - 2018;
        final var expTo = LocalDate.now().getYear() - 2020;

        mvc.perform(post("/api/latest/coaches")
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("""
                {
                    "page": {},
                    "experienceFrom": %s,
                    "experienceTo": %s
                }
                """, expFrom, expTo))
        )
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username", is("coach3")))
        ;
    }

    @Test
    @WithMockUser
    void mappingTest() throws Exception {

        final var exp = LocalDate.now().getYear() - 2019;

        mvc.perform(post("/api/latest/coaches")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "page": {},
                    "name": "Mich"
                }
                """)
            )
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username", is("coach3")))
            .andExpect(jsonPath("$.content[0].firstName", is("Michael")))
            .andExpect(jsonPath("$.content[0].lastName", is("Johnson")))
            .andExpect(jsonPath("$.content[0].email", is("michael.johnson@example.com")))
            .andExpect(jsonPath("$.content[0].bio", is("Certified fitness coach")))
            .andExpect(jsonPath("$.content[0].experience", is(exp)))
            .andExpect(jsonPath("$.content[0].rate", is("$$$")))
        ;
    }
}
