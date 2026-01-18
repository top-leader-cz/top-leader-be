/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import com.topleader.topleader.IntegrationTest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserChatRepository userChatRepository;

    @Autowired
    private LastMessageRepository lastMessageRepository;

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

        final var messages = messageRepository.findAll().stream()
                .filter(m -> m.getUserFrom().equals("user1") && m.getUserTo().equals("user2"))
                .collect(Collectors.toList());

        assertThat(messages, hasSize(3));

        final var latestMessage = messages.stream()
            .max(Comparator.comparing(Message::getCreatedAt));

        assertThat(latestMessage.isPresent(), is(true));
        assertThat(latestMessage.get().getMessageData(), is("hello there :-)"));

        final var lastMessage = messageRepository.findById(lastMessageRepository.findById(1L).orElseThrow().getMessageId()).orElseThrow();

        assertThat(lastMessage.getMessageData(), is("hello there :-)"));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void testSendMessageNotExistentChat() throws Exception {

        mvc.perform(post("/api/latest/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userTo": "user4",
                        "messageData": "hello there :-)"
                    }
                    """)
            )
            .andExpect(status().isOk())
        ;

        final var messages = messageRepository.findAll().stream()
            .filter(m -> m.getUserFrom().equals("user1") && m.getUserTo().equals("user4"))
            .toList();

        assertThat(messages, hasSize(1));

        assertThat(messages.getFirst().getMessageData(), is("hello there :-)"));

        final var chat = userChatRepository.findUserChat("user1", "user4");

        assertThat(chat.isPresent(), is(true));


        final var lastMessage = messageRepository.findById(
            lastMessageRepository.findById(chat.get().getChatId()).orElseThrow().getMessageId()
        ).orElseThrow();

        assertThat(lastMessage.getMessageData(), is("hello there :-)"));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void testGetUserChatInfo() throws Exception {

        mvc.perform(get("/api/latest/messages"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    [
                      {
                        "username": "user2",
                        "unreadMessageCount": 1,
                        "lastMessage": "Im doing well, thanks ! ",
                        "createdAt": "2023-08-01T10:10:00",
                        "firstName": "Bad",
                        "lastName": "user2"
                      },
                      {
                        "username": "user3",
                        "unreadMessageCount": 1,
                        "lastMessage": "Hello from user1 to user3",
                        "createdAt": "2023-08-01T11:01:00",
                        "firstName": "No",
                        "lastName": "user3"
                      }
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
            .andExpect(content().json("""
                {
                   "content": [
                     {
                       "id": 1,
                       "username": "user1",
                       "addressee": "user2",
                       "messageData": "Hello from user1 to user2",
                       "displayed": true,
                       "createdAt": "2023-08-01T10:00:00"
                     },
                     {
                       "id": 2,
                       "username": "user2",
                       "addressee": "user1",
                       "messageData": "Hi there! How are you?",
                       "displayed": true,
                       "createdAt": "2023-08-01T10:05:00"
                     },
                     {
                       "id": 3,
                       "username": "user1",
                       "addressee": "user2",
                       "messageData": "Im doing well, thanks ! ",
                       "displayed": false,
                       "createdAt": "2023-08-01T10:10:00"
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
                     "sorted": true,
                     "unsorted": false
                   },
                   "numberOfElements": 3,
                   "first": true,
                   "empty": false
                 }
                """));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void testValidationSendMessage() throws Exception {

        mvc.perform(post("/api/latest/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userTo": "user2",
                                    "messageData": ""
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                    [
                      {
                        "errorCode": "Size",
                        "fields": [
                          {
                            "name": "messageData",
                            "value": ""
                          }
                        ],
                        "errorMessage": "Message length needs to be at least 1 character"
                      }
                    ]

                """));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void testLastMessageUpsert() throws Exception {
        // Send first message
        mvc.perform(post("/api/latest/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userTo": "user2",
                        "messageData": "First message"
                    }
                    """)
        ).andExpect(status().isOk());

        var chat = userChatRepository.findUserChat("user1", "user2").orElseThrow();
        var firstLastMessage = lastMessageRepository.findById(chat.getChatId()).orElseThrow();
        var firstMessage = messageRepository.findById(firstLastMessage.getMessageId()).orElseThrow();
        assertThat(firstMessage.getMessageData(), is("First message"));

        // Send second message - should update last message
        mvc.perform(post("/api/latest/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userTo": "user2",
                        "messageData": "Second message"
                    }
                    """)
        ).andExpect(status().isOk());

        var secondLastMessage = lastMessageRepository.findById(chat.getChatId()).orElseThrow();
        var secondMessage = messageRepository.findById(secondLastMessage.getMessageId()).orElseThrow();

        // Verify last message was updated (upserted)
        assertThat(secondMessage.getMessageData(), is("Second message"));
        assertThat(secondMessage.getId() > firstMessage.getId(), is(true));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void testMarkMessagesAsDisplayed() throws Exception {
        // Initially user1 has unread messages from user2 and user3
        var unreadBefore = messageRepository.getUnreadMessagesCount("user1");
        assertThat(unreadBefore, hasSize(2));

        // Get chat with user2 marks ALL messages for user1 as displayed
        mvc.perform(get("/api/latest/messages/user2"))
            .andExpect(status().isOk());

        // All messages should now be marked as displayed
        var unreadAfter = messageRepository.getUnreadMessagesCount("user1");
        assertThat(unreadAfter, hasSize(0));
    }



}
