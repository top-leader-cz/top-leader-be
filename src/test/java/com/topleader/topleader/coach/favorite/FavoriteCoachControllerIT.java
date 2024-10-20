package com.topleader.topleader.coach.favorite;

import com.topleader.topleader.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/coach/favorite/favorite_coach.sql")
class FavoriteCoachControllerIT extends IntegrationTest {

    @Autowired
    private FavoriteCoachRepository favoriteCoachRepository;

    @Test
    @WithUserDetails("jakub.svezi@dummy.com")
    public void saveFavoriteCoach() throws Exception {
        mvc.perform(post("/api/latest/coach-favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                    "test@coach1", "test@coach2"
                                ]
                                """)
                )
                .andExpect(status().isOk())
        ;
        Assertions.assertThat(favoriteCoachRepository.findAll())
                .extracting( i -> i.getId().getCoachUsername())
                .containsExactlyInAnyOrder("coach", "coach2", "test@coach1", "test@coach2");

    }

    @Test
    @WithUserDetails("jakub.svezi@dummy.com")
    public void getFavoriteCoach() throws Exception {
        mvc.perform(get("/api/latest/coach-favorite"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$", hasItems("coach")));
    }

}