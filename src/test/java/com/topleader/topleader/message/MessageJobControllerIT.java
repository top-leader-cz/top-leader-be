package com.topleader.topleader.message;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class MessageJobControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(authorities = "JOB")
    void processNotDisplayedMessages() throws Exception {

        mvc.perform(get("/api/protected/jobs/displayedMessages"))
                .andExpect(status().isOk())
                .andDo(print());
//                .andExpect(content().json("""
//                    [
//                      {
//                        "username": "user2",
//                        "unreadMessageCount": 1,
//                        "lastMessage": "Im doing well, thanks ! ",
//                        "createdAt": "2023-08-01T10:10:00",
//                        "firstName": "Bad",
//                        "lastName": "user2"
//                      },
//                      {
//                        "username": "user3",
//                        "unreadMessageCount": 1,
//                        "lastMessage": "Hello from user1 to user3",
//                        "createdAt": "2023-08-01T11:01:00",
//                        "firstName": "No",
//                        "lastName": "user3"
//                      }
//                    ]
//                """));
    }
}
