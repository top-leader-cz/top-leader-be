package com.topleader.topleader.myteam;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.common.util.image.ArticleImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/manager/my-team.sql")
class MyTeamViewControllerIT extends IntegrationTest {
    @Autowired
    private ArticleImageService articleImageService;

    @Test
    @WithMockUser(username = "managerUser", authorities = "MANAGER")
    void testListUsersEndpoint() throws Exception {

        mvc.perform(get("/api/latest/my-team"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                  "content": [
                    {
                      "firstName": "Alice",
                      "lastName": "Smith",
                      "username": "user1",
                      "coach": "Coach2",
                      "coachFirstName": "Coach",
                      "coachLastName": "Borek",
                      "credit": 50,
                      "requestedCredit": 10,
                      "scheduledCredit": 20,
                      "paidCredit": 222,
                      "longTermGoal": "Goal1",
                      "areaOfDevelopment": [
                        "aod"
                      ],
                      "strengths": [
                        "s1",
                        "s2",
                        "s3",
                        "s4",
                        "s5"
                      ]
                    },
                    {
                      "firstName": "Bob",
                      "lastName": "Johnson",
                      "username": "user2",
                      "coach": "Coach3",
                      "coachFirstName": null,
                      "coachLastName": null,
                      "credit": 75,
                      "requestedCredit": 25,
                      "scheduledCredit": 30,
                      "paidCredit": 333,
                      "longTermGoal": "Goal2",
                      "areaOfDevelopment": [
                        "Area2"
                      ],
                      "strengths": [
                        "s6",
                        "s7",
                        "s8",
                        "s9",
                        "s10"
                      ]
                    }
                  ],
                  "pageable": {
                    "pageNumber": 0,
                    "pageSize": 20,
                    "sort": {
                      "sorted": false,
                      "unsorted": true,
                      "empty": true
                    },
                    "offset": 0,
                    "paged": true,
                    "unpaged": false
                  },
                  "totalPages": 1,
                  "totalElements": 2,
                  "last": true,
                  "numberOfElements": 2,
                  "first": true,
                  "size": 20,
                  "number": 0,
                  "sort": {
                    "sorted": false,
                    "unsorted": true,
                    "empty": true
                  },
                  "empty": false
                }
                """));
    }

    @Test
    @WithMockUser(username = "user1", authorities = "USER")
    void testAccessDeniedForNonManager() throws Exception {
        mvc.perform(get("/api/latest/my-team"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "managerUser", authorities = "MANAGER")
    void testPaginationAndSorting() throws Exception {
        mvc.perform(get("/api/latest/my-team")
                .param("page", "0")
                .param("size", "1")
                .param("sort", "firstName,asc")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "content": [
                    {
                      "firstName": "Alice",
                      "lastName": "Smith",
                      "username": "user1"
                    }
                  ],
                  "totalElements": 2,
                  "totalPages": 2,
                  "size": 1,
                  "number": 0
                }
                """, false));
    }

}