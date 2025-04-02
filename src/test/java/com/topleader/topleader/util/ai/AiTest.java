package com.topleader.topleader.util.ai;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

class AiTest  {


    @Disabled
    @Test
    void aITest() {
        var a = "2025-02-17T08:00:00Z";
        LocalDateTime parse = ZonedDateTime.parse(a).toLocalDateTime();
//        var a = "Based on the user's profile information, where the company’s business strategy is %s, the current position is %s, and the aspired competency is %s, suggest a few recommended areas for development that would align with both their role and their growth aspirations. Please keep each recommendation concise and focused, providing 2-3 suggested areas for development that would best support the user's career growth. return result as json array in the format and  in %s language";
//        String call = chatClient.call(String.format(a, "make am money", "developer", "make more money", "English"));
    }


}
