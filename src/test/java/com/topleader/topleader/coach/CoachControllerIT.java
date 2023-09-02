/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static com.topleader.topleader.TestUtils.readFileAsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/coach/coach-info-test.sql")
class CoachControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(username = "no_coach")
    void getCoachInfoNoRights() throws Exception {

        mvc.perform(get("/api/latest/coach-info"))
            .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "no_coach", authorities = {"COACH"})
    void getCoachInfoEmpty() throws Exception {

        mvc.perform(get("/api/latest/coach-info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("publicProfile", is(false)))
            .andExpect(jsonPath("firstName", nullValue()))
            .andExpect(jsonPath("lastName", nullValue()))
            .andExpect(jsonPath("email", nullValue()))
            .andExpect(jsonPath("photo", nullValue()))
            .andExpect(jsonPath("bio", nullValue()))
            .andExpect(jsonPath("languages", hasSize(0)))
            .andExpect(jsonPath("fields", hasSize(0)))
            .andExpect(jsonPath("experienceSince", nullValue()))
            .andExpect(jsonPath("rate", nullValue()))
            ;

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getCoachInfo() throws Exception {

        mvc.perform(get("/api/latest/coach-info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("publicProfile", is(true)))
            .andExpect(jsonPath("firstName", is("firstName")))
            .andExpect(jsonPath("lastName", is("lastName")))
            .andExpect(jsonPath("email", is("cool@email.cz")))
            .andExpect(jsonPath("photo", nullValue()))
            .andExpect(jsonPath("bio", is("some bio")))
            .andExpect(jsonPath("languages", hasSize(2)))
            .andExpect(jsonPath("languages", hasItems("cz", "aj")))
            .andExpect(jsonPath("fields", hasSize(2)))
            .andExpect(jsonPath("fields", hasItems("field1", "field2")))
            .andExpect(jsonPath("experienceSince", is("2023-08-06")))
            .andExpect(jsonPath("rate", is("$$$")))
            ;

    }

    @Test
    @WithMockUser(username = "coach_no_info")
    void setCoachInfoNoRights() throws Exception {

        mvc.perform(post("/api/latest/coach-info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(readFileAsString("json/coach/set-coach-info-request.json"))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "coach_no_info", authorities = {"COACH"})
    void setCoachInfo() throws Exception {

        mvc.perform(post("/api/latest/coach-info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(readFileAsString("json/coach/set-coach-info-request.json"))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("publicProfile", is(true)))
            .andExpect(jsonPath("firstName", is("firstName")))
            .andExpect(jsonPath("lastName", is("lastName")))
            .andExpect(jsonPath("email", is("cool@email.cz")))
            .andExpect(jsonPath("photo", nullValue()))
            .andExpect(jsonPath("bio", is("some bio")))
            .andExpect(jsonPath("languages", hasSize(2)))
            .andExpect(jsonPath("languages", hasItems("cz", "aj")))
            .andExpect(jsonPath("fields", hasSize(2)))
            .andExpect(jsonPath("fields", hasItems("field1", "field2")))
            .andExpect(jsonPath("experienceSince", is("2023-08-06")))
            .andExpect(jsonPath("rate", is("$$$")))
        ;
    }
}
