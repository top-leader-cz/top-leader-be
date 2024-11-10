package com.topleader.topleader.user.session.reminder;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.apache.commons.lang3.BooleanUtils.forEach;

@Slf4j
@RestController
@RequestMapping("/api/protected/jobs")
@RequiredArgsConstructor
public class SessionReminderController {

   SessionReminderService sessionReminderService;

    @GetMapping("/unscheduled-sessions")
    @Secured({"JOB"})
    public void processNotDisplayedMessages() {
        var usersToNotify = sessionReminderService.getUserWithNoScheduledSessions();
        log.info("Users to remind to schedule sessions {}", usersToNotify);
        usersToNotify.forEach(user -> sessionReminderService.sendReminder(user));
    }
}
