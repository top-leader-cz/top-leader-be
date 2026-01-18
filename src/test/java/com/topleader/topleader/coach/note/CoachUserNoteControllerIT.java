package com.topleader.topleader.coach.note;

import com.topleader.topleader.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/user/note/coach-user-note.sql")
class CoachUserNoteControllerIT extends IntegrationTest {

    @Autowired
    CoachUserNoteRepository repository;

    @Test
    @WithUserDetails(value = "petr.cocah@dummy.com")
    void addNote() throws Exception {
        mvc.perform(post("/api/latest/coach-user-note/jakub.user@dummy.com").
                        contentType(MediaType.APPLICATION_JSON).content("""

                                        {
                                   "note": "test note"
                                           
                                }
                                """))
                .andExpect(status().isOk());

        Assertions.assertThat(repository.findByCoachIdAndUserId("petr.cocah@dummy.com", "jakub.user@dummy.com").get().getNote())
                .isEqualTo("test note");

    }

    @Test
    @WithUserDetails(value = "petr.cocah@dummy.com")
    void getNote() throws Exception {
        mvc.perform(get("/api/latest/coach-user-note/jakub.user2@dummy.com")).
                andExpect(status().isOk())
                .andExpect(content().json("""
                                                                 {
                        "coachId": "petr.cocah@dummy.com",
                        "userId": "jakub.user2@dummy.com",
                        "note": "note1"
                        }
                                                                 """));
    }


}