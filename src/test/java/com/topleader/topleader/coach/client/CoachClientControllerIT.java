/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.client;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.notification.Notification;
import com.topleader.topleader.notification.NotificationRepository;
import com.topleader.topleader.notification.context.CoachUnlinkedNotificationContext;
import com.topleader.topleader.scheduled_session.ScheduledSession;
import com.topleader.topleader.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.token.TokenRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/coach/coach-clients-test.sql")
class CoachClientControllerIT extends IntegrationTest {

    @Autowired
    private ScheduledSessionRepository scheduledSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    @WithMockUser(username = "coach", authorities = "COACH")
    void findClientsTest() throws Exception {

        final var now = LocalDateTime.now().withNano(0);

        final var nextSessionTimeClient1 = now.plusHours(1L);
        final var nextSessionTimeClient2 = now.plusHours(1L).plusMinutes(10);

        scheduledSessionRepository.saveAll(
            List.of(
                new ScheduledSession()
                    .setPaid(false)
                    .setPrivate(false)
                    .setCoachUsername("coach")
                    .setUsername("client1")
                    .setTime(now.plusHours(2L)),
                new ScheduledSession()
                    .setPaid(false)
                    .setPrivate(false)
                    .setCoachUsername("coach")
                    .setUsername("client1")
                    .setTime(nextSessionTimeClient1),
                new ScheduledSession()
                    .setPaid(false)
                    .setPrivate(false)
                    .setCoachUsername("coach")
                    .setUsername("client2")
                    .setTime(now.plusHours(2L)),
                new ScheduledSession()
                    .setPaid(false)
                    .setPrivate(false)
                    .setCoachUsername("coach")
                    .setUsername("client2")
                    .setTime(nextSessionTimeClient2),
                new ScheduledSession()
                    .setPaid(false)
                    .setPrivate(false)
                    .setCoachUsername("coach")
                    .setUsername("client3")
                    .setTime(now.plusHours(2L)),
                new ScheduledSession()
                    .setPaid(false)
                    .setPrivate(false)
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
                """, nextSessionTimeClient1.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), nextSessionTimeClient2.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
    }

    @Test
    @WithMockUser(username = "coach", authorities = "COACH")
    void deleteClientTest() throws Exception {

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

    @Test
    @WithMockUser(username = "coach", authorities = "COACH")
    void testInviteUserEndpoint() throws Exception {

        mvc.perform(post("/api/latest/coach-clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "user4",
                        "firstName": "Dan",
                        "lastName": "Aaa",
                        "isTrial": false,
                        "locale": "en"
                    }
                    """
                ))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""
                {
                  "username": "user4",
                  "firstName": "Dan",
                  "lastName": "Aaa",
                  "lastSession": null,
                  "nextSession": null
                }
                """))
        ;

        final var user = userRepository.findById("user4").orElseThrow();

        assertThat(user.getCoach(), is("coach"));
        assertThat(user.getFreeCoach(), is("coach"));
        assertThat(user.getStatus(), is(User.Status.PENDING));
        assertThat(user.getLocale(), is("en"));
    }
    @Test
    @WithMockUser(username = "coach", authorities = "COACH")
    void testInviteTrialUserEndpoint() throws Exception {

        mvc.perform(post("/api/latest/coach-clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "user4",
                        "firstName": "Dan",
                        "lastName": "Aaa",
                        "isTrial": true,
                        "locale": "en"
                    }
                    """
                ))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""
                {
                  "username": "user4",
                  "firstName": "Dan",
                  "lastName": "Aaa",
                  "lastSession": null,
                  "nextSession": null
                }
                """))
        ;

        final var user = userRepository.findById("user4").orElseThrow();

        assertThat(user.getCoach(), is("coach"));
        assertThat(user.getFreeCoach(), is("coach"));
        assertThat(user.getStatus(), is(User.Status.AUTHORIZED));

        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("top-leader");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("user4");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Unlock Your Potential with TopLeader!");
        Assertions.assertThat(body)
            .contains("Dan Aaa,")
            .contains("http://app-test-ur=\r\nl/#/api/public/set-password/")
            .contains("Unlock Your ");

        Assertions.assertThat(tokenRepository.findAll()).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "coach", authorities = "COACH")
    @Sql(scripts = "/sql/coach/coach-empty.sql")
    void findClientsEmpty() throws Exception {

        final var now = LocalDateTime.now().withNano(0);

        final var nextSessionTimeClient1 = now.plusHours(1L);
        final var nextSessionTimeClient2 = now.plusHours(1L).plusMinutes(10);
        mvc.perform(get("/api/latest/coach-clients"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("[]", nextSessionTimeClient1, nextSessionTimeClient2)));
    }
}
