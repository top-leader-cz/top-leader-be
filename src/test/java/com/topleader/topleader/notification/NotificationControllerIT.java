/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.notification;

import com.topleader.topleader.IntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static java.util.function.Predicate.not;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/notifications/notifications-test-data.sql")
public class NotificationControllerIT extends IntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @WithMockUser("testuser1")
    public void getNotificationTest() throws Exception {

        mvc.perform(get("/api/latest/notifications")
                .param("size", "2")
                .param("sort", "createdAt,asc")
            )
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json("""
                {
                  "content": [
                    {
                      "id": 50,
                      "username": "testuser1",
                      "type": "MESSAGE",
                      "read": false,
                      "context": {
                        "type": "MESSAGE",
                        "fromUser": "sender1",
                        "message": "Notification 1"
                      },
                      "createdAt": "2023-08-01T10:00:00"
                    },
                    {
                      "id": 51,
                      "username": "testuser1",
                      "type": "MESSAGE",
                      "read": true,
                      "context": {
                        "type": "MESSAGE",
                        "fromUser": "sender2",
                        "message": "Notification 2"
                      },
                      "createdAt": "2023-08-01T11:00:00"
                    }
                  ],
                  "pageable": {
                    "sort": {
                      "empty": false,
                      "sorted": true,
                      "unsorted": false
                    },
                    "offset": 0,
                    "pageNumber": 0,
                    "pageSize": 2,
                    "paged": true,
                    "unpaged": false
                  },
                  "totalPages": 2,
                  "totalElements": 3,
                  "last": false,
                  "size": 2,
                  "number": 0,
                  "sort": {
                    "empty": false,
                    "sorted": true,
                    "unsorted": false
                  },
                  "numberOfElements": 2,
                  "first": true,
                  "empty": false
                }
                """))
        ;
    }

    @Test
    @WithMockUser("testuser1")
    public void markAllReadForUserTest() throws Exception {

        mvc.perform(post("/api/latest/notifications/mark-as-read"));

        assertThat(
            notificationRepository.findByUsername("testuser1", Pageable.unpaged()).stream()
                .noneMatch(not(Notification::isRead)),
            is(true)
        );

    }
}
