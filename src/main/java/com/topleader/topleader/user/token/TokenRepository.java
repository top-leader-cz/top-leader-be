package com.topleader.topleader.user.token;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenAndType(String token, Token.Type type);

}
