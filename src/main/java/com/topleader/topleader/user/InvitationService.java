package com.topleader.topleader.user;

import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.email.VelocityService;
import com.topleader.topleader.user.token.Token;
import com.topleader.topleader.user.token.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    public void sendInvite(UserInvitationRequestDto request) {
        log.info("Sending invitation for: [{}]", request.username());
        var token = tokenService.generateToken();
        var setPasswordUrl =  String.format( "%s/#/api/public/set-password/%s/%s", appUrl, request.username, token);
        var params = Map.of("firstName", request.firstName(), "lastName", request.lastName(), "appUrl", appUrl, "passwordLink", setPasswordUrl);
        var emailBody = velocityService.getMessage(new HashMap<>(params), parseTemplateName(request.locale()));
        tokenService.saveToken(new Token().setToken(token).setUsername(request.username()).setType(Token.Type.SET_PASSWORD));
        emailService.sendEmail(request.username(), "Unlock Your Potential with TopLeader!", emailBody);

    }

    public String parseTemplateName(String locale) {
        return "templates/invitation/invitation-" + parseLocale(locale) + ".vm";
    }

    public String parseLocale(String locale) {
        return supportedInvitations.contains(locale) ? locale : defaultLocale;
    }



    public record UserInvitationRequestDto(String firstName, String lastName, String username, String timezone, String locale) {
        public static UserInvitationRequestDto from(User user, String locale) {
            return new UserInvitationRequestDto(user.getFirstName(), user.getLastName(), user.getUsername(), user.getTimeZone(), locale);
        }
    }

}
