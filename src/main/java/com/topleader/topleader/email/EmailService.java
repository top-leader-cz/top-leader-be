package com.topleader.topleader.email;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
        sendEmail(defaultFrom, to, subject, body);
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
}
