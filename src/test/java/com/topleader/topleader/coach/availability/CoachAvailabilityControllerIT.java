/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import com.topleader.topleader.IntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql("/sql/coach/coach-availability-test.sql")
class CoachAvailabilityControllerIT extends IntegrationTest {

    @Autowired
    private CoachAvailabilityRepository coachAvailabilityRepository;

    @Test
    @WithMockUser(username = "coach")
    void getApiIsSecured() throws Exception {

        mvc.perform(get("/api/latest/coach-availability/RECURRING"))
            .andExpect(status().isForbidden())
        ;

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getRecurringTest() throws Exception {

        mvc.perform(get("/api/latest/coach-availability/RECURRING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("TUESDAY").isArray())
            .andExpect(jsonPath("TUESDAY", hasSize(1)))
            .andExpect(jsonPath("TUESDAY[0].day", is("TUESDAY")))
            .andExpect(jsonPath("TUESDAY[0].date").doesNotExist()) // date is null
            .andExpect(jsonPath("TUESDAY[0].timeFrom", is("12:00:00")))
            .andExpect(jsonPath("TUESDAY[0].timeTo", is("13:00:00")))
            .andExpect(jsonPath("TUESDAY[0].recurring", is(true)))

            .andExpect(jsonPath("MONDAY").isArray())
            .andExpect(jsonPath("MONDAY", hasSize(1)))
            .andExpect(jsonPath("MONDAY[0].day", is("MONDAY")))
            .andExpect(jsonPath("MONDAY[0].date").doesNotExist()) // date is null
            .andExpect(jsonPath("MONDAY[0].timeFrom", is("13:00:00")))
            .andExpect(jsonPath("MONDAY[0].timeTo", is("14:00:00")))
            .andExpect(jsonPath("MONDAY[0].recurring", is(true)))
            .andDo(print())
        ;

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getNonRecurringTest() throws Exception {

        mvc.perform(get("/api/latest/coach-availability/NON_RECURRING")
                .param("firstDayOfTheWeek", "2023-08-14")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("MONDAY").isArray())
            .andExpect(jsonPath("MONDAY", hasSize(1)))
            .andExpect(jsonPath("MONDAY[0].day", is("MONDAY")))
            .andExpect(jsonPath("MONDAY[0].date", is("2023-08-14")))
            .andExpect(jsonPath("MONDAY[0].timeFrom", is("13:00:00")))
            .andExpect(jsonPath("MONDAY[0].timeTo", is("14:00:00")))
            .andExpect(jsonPath("MONDAY[0].recurring", is(false)))

            .andExpect(jsonPath("TUESDAY").isArray())
            .andExpect(jsonPath("TUESDAY", hasSize(1)))
            .andExpect(jsonPath("TUESDAY[0].day", is("TUESDAY")))
            .andExpect(jsonPath("TUESDAY[0].date", is("2023-08-15")))
            .andExpect(jsonPath("TUESDAY[0].timeFrom", is("12:00:00")))
            .andExpect(jsonPath("TUESDAY[0].timeTo", is("13:00:00")))
            .andExpect(jsonPath("TUESDAY[0].recurring", is(false)))
        ;

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getCompleteWeekTest() throws Exception {

        mvc.perform(get("/api/latest/coach-availability/ALL")
                .param("firstDayOfTheWeek", "2023-08-14")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("MONDAY").isArray())
            .andExpect(jsonPath("MONDAY", hasSize(2)))

            .andExpect(jsonPath("MONDAY[0].day", is("MONDAY")))
            .andExpect(jsonPath("MONDAY[0].date").doesNotExist()) // date is null
            .andExpect(jsonPath("MONDAY[0].timeFrom", is("13:00:00")))
            .andExpect(jsonPath("MONDAY[0].timeTo", is("14:00:00")))
            .andExpect(jsonPath("MONDAY[0].recurring", is(true)))

            .andExpect(jsonPath("MONDAY[1].day", is("MONDAY")))
            .andExpect(jsonPath("MONDAY[1].date", is("2023-08-14")))
            .andExpect(jsonPath("MONDAY[1].timeFrom", is("13:00:00")))
            .andExpect(jsonPath("MONDAY[1].timeTo", is("14:00:00")))
            .andExpect(jsonPath("MONDAY[1].recurring", is(false)))

            .andExpect(jsonPath("TUESDAY").isArray())
            .andExpect(jsonPath("TUESDAY", hasSize(2)))

            .andExpect(jsonPath("TUESDAY[0].day", is("TUESDAY")))
            .andExpect(jsonPath("TUESDAY[0].date").doesNotExist()) // date is null
            .andExpect(jsonPath("TUESDAY[0].timeFrom", is("12:00:00")))
            .andExpect(jsonPath("TUESDAY[0].timeTo", is("13:00:00")))
            .andExpect(jsonPath("TUESDAY[0].recurring", is(true)))

            .andExpect(jsonPath("TUESDAY[1].day", is("TUESDAY")))
            .andExpect(jsonPath("TUESDAY[1].date", is("2023-08-15")))
            .andExpect(jsonPath("TUESDAY[1].timeFrom", is("12:00:00")))
            .andExpect(jsonPath("TUESDAY[1].timeTo", is("13:00:00")))
            .andExpect(jsonPath("TUESDAY[1].recurring", is(false)))

        ;

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void setNonRecurringTest() throws Exception {

        mvc.perform(post("/api/latest/coach-availability/NON_RECURRING")
                .param("firstDayOfTheWeek", "2023-08-14")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                          "firstDayOfTheWeek": "2023-08-14",
                          "availabilities" : {
                            "MONDAY": [
                              {
                                "timeFrom": "01:00:00",
                                "timeTo": "02:00:00"
                              }
                            ],
                            "TUESDAY": [
                              {
                                "timeFrom": "02:00:00",
                                "timeTo": "03:00:00"
                              }
                            ]
                          }
                        }
                        """
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("TUESDAY").isArray())
            .andExpect(jsonPath("TUESDAY", hasSize(1)))
            .andExpect(jsonPath("TUESDAY[0].day", is("TUESDAY")))
            .andExpect(jsonPath("TUESDAY[0].date", is("2023-08-15")))
            .andExpect(jsonPath("TUESDAY[0].timeFrom", is("02:00:00")))
            .andExpect(jsonPath("TUESDAY[0].timeTo", is("03:00:00")))
            .andExpect(jsonPath("TUESDAY[0].recurring", is(false)))

            .andExpect(jsonPath("MONDAY").isArray())
            .andExpect(jsonPath("MONDAY", hasSize(1)))
            .andExpect(jsonPath("MONDAY[0].day", is("MONDAY")))
            .andExpect(jsonPath("MONDAY[0].date", is("2023-08-14")))
            .andExpect(jsonPath("MONDAY[0].timeFrom", is("01:00:00")))
            .andExpect(jsonPath("MONDAY[0].timeTo", is("02:00:00")))
            .andExpect(jsonPath("MONDAY[0].recurring", is(false)))
        ;

        final var changedData = coachAvailabilityRepository.findAllByUsernameAndFirstDayOfTheWeekAndRecurringIsFalse("coach", LocalDate.parse("2023-08-14"));

        assertThat(changedData, hasSize(2));

        final var unchangedData = coachAvailabilityRepository.findAllByUsernameAndFirstDayOfTheWeekAndRecurringIsFalse("coach", LocalDate.parse("2023-09-14"));

        assertThat(unchangedData, hasSize(2));

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void setRecurringTest() throws Exception {

        mvc.perform(post("/api/latest/coach-availability/RECURRING")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                          "availabilities" : {
                            "MONDAY": [
                              {
                                "timeFrom": "01:00:00",
                                "timeTo": "02:00:00"
                              }
                            ],
                            "TUESDAY": [
                              {
                                "timeFrom": "02:00:00",
                                "timeTo": "03:00:00"
                              }
                            ]
                          }
                        }
                        """
                )
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("MONDAY").isArray())
            .andExpect(jsonPath("MONDAY", hasSize(1)))
            .andExpect(jsonPath("MONDAY[0].day", is("MONDAY")))
            .andExpect(jsonPath("MONDAY[0].date").doesNotExist()) // date is null
            .andExpect(jsonPath("MONDAY[0].timeFrom", is("01:00:00")))
            .andExpect(jsonPath("MONDAY[0].timeTo", is("02:00:00")))
            .andExpect(jsonPath("MONDAY[0].recurring", is(true)))

            .andExpect(jsonPath("TUESDAY").isArray())
            .andExpect(jsonPath("TUESDAY", hasSize(1)))
            .andExpect(jsonPath("TUESDAY[0].day", is("TUESDAY")))
            .andExpect(jsonPath("TUESDAY[0].date").doesNotExist()) // date is null
            .andExpect(jsonPath("TUESDAY[0].timeFrom", is("02:00:00")))
            .andExpect(jsonPath("TUESDAY[0].timeTo", is("03:00:00")))
            .andExpect(jsonPath("TUESDAY[0].recurring", is(true)))
        ;

        final var changedData = coachAvailabilityRepository.findAllByUsernameAndRecurringIsTrue("coach");

        assertThat(changedData, hasSize(2));


    }
}
