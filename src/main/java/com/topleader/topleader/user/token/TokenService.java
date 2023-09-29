package com.topleader.topleader.user.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    private final PasswordEncoder passwordEncoder;

    public String generateToken() {
        var uuid = UUID.randomUUID().toString();
        return passwordEncoder.encode(uuid);
    }

    public void saveToken(Token token) {
        log.info("Saving token for: [{}] type: [{}]", token.getUsername(), token.getType());
        tokenRepository.save(token);
    }
}
