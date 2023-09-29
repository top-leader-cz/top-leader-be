package com.topleader.topleader.user;

import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.email.VelocityService;
import com.topleader.topleader.user.token.Token;
import com.topleader.topleader.user.token.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationService {

    private final EmailService emailService;

    private final VelocityService velocityService;

    private final TokenService tokenService;

    @Value("${top-leader.app-url}")
    private String appUrl;

    @Value("${top-leader.supported-invitations}")
    private List<String> supportedInvitations;

    @Value("${top-leader.default-locale}")
    private String defaultLocale;

    public void sendInvite(UserController.UserRequest request) {
        log.info("Sending invitation for: [{}]", request.getUsername());
        var token = tokenService.generateToken();
        var params = Map.of("firstName", request.getFirstName(), "lastName", request.getLastName(), "appUrl", appUrl +"/#/set-password/" + token);
        var emailBody = velocityService.getMessage(new HashMap<>(params), parseTemplateName(request.getLocale()));
        tokenService.saveToken(new Token().setToken(token).setUsername(request.getUsername()).setType(Token.Type.SET_PASSWORD));
        emailService.sendEmail(request.getUsername(), "Unlock Your Potential with TopLeader!",  emailBody);

    }

    public String parseTemplateName(String locale) {
        return "templates/invitation-" + parseLocale(locale) + ".vm";
    }

    public String parseLocale(String locale) {
        return supportedInvitations.contains(locale) ? locale : defaultLocale;
    }

  }
