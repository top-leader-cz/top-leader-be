package com.topleader.topleader.coach.session;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.scheduled_session.ScheduledSession;
import com.topleader.topleader.scheduled_session.ScheduledSessionRepository;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static com.topleader.topleader.scheduled_session.ScheduledSession.Status.COMPLETED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/coach/coach-session.sql")
class ClientSessionControllerIT extends IntegrationTest {


    @Autowired
    ScheduledSessionRepository repository;

    @Autowired
    CoachSessionViewRepository coachSessionViewRepository;

    @SneakyThrows
    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void updateSessions() {
        mvc.perform(patch("/api/latest/coach-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                            "id": "1",
                                            "status": "COMPLETED"
                                          }
                                    """
                        ))
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                           "id": 1,
                           "date": "2023-08-14T10:30:00Z",
                           "client": "client1",
                           "status": "COMPLETED"
                         }
                                          """)));

        Assertions.assertThat(repository.findOne(Example.of(new ScheduledSession().setId(1L))).get().getStatus()).isEqualTo(COMPLETED);

    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void fetchClients() {
        mvc.perform(get("/api/latest/coach-sessions/clients"))
                .andExpect(status().isOk())
                .andDo(print())
                 .andExpect(content().json(String.format("""
                         [
                           {
                             "username": "client1",
                             "firstName": "Cool",
                             "lastName": "Client"
                           },
                           {
                             "username": "client2",
                             "firstName": "Bad",
                             "lastName": "Client"
                           },
                           {
                             "username": "client3",
                             "firstName": "No",
                             "lastName": "Client"
                           }
                         ]
                                          """)));


    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getSessionsUpcoming() {
        var sessionDate = coachSessionViewRepository.findById(7L).get().getDate();
        mvc.perform(post("/api/latest/coach-sessions?page=0&size=10&sort=date,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                            "client": "client1",
                                            "status": "UPCOMING"
                                          }
                                    """
                        ))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(String.format("""
                        {
                          "content": [
                            {
                              "id": 7,
                              "date": "%s",
                              "status": "UPCOMING",
                              "client": "client1",
                              "firstName": "Cool",
                              "lastName": "Client"
                            }
                          ],
                          "pageable": {
                            "pageNumber": 0,
                            "pageSize": 10,
                            "sort": {
                              "empty": false,
                              "sorted": true,
                              "unsorted": false
                            },
                            "offset": 0,
                            "paged": true,
                            "unpaged": false
                          },
                          "last": true,
                          "totalPages": 1,
                          "totalElements": 1,
                          "first": true,
                          "size": 10,
                          "number": 0,
                          "sort": {
                            "empty": false,
                            "sorted": true,
                            "unsorted": false
                          },
                          "numberOfElements": 1,
                          "empty": false
                        }
                        """, sessionDate.toOffsetDateTime())));

    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "coach", authorities = {"COACH"})
    void getSessionsPending() {
        mvc.perform(post("/api/latest/coach-sessions?page=0&size=10&sort=date,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                            "client": "client1",
                                            "status": "PENDING",
                                            "from": "2023-08-14T11:30:00Z",
                                            "to":   "2023-08-16T10:30:00Z"
                                          }
                                    """
                        ))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(String.format("""
                        {
                             "content": [
                               {
                                 "id": 2,
                                 "date": "2023-08-14T11:30:00Z",
                                 "client": "client1",
                                 "status": "PENDING"
                               },
                               {
                                 "id": 4,
                                 "date": "2023-08-15T11:30:00Z",
                                 "client": "client1",
                                 "status": "PENDING"
                               }
                             ],
                             "pageable": {
                               "pageNumber": 0,
                               "pageSize": 10,
                               "sort": {
                                 "empty": false,
                                 "sorted": true,
                                 "unsorted": false
                               },
                               "offset": 0,
                               "unpaged": false,
                               "paged": true
                             },
                             "last": true,
                             "totalElements": 2,
                             "totalPages": 1,
                             "first": true,
                             "size": 10,
                             "number": 0,
                             "sort": {
                               "empty": false,
                               "sorted": true,
                               "unsorted": false
                             },
                             "numberOfElements": 2,
                             "empty": false
                           }
                        """)));

    }

}