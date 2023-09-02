/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import com.topleader.topleader.IntegrationTest;
import java.util.Comparator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/messages/messages-test.sql")
class MessageControllerIT extends IntegrationTest {

    @Autowired
    private MessageRepository messageRepository;

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void testSendMessage() throws Exception {

        mvc.perform(post("/api/latest/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userTo": "user2",
                        "messageData": "hello there :-)"
                    }
                    """)
            )
            .andExpect(status().isOk())
        ;

        final var messages = messageRepository.findAll(Example.of(
            new Message()
                .setUserFrom("user1")
                .setUserTo("user2")
        ));

        assertThat(messages, hasSize(3));

        final var latestMessage = messages.stream()
            .max(Comparator.comparing(Message::getCreatedAt));

        assertThat(latestMessage.isPresent(), is(true));
        assertThat(latestMessage.get().getMessageData(), is("hello there :-)"));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void testGetUserChatInfo() throws Exception {

        mvc.perform(get("/api/latest/messages"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                {"username":"user2","unreadMessageCount":1,"lastMessage":"Hi there! How are you?"},
                {"username":"user3","unreadMessageCount":1,"lastMessage":"Hello from user3 to user1"}
                ]
                """));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void testGetUserChat() throws Exception {

        mvc.perform(get("/api/latest/messages/user2")
                .param("sort", "createdAt,asc")
            )
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json("""
                {
                  "content": [
                    {
                      "id": 50,
                      "username": "user1",
                      "addressee": "user2",
                      "messageData": "Hello from user1 to user2",
                      "displayed": true,
                      "createdAt": "2023-08-01T10:00:00"
                    },
                    {
                      "id": 51,
                      "username": "user2",
                      "addressee": "user1",
                      "messageData": "Hi there! How are you?",
                      "displayed": false,
                      "createdAt": "2023-08-01T10:05:00"
                    },
                    {
                      "id": 52,
                      "username": "user1",
                      "addressee": "user2",
                      "messageData": "Im doing well, thanks ! ",
                      "displayed": true,
                      "createdAt": "2023-08-01T10:10:00"
                    }
                  ],
                  "pageable": {
                    "sort": {
                      "empty": false,
                      "unsorted": false,
                      "sorted": true
                    },
                    "offset": 0,
                    "pageNumber": 0,
                    "pageSize": 20,
                    "paged": true,
                    "unpaged": false
                  },
                  "totalPages": 1,
                  "totalElements": 3,
                  "last": true,
                  "size": 20,
                  "number": 0,
                  "sort": {
                    "empty": false,
                    "unsorted": false,
                    "sorted": true
                  },
                  "numberOfElements": 3,
                  "first": true,
                  "empty": false
                }
                """));
    }
}
