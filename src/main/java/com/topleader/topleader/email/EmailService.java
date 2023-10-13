package com.topleader.topleader.email;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${top-leader.default-from}")
    private String defaultFrom;

    public void sendEmail(String to, String subject, String body) {
        sendEmail(defaultFrom, to, subject, body);
    }

    public void sendEmail(String from, String to, String subject, String body) {
        log.info("Sending email to: [{}] subject: [{}]", to, subject);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
