/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.scheduled_session.ScheduledSession;
import com.topleader.topleader.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.util.image.ImageUtil;
import java.time.LocalDateTime;
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

        final var file = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "image-data".getBytes());

        mvc.perform(multipart("/api/latest/coach-info/photo")
                .file(file))
            .andExpect(status().isOk());

        final var image = coachImageRepository.findById("coach");

        assertThat(image.isPresent(), is(true));
        assertThat(image.get().getType(), is("image/jpeg"));
        assertThat(new String(ImageUtil.decompressImage(image.get().getImageData())), is("image-data"));


        final var result = mvc.perform(get("/api/latest/coach-info/photo"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_JPEG))
            .andReturn();

        final var imageData = new String(result.getResponse().getContentAsByteArray());

        assertThat(imageData, is("image-data"));

    }

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
            .andExpect(jsonPath("webLink", nullValue()))
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
            .andExpect(jsonPath("webLink", is("http://some_video1")))
            .andExpect(jsonPath("bio", is("some bio")))
            .andExpect(jsonPath("languages", hasSize(2)))
            .andExpect(jsonPath("languages", hasItems("cz", "aj")))
            .andExpect(jsonPath("fields", hasSize(2)))
            .andExpect(jsonPath("fields", hasItems("field1", "field2")))
            .andExpect(jsonPath("experienceSince", is("2023-08-06")))
            .andExpect(jsonPath("rate", is("$$$")))
            .andExpect(jsonPath("timeZone", is("Europe/Prague")))
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
            .andExpect(jsonPath("webLink", is("http://some_video1")))
            .andExpect(jsonPath("bio", is("some bio")))
            .andExpect(jsonPath("languages", hasSize(2)))
            .andExpect(jsonPath("languages", hasItems("cz", "aj")))
            .andExpect(jsonPath("fields", hasSize(2)))
            .andExpect(jsonPath("fields", hasItems("field1", "field2")))
            .andExpect(jsonPath("experienceSince", is("2023-08-06")))
            .andExpect(jsonPath("rate", is("$$$")))
            .andExpect(jsonPath("timeZone", is("UTC")))
        ;
    }

    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getCoachUpcomingSessions() throws Exception {

        final var now = LocalDateTime.now().withNano(0);

        final var user1Session1 = now.plusHours(2);
        final var user1Session2 = now.plusDays(2);

        final var id1 = scheduledSessionRepository.save(new ScheduledSession()
            .setPaid(false)
            .setPrivate(false)
            .setCoachUsername("coach")
            .setTime(user1Session1)
            .setUsername("user1")
        ).getId();

        final var id2 = scheduledSessionRepository.save(new ScheduledSession()
            .setPaid(false)
            .setPrivate(false)
            .setCoachUsername("coach")
            .setTime(user1Session2)
            .setUsername("user1")
        ).getId();

        final var id3 = scheduledSessionRepository.save(new ScheduledSession()
            .setPaid(false)
            .setPrivate(false)
            .setCoachUsername("coach_no_info")
            .setTime(now.plusHours(3))
            .setUsername("user1")
        ).getId();

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

        final var id1 = scheduledSessionRepository.save(new ScheduledSession()
            .setPaid(false)
            .setCoachUsername("coach")
            .setTime(user1Session1)
            .setUsername("user1")
        ).getId();

        final var id2 = scheduledSessionRepository.save(new ScheduledSession()
            .setPaid(false)
            .setCoachUsername("coach")
            .setTime(user1Session2)
            .setUsername("user1")
        ).getId();

        final var id3 = scheduledSessionRepository.save(new ScheduledSession()
            .setPaid(false)
            .setCoachUsername("coach_no_info")
            .setTime(now.plusHours(3))
            .setUsername("user1")
        ).getId();

        mvc.perform(delete("/api/latest/coach-info/upcoming-sessions/" + id2))
            .andExpect(status().isOk())
        ;
        assertThat(scheduledSessionRepository.findById(id1).isPresent(), is(true));
        assertThat(scheduledSessionRepository.findById(id2).isPresent(), is(false));
        assertThat(scheduledSessionRepository.findById(id3).isPresent(), is(true));
    }
}
