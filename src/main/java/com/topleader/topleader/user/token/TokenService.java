package com.topleader.topleader.user.token;

import com.topleader.topleader.util.common.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    private final PasswordEncoder passwordEncoder;

    public String generateToken() {
        return Base64.getEncoder().encodeToString(passwordEncoder.encode(CommonUtils.generateToken()).getBytes());
    }

    public void saveToken(Token token) {
        log.info("Saving token for: [{}] type: [{}]", token.getUsername(), token.getType());
        tokenRepository.save(token);
    }

    public Optional<Token> findByTokenAndType(String token, Token.Type type) {
        return tokenRepository.findByTokenAndType(token, type);
    }

    public void delete(Token token) {
        tokenRepository.delete(token);
    }
}
