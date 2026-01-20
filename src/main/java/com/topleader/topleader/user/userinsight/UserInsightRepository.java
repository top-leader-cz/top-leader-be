package com.topleader.topleader.user.userinsight;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface UserInsightRepository extends ListCrudRepository<UserInsight, Long> {

    @Query("SELECT * FROM user_insight WHERE username = :username")
    Optional<UserInsight> findByUsername(String username);
}
