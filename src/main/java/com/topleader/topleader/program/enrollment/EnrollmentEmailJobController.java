package com.topleader.topleader.program.enrollment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/protected/jobs")
@RequiredArgsConstructor
public class EnrollmentEmailJobController {

    private final ProgramEnrollmentEmailService enrollmentEmailService;

    @PostMapping("/enrollment-emails")
    @Secured({"JOB"})
    public void processEnrollmentEmails() {
        log.info("Triggering enrollment email processing");
        enrollmentEmailService.processScheduledEmails();
    }
}
