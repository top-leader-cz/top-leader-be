/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.coach.availability.CoachAvailability;
import com.topleader.topleader.coach.availability.CoachAvailabilityRepository;
import com.topleader.topleader.coach.availability.DayType;
import com.topleader.topleader.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.util.image.ImageUtil;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql("/sql/coach/coach-list-test.sql")
class CoachListControllerIT extends IntegrationTest {

    @Autowired
    private CoachImageRepository coachImageRepository;

    @Autowired
    private CoachAvailabilityRepository coachAvailabilityRepository;

    @Autowired
    private ScheduledSessionRepository scheduledSessionRepository;

    @Test
    @WithMockUser
    void scheduleEventTest() throws Exception {

        final var nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        coachAvailabilityRepository.save(
            new CoachAvailability()
                .setUsername("coach1")
                .setDay(DayType.MONDAY)
                .setDate(nextMonday)
                .setTimeFrom(LocalTime.of(10, 0))
                .setTimeTo(LocalTime.of(12, 0))
                .setRecurring(false)
                .setFirstDayOfTheWeek(nextMonday)
        );

        mvc.perform(post("/api/latest/coaches/coach1/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                    {
                        "firstDayOfTheWeek": "%s",
                        "day": "MONDAY",
                        "time": "11:00:00"
                    }
                    """, nextMonday))
            )
            .andDo(print())
            .andExpect(status().isOk());

        final var sessions = scheduledSessionRepository.findAll();

        assertThat(sessions, hasSize(1));

        final var session = sessions.get(0);

        assertThat(session.getCoachUsername(), is("coach1"));
        assertThat(session.getUsername(), is("user"));
        assertThat(session.getTime(), is(LocalDateTime.of(nextMonday, LocalTime.of(11, 0))));
        assertThat(session.getFirstDayOfTheWeek(), is(nextMonday));
    }

    @Test
    @WithMockUser
    void getCompleteWeekTest() throws Exception {

        mvc.perform(get("/api/latest/coaches/coach1/availability")
                .param("firstDayOfTheWeek", "2023-08-14")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "MONDAY": [
                    {
                      "day": "MONDAY",
                      "date": "2023-08-14",
                      "timeFrom": "13:00:00",
                      "timeTo": "14:00:00",
                      "firstDayOfTheWeek": "2023-08-14"
                    },
                    {
                      "day": "MONDAY",
                      "date": "2023-08-14",
                      "timeFrom": "14:00:00",
                      "timeTo": "15:00:00",
                      "firstDayOfTheWeek": "2023-08-14"
                    },
                    {
                      "day": "MONDAY",
                      "date": "2023-08-14",
                      "timeFrom": "12:00:00",
                      "timeTo": "13:00:00",
                      "firstDayOfTheWeek": "2023-08-14"
                    }
                  ],
                  "TUESDAY": [
                    {
                      "day": "TUESDAY",
                      "date": "2023-08-15",
                      "timeFrom": "12:00:00",
                      "timeTo": "13:00:00",
                      "firstDayOfTheWeek": "2023-08-14"
                    },
                    {
                      "day": "TUESDAY",
                      "date": "2023-08-15",
                      "timeFrom": "13:00:00",
                      "timeTo": "14:00:00",
                      "firstDayOfTheWeek": "2023-08-14"
                    }
                  ]
                }
                """))
        ;
    }

    @Test
    @WithMockUser
    void getCompleteWeekTest_empty() throws Exception {

        mvc.perform(get("/api/latest/coaches/coach2/availability")
                .param("firstDayOfTheWeek", "2023-08-14")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isMap())
            .andExpect(jsonPath("$").isEmpty())

        ;
    }


    @Test
    @WithMockUser
    void getCoachPhoto() throws Exception {

        coachImageRepository.save(
            new CoachImage()
                .setUsername("coach1")
                .setType(MediaType.IMAGE_PNG_VALUE)
                .setImageData(ImageUtil.compressImage("image-data".getBytes()))
        );

        final var result = mvc.perform(get("/api/latest/coaches/coach1/photo"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andReturn();

        final var imageData = new String(result.getResponse().getContentAsByteArray());

        assertThat(imageData, is("image-data"));
    }

    @Test
    @WithMockUser
    void getCoachPhotoNotFound() throws Exception {

        mvc.perform(get("/api/latest/coaches/coach1/photo"))
            .andExpect(status().isNotFound())
        ;
    }

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

        final var expFrom = LocalDate.now().getYear() - 2020;
        final var expTo = LocalDate.now().getYear() - 2018;

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

    @Test
    @WithMockUser
    void singleCoachTest() throws Exception {

        final var exp = LocalDate.now().getYear() - 2019;

        mvc.perform(get("/api/latest/coaches/coach3"))
            .andExpect(jsonPath("$.username", is("coach3")))
            .andExpect(jsonPath("$.firstName", is("Michael")))
            .andExpect(jsonPath("$.lastName", is("Johnson")))
            .andExpect(jsonPath("$.email", is("michael.johnson@example.com")))
            .andExpect(jsonPath("$.bio", is("Certified fitness coach")))
            .andExpect(jsonPath("$.experience", is(exp)))
            .andExpect(jsonPath("$.rate", is("$$$")))
        ;
    }
}
