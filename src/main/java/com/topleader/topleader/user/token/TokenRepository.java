package com.topleader.topleader.user.token;


import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;


public interface TokenRepository extends CrudRepository<Token, Long>, PagingAndSortingRepository<Token, Long> {

    @Query("SELECT * FROM token WHERE token = :token AND type = :type")
    Optional<Token> findByTokenAndType(String token, String type);

    default Optional<Token> findByTokenAndType(String token, Token.Type type) {
        return findByTokenAndType(token, type.name());
    }
}
