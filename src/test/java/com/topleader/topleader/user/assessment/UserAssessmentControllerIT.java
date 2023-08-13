/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.assessment;

import com.topleader.topleader.IntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql("/sql/user_info/assessments/user-assessment-test.sql")
public class UserAssessmentControllerIT extends IntegrationTest {

    @Autowired
    private UserAssessmentRepository userAssessmentRepository;


    @Test
    @WithMockUser
    public void getUserAssessmentsTest() throws Exception {
        mvc.perform(get("/api/latest/user-assessments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("questionAnswered").value(3))
            .andExpect(jsonPath("answers").isArray())
            .andExpect(jsonPath("answers", hasSize(3)))
            .andExpect(jsonPath("answers[0].questionId").value(1))
            .andExpect(jsonPath("answers[0].answer").value(1))
            .andExpect(jsonPath("answers[1].questionId").value(2))
            .andExpect(jsonPath("answers[1].answer").value(1))
            .andExpect(jsonPath("answers[2].questionId").value(3))
            .andExpect(jsonPath("answers[2].answer").value(2))
        ;
    }

    @Test
    @WithMockUser
    public void deleteUserAssessmentsTest() throws Exception {
        mvc.perform(delete("/api/latest/user-assessments"))
            .andExpect(status().isOk())
        ;

        assertThat(userAssessmentRepository.findAll(), hasSize(0));
    }

    @Test
    @WithMockUser
    public void setUserAssessmentsTest() throws Exception {
        mvc.perform(post("/api/latest/user-assessments/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "answer": 2
                    }
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("questionId").value(4))
            .andExpect(jsonPath("answer").value(2))
        ;

        assertThat(userAssessmentRepository.findById(new UserAssessmentId().setQuestionId(4L).setUsername("user")), is(2));

    }

    @Test
    @WithMockUser
    public void getUserAssessmentsByIdTest() throws Exception {
        mvc.perform(get("/api/latest/user-assessments/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("questionId").value(3))
            .andExpect(jsonPath("answer").value(2))
        ;
    }
}
