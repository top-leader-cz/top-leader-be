package com.topleader.topleader.util.ai;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;

class AiTest extends IntegrationTest {

    @Autowired
    private ChatModel chatClient;
    @Test
    void aITest() {
        String call = chatClient.call("Are you working?");
    }
}
