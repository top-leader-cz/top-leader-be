package com.topleader.topleader.user.token;

import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.email.VelocityService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/public")
public class TokenController {

    private static final Map<String, String> subjects = Map.of(
            "en", "Password Reset Instructions for Your TopLeader Account",
            "cs", "Instrukce pro obnovení hesla k vašemu účtu v TopLeader",
            "fr", "Instructions pour réinitialiser le mot de passe de votre compte TopLeader",
            "de", "Anweisungen zum Zurücksetzen Ihres Passworts für Ihr TopLeader-Konto");

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final TokenService tokenService;

    private final EmailService emailService;

    private final VelocityService velocityService;

    @Value("${top-leader.app-url}")
    private String appUrl;

    @Value("${top-leader.supported-invitations}")
    private List<String> supportedInvitations;

    @Value("${top-leader.default-locale}")
    private String defaultLocale;

    @PostMapping("/set-password/{token}")
    public void setPassword(@PathVariable String token,  @Valid @RequestBody TokenController.SetPasswordRequestRequest request) {
        log.info("User set-password start. token: {}", token);

        var savedToken = tokenService.findByTokenAndType(token, Token.Type.SET_PASSWORD)
                .orElseThrow(() -> new EntityNotFoundException("Token not found. Token: " + token));

        var user = userRepository.findById(savedToken.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found. User: " + savedToken.getUsername()));

        log.info("Token found for user {}", user.getUsername());
        user.setPassword(passwordEncoder.encode(request.password));
        userRepository.save(user);
        tokenService.delete(savedToken);

        log.info("User set-password finished. token: {}", token);
    }

    @PostMapping("/reset-password-link")
    public void generateResetPasswordLink(@Valid @RequestBody TokenController.GenerateLinkRequest request) {
        log.info("User generate-link username: [{}]", request.username);

        userRepository.findByEmail(request.username)
                .ifPresentOrElse(user -> {
                    var token = tokenService.generateToken();
                    var link = String.format("%s/#/api/public/set-password/%s/%s", appUrl, request.username, token);
                    var params = Map.of("firstName", user.getFirstName(), "lastName", user.getLastName(), "link", link);
                    var emailBody = velocityService.getMessage(new HashMap<>(params), parseTemplateName(request.locale()));
                    tokenService.saveToken(new Token().setToken(token).setUsername(user.getUsername()).setType(Token.Type.SET_PASSWORD));

                    emailService.sendEmail(request.username, subjects.get(request.locale), emailBody);
                    log.info("User generate-link  finished. username: [{}]", request.username);
                }, () -> log.warn("Generate-link failed! User not found: [{}]", request.username));
    }

    public String parseTemplateName(String locale) {
        return "templates/reset-password/reset-password-" + parseLocale(locale) + ".vm";
    }

    public String parseLocale(String locale) {
        return supportedInvitations.contains(locale) ? locale : defaultLocale;
    }

    public  record SetPasswordRequestRequest(
            @NotEmpty String password

    ) {
    }

    public  record GenerateLinkRequest(
            @NotEmpty String username, // this is new email

            @Pattern(regexp = "[a-z]{2}")
            String locale


    ) {
    }
}
