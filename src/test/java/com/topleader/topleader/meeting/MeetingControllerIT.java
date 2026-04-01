package com.topleader.topleader.meeting;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.common.meeting.MeetingInfoRepository;
import com.topleader.topleader.common.meeting.domain.MeetingInfo;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/meeting/meeting-test.sql")
class MeetingControllerIT extends IntegrationTest {

    @Autowired
    private MeetingInfoRepository meetingInfoRepository;

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void getSettingsConnectedTest() {
        mvc.perform(get("/api/latest/meeting"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "provider": "GOOGLE",
                            "email": "meet-coach@gmail.com",
                            "autoGenerate": true,
                            "status": "OK"
                        }
                        """));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-user", authorities = {"USER", "COACH"})
    void getSettingsNotConnectedTest() {
        mvc.perform(get("/api/latest/meeting"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "provider": null,
                            "email": null,
                            "autoGenerate": false,
                            "status": null
                        }
                        """));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void updateAutoGenerateTest() {
        mvc.perform(patch("/api/latest/meeting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"autoGenerate": false}
                                """))
                .andExpect(status().isOk());

        var info = meetingInfoRepository.findByUsername("meet-coach").orElseThrow();
        assertThat(info.isAutoGenerate(), is(false));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void updateAutoGenerateToggleBackTest() {
        meetingInfoRepository.updateAutoGenerate("meet-coach", false);

        mvc.perform(patch("/api/latest/meeting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"autoGenerate": true}
                                """))
                .andExpect(status().isOk());

        var info = meetingInfoRepository.findByUsername("meet-coach").orElseThrow();
        assertThat(info.isAutoGenerate(), is(true));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-user", authorities = {"USER", "COACH"})
    void updateAutoGenerateNoProviderTest() {
        mvc.perform(patch("/api/latest/meeting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"autoGenerate": true}
                                """))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void disconnectTest() {
        mvc.perform(delete("/api/latest/meeting"))
                .andExpect(status().isOk());

        assertTrue(meetingInfoRepository.findByUsername("meet-coach").isEmpty());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void disconnectAndGetSettingsTest() {
        mvc.perform(delete("/api/latest/meeting"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/latest/meeting"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "provider": null,
                            "autoGenerate": false
                        }
                        """));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-user", authorities = {"USER"})
    void getSettingsUnauthorizedNonCoachTest() {
        mvc.perform(get("/api/latest/meeting"))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void upsertReplacesExistingProviderTest() {
        var repo = meetingInfoRepository;

        // Verify Google is connected
        var before = repo.findByUsername("meet-coach").orElseThrow();
        assertThat(before.getProvider(), is(MeetingInfo.Provider.GOOGLE));

        // Simulate connecting a different provider (upsert replaces)
        repo.upsertConnection("meet-coach", "ZOOM", "zoom-refresh", "zoom-access", "coach@zoom.com");

        var after = repo.findByUsername("meet-coach").orElseThrow();
        assertThat(after.getProvider(), is(MeetingInfo.Provider.ZOOM));
        assertThat(after.getEmail(), is("coach@zoom.com"));

        // Only one row per user
        assertThat(repo.count(), is(1L));
    }
}
