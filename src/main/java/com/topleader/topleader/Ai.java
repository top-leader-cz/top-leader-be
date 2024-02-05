package com.topleader.topleader;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RequiredArgsConstructor
@RequestMapping("/api/public")
@RestController
public class Ai {

     private final ChatClient chatClient;
    @GetMapping("/ai-test")
    public String test() {

        var query = new Prompt("what is result of 1 + 3");

        return chatClient.call(query).getResult().getOutput().getContent();
    }
}
