package com.topleader.topleader.user;

import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.email.VelocityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final EmailService emailService;

    private final VelocityService velocityService;

    @Value("${top-leader.app-url}")
    private String appUrl;

    public void sendInvite(UserController.AddUserRequest request) {
        var params = Map.of("firstName", request.getFirstName(), "lastName", request.getLastName(), "appUrl", appUrl);
        var emailBody = velocityService.getMessage(new HashMap<>(params), "templates/invitation-" + request.getLocale());
        emailService.sendEmail(request.getUsername(), "Unlock Your Potential with TopLeader!",  emailBody);
    }
}
