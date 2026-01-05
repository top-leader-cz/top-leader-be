package com.topleader.topleader.common.email;


import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${top-leader.default-from}")
    private String defaultFrom;

    public void sendEmail(String to, String subject, String body) {
        Try.run(() -> sendEmail(defaultFrom, to, subject, body))
                .onFailure(e -> log.error("Failed to send email to: [{}] subject: [{}]", to, subject, e));
    }

    @SneakyThrows
    public void sendEmail(String from, String to, String subject, String body) {
        log.info("Sending email to: [{}] subject: [{}]", to, subject);
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
    }

    @SneakyThrows
    public void sendEmail(String to, String subject, String body, Calendar event) {
        log.info("Sending email to: [{}] subject: [{}]", to, subject);
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true, "utf-8");

        log.info(event.toString());

        helper.setFrom(defaultFrom);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        helper.addAttachment("event.ics", new ByteArrayResource(event.toString().getBytes()), "text/calendar");
        mailSender.send(message);
    }
}
