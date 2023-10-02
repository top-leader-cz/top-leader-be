/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import com.topleader.topleader.IntegrationTest;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

        mvc.perform(get("/api/latest/coach-availability/recurring"))
            .andExpect(status().isForbidden())
        ;

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getRecurringTest() throws Exception {

        mvc.perform(get("/api/latest/coach-availability/recurring"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    [
                      {
                        "from": {
                          "day": "MONDAY",
                          "time": "15:00:00"
                        },
                        "to": {
                          "day": "MONDAY",
                          "time": "16:00:00"
                        }
                      },
                      {
                        "from": {
                          "day": "TUESDAY",
                          "time": "15:00:00"
                        },
                        "to": {
                          "day": "TUESDAY",
                          "time": "16:00:00"
                        }
                      }
                    ]
                """))
        ;

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getNonRecurringTest() throws Exception {

        mvc.perform(get("/api/latest/coach-availability/non-recurring")
                .param("from", "2023-08-14T00:00:00")
                .param("to", "2023-08-16T00:00:00")
            )
            .andDo(print())
            .andExpect(content().json("""
                [
                  {
                    "from": "2023-08-14T15:00:00",
                    "to": "2023-08-14T16:00:00"
                  },
                  {
                    "from": "2023-08-15T14:00:00",
                    "to": "2023-08-15T16:00:00"
                  }
                ]"""))
        ;

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getNonRecurringSingleItemTest() throws Exception {

        mvc.perform(get("/api/latest/coach-availability/non-recurring")
                .param("from", "2023-08-14T14:59:00")
                .param("to", "2023-08-14T16:01:00")
            )
            .andExpect(content().json("""
                [
                  {
                    "from": "2023-08-14T15:00:00",
                    "to": "2023-08-14T16:00:00"
                  }
                ]"""))
        ;

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void setNonRecurringTest() throws Exception {

        mvc.perform(post("/api/latest/coach-availability/non-recurring")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                          "timeFrame": {
                            "from": "2023-08-14T00:00:00",
                            "to": "2023-08-16T00:00:00"
                          },
                          "events": [
                              {
                                "from": "2023-08-14T16:00:00",
                                "to": "2023-08-14T18:00:00"
                              },
                              {
                                "from": "2023-08-15T14:00:00",
                                "to": "2023-08-15T16:00:00"
                              }
                          ]
                        }
                        """
                )
            )
            .andExpect(status().isOk())
        ;

        final var events = coachAvailabilityRepository.findAllByUsernameAndDateTimeFromAfterAndDateTimeToBefore(
                "coach",
                LocalDateTime.parse("2023-08-14T00:00:00"),
                LocalDateTime.parse("2023-08-16T00:00:00")
            ).stream()
            .sorted(Comparator.comparingLong(CoachAvailability::getId))
            .toList();

        assertThat(events, hasSize(2));
        assertThat(events.get(0).getRecurring(), is(false));
        assertThat(events.get(0).getDateTimeFrom(), is(LocalDateTime.parse("2023-08-15T12:00:00")));
        assertThat(events.get(0).getDateTimeTo(), is(LocalDateTime.parse("2023-08-15T14:00:00")));
        assertThat(events.get(1).getRecurring(), is(false));
        assertThat(events.get(1).getDateTimeFrom(), is(LocalDateTime.parse("2023-08-14T14:00:00")));
        assertThat(events.get(1).getDateTimeTo(), is(LocalDateTime.parse("2023-08-14T16:00:00")));

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void setRecurringTest() throws Exception {

        mvc.perform(post("/api/latest/coach-availability/recurring")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        [
                          {
                            "from": {
                              "day": "MONDAY",
                              "time": "15:00:00"
                            },
                            "to": {
                              "day": "MONDAY",
                              "time": "16:00:00"
                            }
                          },
                          {
                            "from": {
                              "day": "TUESDAY",
                              "time": "12:00:00"
                            },
                            "to": {
                              "day": "TUESDAY",
                              "time": "14:00:00"
                            }
                          }
                        ]
                            """
                )
            )
            .andExpect(status().isOk())
        ;

        final var events = coachAvailabilityRepository.findAllByUsernameAndRecurringIsTrue("coach")
            .stream()
            .sorted(Comparator.comparingLong(CoachAvailability::getId))
            .toList();

        assertThat(events, hasSize(2));
        assertThat(events.get(0).getRecurring(), is(true));
        assertThat(events.get(0).getDayFrom(), is(DayOfWeek.MONDAY));
        assertThat(events.get(0).getTimeFrom(), is(LocalTime.parse("13:00:00")));
        assertThat(events.get(0).getDayTo(), is(DayOfWeek.MONDAY));
        assertThat(events.get(0).getTimeTo(), is(LocalTime.parse("14:00:00")));
        assertThat(events.get(1).getRecurring(), is(true));
        assertThat(events.get(1).getDayFrom(), is(DayOfWeek.TUESDAY));
        assertThat(events.get(1).getTimeFrom(), is(LocalTime.parse("10:00:00")));
        assertThat(events.get(1).getDayTo(), is(DayOfWeek.TUESDAY));
        assertThat(events.get(1).getTimeTo(), is(LocalTime.parse("12:00:00")));


    }
}
