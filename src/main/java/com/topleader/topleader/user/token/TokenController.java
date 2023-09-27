package com.topleader.topleader.user.token;

import com.topleader.topleader.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/set-password")
public class TokenController {

    private final PasswordEncoder passwordEncoder;

    private final TokenRepository tokenRepository;

    private final UserRepository userRepository;

    @PostMapping("{token}")
    public void setPassword(@PathVariable String token,  @Valid @RequestBody SetPasswordRequestDto request) {
        log.info("User set-password start. token: {}", token);

        var savedToken = tokenRepository.findByTokenAndType(token, Token.Type.SET_PASSWORD)
                .orElseThrow(() -> new EntityNotFoundException("Token not found. Token: " + token));

        var user = userRepository.findById(savedToken.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found. User: " + savedToken.getUsername()));

        log.info("Token found for user {}", user.getUsername());
        user.setPassword(passwordEncoder.encode(request.password));
        userRepository.save(user);

        log.info("User set-password finished. token: {}", token);
    }

    public  record SetPasswordRequestDto(
            @NotEmpty String password

    ) {
    }
}
