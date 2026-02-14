/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.common.util.image.ImageUtil;
import java.time.LocalDateTime;
import java.util.Base64;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static com.topleader.topleader.TestUtils.readFileAsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/coach/coach-info-test.sql")
class CoachControllerIT extends IntegrationTest {

    @Autowired
    private CoachImageRepository coachImageRepository;

    @Autowired
    private ScheduledSessionRepository scheduledSessionRepository;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private UserRepository userRepository;

    private ScheduledSession createSession(String username, String coachUsername, LocalDateTime time) {
        var now = LocalDateTime.now();
        return new ScheduledSession()
                .setPaid(false)
                .setPrivate(false)
                .setCoachUsername(coachUsername)
                .setUsername(username)
                .setTime(time)
                .setCreatedAt(now)
                .setUpdatedAt(now);
    }

    @Test
    @WithMockUser(username = "no_coach")
    void getCoachImageNoRights() throws Exception {

        mvc.perform(get("/api/latest/coach-info/photo"))
            .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "no_coach")
    void setCoachImageNoRights() throws Exception {

        final var file = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "your-image-data".getBytes());

        mvc.perform(multipart("/api/latest/coach-info/photo")
                .file(file))
            .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void setCoachImage() throws Exception {

        // Minimal valid JPEG (1x1 pixel, no AWT dependency)
        var jpegBytes = Base64.getDecoder().decode(
                "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRof"
                + "Hh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwh"
                + "MjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAAR"
                + "CAABAAEDASIAAhEBAxEB/8QAFAABAAAAAAAAAAAAAAAAAAAACf/EABQQAQAAAAAAAAAAAAAAAAAA"
                + "AAD/xAAUAQEAAAAAAAAAAAAAAAAAAAAA/8QAFBEBAAAAAAAAAAAAAAAAAAAAAP/aAAwDAQACEQMR"
                + "AD8AKwA//9k="
        );

        final var file = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", jpegBytes);

        mvc.perform(multipart("/api/latest/coach-info/photo")
                .file(file))
            .andExpect(status().isOk());

        final var image = coachImageRepository.findByUsername("coach");

        assertThat(image.isPresent(), is(true));
        assertThat(image.get().getType(), is("image/jpeg"));

        // Verify the image can be decompressed
        var decompressed = ImageUtil.decompressImage(image.get().getImageData());
        assertThat(decompressed.length, is(jpegBytes.length));

        final var result = mvc.perform(get("/api/latest/coach-info/photo"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_JPEG))
            .andReturn();

        final var imageData = result.getResponse().getContentAsByteArray();
        assertThat(imageData.length, is(jpegBytes.length));

    }

    @Test
    @WithMockUser(username = "no_coach")
    void getCoachInfoNoRights() throws Exception {

        mvc.perform(get("/api/latest/coach-info"))
            .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "coach3", authorities = {"COACH"} )
    void publicProfileNull() throws Exception {

        mvc.perform(get("/api/latest/coach-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("publicProfile", is(false)));
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
            .andExpect(jsonPath("webLink", nullValue()))
            .andExpect(jsonPath("bio", nullValue()))
            .andExpect(jsonPath("languages", hasSize(0)))
            .andExpect(jsonPath("fields", hasSize(0)))
            .andExpect(jsonPath("experienceSince", nullValue()))
            .andExpect(jsonPath("rate", nullValue()))
            .andExpect(jsonPath("rateOrder", nullValue()))
            .andExpect(jsonPath("internalRate", nullValue()))
            .andExpect(jsonPath("linkedinProfile", nullValue()))
            .andExpect(jsonPath("freeSlots", is(false)))
            .andExpect(jsonPath("priority", is(0)))
            .andExpect(jsonPath("primaryRoles", hasItems(Coach.PrimaryRole.COACH.name())))
            .andExpect(jsonPath("certificate", hasSize(0)))
            .andExpect(jsonPath("baseLocations", hasSize(0)))
            .andExpect(jsonPath("travelWillingness", nullValue()))
            .andExpect(jsonPath("deliveryFormat", hasSize(0)))
            .andExpect(jsonPath("serviceType", hasSize(0)))
            .andExpect(jsonPath("topics", hasSize(0)))
            .andExpect(jsonPath("diagnosticTools", hasSize(0)))
            .andExpect(jsonPath("industryExperience", hasSize(0)))
            .andExpect(jsonPath("references", nullValue()))
            .andExpect(jsonPath("timeZone", nullValue()))
        ;

    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getCoachInfo() throws Exception {

        mvc.perform(get("/api/latest/coach-info"))
                .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("publicProfile", is(true)))
            .andExpect(jsonPath("firstName", is("firstName")))
            .andExpect(jsonPath("lastName", is("lastName")))
            .andExpect(jsonPath("email", is("cool123@email.cz")))
            .andExpect(jsonPath("webLink", is("http://some_video1")))
            .andExpect(jsonPath("bio", is("some bio")))
            .andExpect(jsonPath("languages", hasSize(2)))
            .andExpect(jsonPath("languages", hasItems("cz", "aj")))
            .andExpect(jsonPath("fields", hasSize(2)))
            .andExpect(jsonPath("fields", hasItems("field1", "field2")))
            .andExpect(jsonPath("experienceSince", is("2023-08-06")))
            .andExpect(jsonPath("certificate", hasItems("ACC")))
            .andExpect(jsonPath("linkedinProfile", is("http://linkedin.com/coach")))
            .andExpect(jsonPath("freeSlots", is(true)))
            .andExpect(jsonPath("priority", is(10)))
            .andExpect(jsonPath("baseLocations", hasSize(2)))
            .andExpect(jsonPath("baseLocations", hasItems("Prague", "Brno")))
            .andExpect(jsonPath("travelWillingness", is("WITHIN_COUNTRY")))
            .andExpect(jsonPath("deliveryFormat", hasSize(2)))
            .andExpect(jsonPath("deliveryFormat", hasItems("ONLINE", "HYBRID")))
            .andExpect(jsonPath("serviceType", hasSize(2)))
            .andExpect(jsonPath("serviceType", hasItems("ONE_TO_ONE", "TEAM")))
            .andExpect(jsonPath("topics", hasSize(2)))
            .andExpect(jsonPath("topics", hasItems("LEADERSHIP", "COMMUNICATION")))
            .andExpect(jsonPath("diagnosticTools", hasSize(2)))
            .andExpect(jsonPath("diagnosticTools", hasItems("MBTI", "DISC")))
            .andExpect(jsonPath("industryExperience", hasSize(2)))
            .andExpect(jsonPath("industryExperience", hasItems("TECH", "FINANCE")))
            .andExpect(jsonPath("references", is("Experienced coach")))
            .andExpect(jsonPath("timeZone", is("Europe/Prague")))
            .andExpect(jsonPath("primaryRoles", hasItems(Coach.PrimaryRole.COACH.name())))
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
            .andExpect(jsonPath("email", is("cool-test@email.cz")))
            .andExpect(jsonPath("webLink", is("http://some_video1")))
            .andExpect(jsonPath("bio", is("some bio")))
            .andExpect(jsonPath("languages", hasSize(2)))
            .andExpect(jsonPath("languages", hasItems("cz", "aj")))
            .andExpect(jsonPath("fields", hasSize(2)))
            .andExpect(jsonPath("fields", hasItems("field1", "field2")))
            .andExpect(jsonPath("experienceSince", is("2023-08-06")))
            .andExpect(jsonPath("certificate", hasItems("ACC", "PCC")))
            .andExpect(jsonPath("linkedinProfile", is("http://linkedin.com")))
            .andExpect(jsonPath("freeSlots", is(true)))
            .andExpect(jsonPath("priority", is(5)))
            .andExpect(jsonPath("baseLocations", hasSize(2)))
            .andExpect(jsonPath("baseLocations", hasItems("Prague", "Vienna")))
            .andExpect(jsonPath("travelWillingness", is("WITHIN_COUNTRY")))
            .andExpect(jsonPath("deliveryFormat", hasSize(2)))
            .andExpect(jsonPath("deliveryFormat", hasItems("ONLINE", "HYBRID")))
            .andExpect(jsonPath("serviceType", hasSize(3)))
            .andExpect(jsonPath("serviceType", hasItems("ONE_TO_ONE", "TEAM", "WORKSHOPS")))
            .andExpect(jsonPath("topics", hasSize(3)))
            .andExpect(jsonPath("topics", hasItems("LEADERSHIP", "COMMUNICATION", "EMOTIONAL_INTELLIGENCE")))
            .andExpect(jsonPath("diagnosticTools", hasSize(2)))
            .andExpect(jsonPath("diagnosticTools", hasItems("MBTI", "DISC")))
            .andExpect(jsonPath("industryExperience", hasSize(2)))
            .andExpect(jsonPath("industryExperience", hasItems("TECH", "FINANCE")))
            .andExpect(jsonPath("references", is("Great coach with 10 years of experience")))
            .andExpect(jsonPath("timeZone", is("UTC")))
            .andExpect(jsonPath("primaryRoles", hasItems(Coach.PrimaryRole.COACH.name(), Coach.PrimaryRole.MENTOR.name())))

        ;
        Assertions.assertThat(userRepository.findByEmail("cool-test@email.cz")).isPresent();
        final var coach = coachRepository.findByUsername("coach_no_info").orElseThrow();
        assertThat(coach.getRateOrder(), nullValue());
    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getCoachUpcomingSessions() throws Exception {

        final var now = LocalDateTime.now().withNano(0);

        final var user1Session1 = now.plusHours(2).withNano(0);
        final var user1Session2 = now.plusDays(2).withNano(0);

        final var id1 = scheduledSessionRepository.save(createSession("user1", "coach", user1Session1)).getId();

        final var id2 = scheduledSessionRepository.save(createSession("user1", "coach", user1Session2)).getId();

        scheduledSessionRepository.save(createSession("user1", "coach_no_info", now.plusHours(3)));

        mvc.perform(get("/api/latest/coach-info/upcoming-sessions"))
            .andExpect(status().isOk())
            .andExpect(content().json(String.format(
                """
                    [
                      {
                        "id": %s,
                        "username": "user1",
                        "firstName": "user1FirstName",
                        "lastName": "user1lastName",
                        "time": "%s"
                      },
                      {
                        "id": %s,
                        "username": "user1",
                        "firstName": "user1FirstName",
                        "lastName": "user1lastName",
                        "time": "%s"
                      }
                    ]
                    """, id1, user1Session1, id2, user1Session2
            )))
        ;
    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void deleteCoachUpcomingSessions() throws Exception {

        final var now = LocalDateTime.now().withNano(0);

        final var user1Session1 = now.plusHours(2);
        final var user1Session2 = now.plusDays(2);

        final var id1 = scheduledSessionRepository.save(createSession("user1", "coach", user1Session1)).getId();

        final var id2 = scheduledSessionRepository.save(createSession("user1", "coach", user1Session2)).getId();

        final var id3 = scheduledSessionRepository.save(createSession("user1", "coach_no_info", now.plusHours(3))).getId();

        mvc.perform(delete("/api/latest/coach-info/upcoming-sessions/" + id2))
            .andExpect(status().isOk())
        ;
        assertThat(scheduledSessionRepository.findById(id1).isPresent(), is(true));
        var cancelledSession = scheduledSessionRepository.findById(id2);
        assertThat(cancelledSession.isPresent(), is(true));
        assertThat(cancelledSession.get().getStatus(), is(ScheduledSession.Status.CANCELED_BY_COACH));
        assertThat(scheduledSessionRepository.findById(id3).isPresent(), is(true));
    }
}
