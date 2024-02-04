package com.topleader.topleader.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/protected/jobs")
@RequiredArgsConstructor
public class MessageJobController {

    private final MessageService messageService;

    @GetMapping("/displayedMessages")
    @Secured({"JOB"})
    public void processNotDisplayedMessages() {
        log.info("Triggering not displayed messages processing");
        messageService.processNotDisplayedMessages();
    }
}
