/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.coach.availability.CoachAvailability;
import com.topleader.topleader.coach.availability.CoachAvailabilityRepository;
import com.topleader.topleader.credit.history.CreditHistory;
import com.topleader.topleader.credit.history.CreditHistoryRepository;
import com.topleader.topleader.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.util.image.ImageUtil;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

import jnr.constants.platform.Local;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditHistoryRepository creditHistoryRepository;

    private ZonedDateTime testedTime = ZonedDateTime.of(LocalDateTime.of(
            LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)),
            LocalTime.of(9, 0)
    ), ZoneId.of("UTC"));

    ZonedDateTime startTime = testedTime.minusHours(1);
    ZonedDateTime endTime = testedTime;

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        super.setUp();

        mockServer.stubFor(WireMock.get(urlMatching("/scheduled_events\\?user=.*")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(String.format("""
                        {
                            "collection": [
                                {
                                    "id": "event1",
                                    "start_time": "%s",
                                    "end_time": "%s"
                                },
                                {
                                    "id": "event2",
                                    "start_time": "2023-08-14T13:00:00Z",
                                    "end_time": "2023-08-14T14:00:00Z"
                                }
                            ]
                        }
                        """, startTime, endTime))));

        mockServer.stubFor(WireMock.post(urlEqualTo("/oauth/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.readFileAsString("json/coach/calendly-token-response.json"))));
    }

    @Test
    @WithMockUser
    void scheduleDefaultCoachEventTest() throws Exception {
        final var scheduleTime = LocalDateTime.of(
                LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)),
                LocalTime.of(9, 0)
        );

       coachAvailabilityRepository.save(
                new CoachAvailability()
                        .setUsername("coach1")
                        .setDateTimeFrom(LocalDateTime.of(scheduleTime.toLocalDate(), LocalTime.of(8, 0)))
                        .setDateTimeTo(LocalDateTime.of(scheduleTime.toLocalDate(), LocalTime.of(10, 0)))
                        .setRecurring(false)
        );

        mvc.perform(post("/api/latest/coaches/coach1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "time": "%s"
                                }
                                """, scheduleTime))
                )
                .andExpect(status().isOk());

        final var sessions = scheduledSessionRepository.findAll();

        assertThat(sessions, hasSize(1));

        final var session = sessions.get(0);

        assertThat(session.getCoachUsername(), is("coach1"));
        assertThat(session.getUsername(), is("user"));
        assertThat(session.getTime(), is(scheduleTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()));

        final var user = userRepository.findById("user").orElseThrow();

        assertThat(user.getScheduledCredit(), is(110));
        assertThat(user.getCredit(), is(400));

        final var creditHistory = creditHistoryRepository.findAll();

        assertThat(creditHistory, hasSize(1));

        final var creditHistoryEvent = creditHistory.get(0);

        assertThat(creditHistoryEvent.getCredit(), is(110));
        assertThat(creditHistoryEvent.getUsername(), is("user"));
        assertThat(creditHistoryEvent.getType(), is(CreditHistory.Type.SCHEDULED));

        var receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("top-leader");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("coach1");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Upozornění na novou rezervaci na TopLeader");
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(body)
                .contains("http://app-test-url")
                .contains("<b>John Doe</b>");
    }

    @Test
    @WithMockUser
    void scheduleEventTest() throws Exception {

        final var scheduleTime = LocalDateTime.of(
                LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)),
                LocalTime.of(9, 0)
        );

        coachAvailabilityRepository.save(
                new CoachAvailability()
                        .setUsername("coach1")
                        .setDateTimeFrom(LocalDateTime.of(scheduleTime.toLocalDate(), LocalTime.of(8, 0)))
                        .setDateTimeTo(LocalDateTime.of(scheduleTime.toLocalDate(), LocalTime.of(10, 0)))
                        .setRecurring(false)
        );

        mvc.perform(post("/api/latest/coaches/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "time": "%s"
                                }
                                """, scheduleTime))
                )
                .andExpect(status().isOk());

        final var sessions = scheduledSessionRepository.findAll();

        assertThat(sessions, hasSize(1));

        final var session = sessions.get(0);

        assertThat(session.getCoachUsername(), is("coach1"));
        assertThat(session.getUsername(), is("user"));
        assertThat(session.getTime(), is(scheduleTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()));

        final var user = userRepository.findById("user").orElseThrow();

        assertThat(user.getScheduledCredit(), is(110));
        assertThat(user.getCredit(), is(400));

        final var creditHistory = creditHistoryRepository.findAll();

        assertThat(creditHistory, hasSize(1));

        final var creditHistoryEvent = creditHistory.get(0);

        assertThat(creditHistoryEvent.getCredit(), is(110));
        assertThat(creditHistoryEvent.getUsername(), is("user"));
        assertThat(creditHistoryEvent.getType(), is(CreditHistory.Type.SCHEDULED));

        var receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("top-leader");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("coach1");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Upozornění na novou rezervaci na TopLeader");
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(body)
                .contains("http://app-test-url")
                .contains("<b>John Doe</b>");
    }

    @Test
    @WithMockUser("no-credit-user")
    void scheduleEventNoCreditTest() throws Exception {

        final var scheduleTime = LocalDateTime.of(
                LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)),
                LocalTime.of(9, 0)
        );

        coachAvailabilityRepository.save(
                new CoachAvailability()
                        .setUsername("coach2")
                        .setDateTimeFrom(LocalDateTime.of(scheduleTime.toLocalDate(), LocalTime.of(8, 0)))
                        .setDateTimeTo(LocalDateTime.of(scheduleTime.toLocalDate(), LocalTime.of(10, 0)))
                        .setRecurring(false)
        );

        mvc.perform(post("/api/latest/coaches/coach2/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "time": "%s"
                                }
                                """, scheduleTime))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        [
                          {
                            "errorCode": "not.enough.credits",
                            "fields": [
                              {
                                "name": "user",
                                "value": "no-credit-user"
                              }
                            ],
                            "errorMessage": "User does not have enough credit"
                          }
                        ]
                        """))
        ;

        final var sessions = scheduledSessionRepository.findAll();

        assertThat(sessions, hasSize(0));

        final var user = userRepository.findById("no-credit-user").orElseThrow();

        assertThat(user.getScheduledCredit(), is(400));
        assertThat(user.getCredit(), is(400));

        final var creditHistory = creditHistoryRepository.findAll();

        assertThat(creditHistory, hasSize(0));
    }

    @Test
    @WithMockUser("no-credit-user-free-coach")
    void scheduleEventNoCreditFreeCoachTest() throws Exception {

        final var scheduleTime = LocalDateTime.of(
                LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)),
                LocalTime.of(9, 0)
        );

        coachAvailabilityRepository.save(
                new CoachAvailability()
                        .setUsername("coach3")
                        .setDateTimeFrom(LocalDateTime.of(scheduleTime.toLocalDate(), LocalTime.of(8, 0)))
                        .setDateTimeTo(LocalDateTime.of(scheduleTime.toLocalDate(), LocalTime.of(10, 0)))
                        .setRecurring(false)
        );

        mvc.perform(post("/api/latest/coaches/coach3/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "time": "%s"
                                }
                                """, scheduleTime))
                )
                .andExpect(status().isOk())
        ;

        final var sessions = scheduledSessionRepository.findAll();

        assertThat(sessions, hasSize(1));

        final var session = sessions.get(0);

        assertThat(session.getCoachUsername(), is("coach3"));
        assertThat(session.getUsername(), is("no-credit-user-free-coach"));
        assertThat(session.getTime(), is(scheduleTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()));

        final var user = userRepository.findById("no-credit-user-free-coach").orElseThrow();

        assertThat(user.getScheduledCredit(), is(400));
        assertThat(user.getCredit(), is(400));

        final var creditHistory = creditHistoryRepository.findAll();

        assertThat(creditHistory, hasSize(0));
    }

    @Test
    @WithMockUser
    void getCoachAvailabilityTest() throws Exception {

        mvc.perform(get("/api/latest/coaches/coach1/availability")
                        .param("from", "2023-08-14T00:00:00")
                        .param("to", "2023-08-14T23:59:00")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json("""
                        ["2023-08-14T10:00:00","2023-08-14T11:00:00"]
                        """))
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
                                    "name": "mich"
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
                .andExpect(jsonPath("$.content[0].timeZone", is("UTC")))
                .andExpect(jsonPath("$.content[0].webLink", is("http://some_video3")))
        ;
    }

    @Test
    @WithMockUser
    void singleCoachTest() throws Exception {

        final var exp = LocalDate.now().getYear() - 2019;

        mvc.perform(get("/api/latest/coaches/coach3"))
                .andDo(print())
                .andExpect(jsonPath("$.username", is("coach3")))
                .andExpect(jsonPath("$.firstName", is("Michael")))
                .andExpect(jsonPath("$.lastName", is("Johnson")))
                .andExpect(jsonPath("$.email", is("michael.johnson@example.com")))
                .andExpect(jsonPath("$.bio", is("Certified fitness coach")))
                .andExpect(jsonPath("$.experience", is(exp)))
                .andExpect(jsonPath("$.rate", is("$$$")))
                .andExpect(jsonPath("$.webLink", is("http://some_video3")))
                .andExpect(jsonPath("$.linkedinProfile", is("http://linkac")))
        ;
    }

    @Test
    @WithMockUser("user-with-filter")
    void coachMaxRateTest() throws Exception {

        mvc.perform(post("/api/latest/coaches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "page": {}
                                }
                                """)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
        ;
    }
}
