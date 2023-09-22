/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.client;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.notification.Notification;
import com.topleader.topleader.notification.NotificationRepository;
import com.topleader.topleader.notification.context.CoachUnlinkedNotificationContext;
import com.topleader.topleader.scheduled_session.ScheduledSession;
import com.topleader.topleader.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/coach/coach-clients-test.sql")
public class CoachClientControllerIT extends IntegrationTest {

    @Autowired
    private ScheduledSessionRepository scheduledSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @WithMockUser(username = "coach", authorities = "COACH")
    public void findClientsTest() throws Exception {

        final var now = LocalDateTime.now();

        final var nextSessionTimeClient1 = now.plusHours(1L);
        final var nextSessionTimeClient2 = now.plusHours(1L).plusMinutes(10);

        scheduledSessionRepository.saveAll(
            List.of(
                new ScheduledSession()
                    .setCoachUsername("coach")
                    .setUsername("client1")
                    .setTime(now.plusHours(2L)),
                new ScheduledSession()
                    .setCoachUsername("coach")
                    .setUsername("client1")
                    .setTime(nextSessionTimeClient1),
                new ScheduledSession()
                    .setCoachUsername("coach")
                    .setUsername("client2")
                    .setTime(now.plusHours(2L)),
                new ScheduledSession()
                    .setCoachUsername("coach")
                    .setUsername("client2")
                    .setTime(nextSessionTimeClient2),
                new ScheduledSession()
                    .setCoachUsername("coach")
                    .setUsername("client3")
                    .setTime(now.plusHours(2L)),
                new ScheduledSession()
                    .setCoachUsername("coach")
                    .setUsername("client3")
                    .setTime(now)
            )
        );


        mvc.perform(get("/api/latest/coach-clients"))
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                [
                  {
                    "username": "client1",
                    "firstName": "Cool",
                    "lastName": "Client",
                    "lastSession": "2023-08-14T11:30:00",
                    "nextSession": "%s"
                  },
                  {
                    "username": "client2",
                    "firstName": "Bad",
                    "lastName": "Client",
                    "lastSession": "2023-08-15T11:30:00",
                    "nextSession": "%s"
                  }
                ]
                """, nextSessionTimeClient1, nextSessionTimeClient2)));
    }

    @Test
    @WithMockUser(username = "coach", authorities = "COACH")
    public void deleteClientTest() throws Exception {

        mvc.perform(delete("/api/latest/coach-clients/client1"))
            .andExpect(status().isOk())
            ;

        assertThat(userRepository.findById("client1").orElseThrow().getCoach(), nullValue());

        final var notifications = notificationRepository.findAll();

        assertThat(notifications, hasSize(1));

        final var notification = notifications.get(0);

        assertThat(notification.getUsername(), is("client1"));
        assertThat(notification.getType(), is(Notification.Type.COACH_UNLINKED));
        assertThat(notification.getContext(), instanceOf(CoachUnlinkedNotificationContext.class));
        assertThat(((CoachUnlinkedNotificationContext)notification.getContext()).getCoach(), is("coach"));


    }
}
