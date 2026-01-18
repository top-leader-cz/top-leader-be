package com.topleader.topleader.user.userinsight;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInsightRepository extends JpaRepository<UserInsight, Long> {
    Optional<UserInsight> findByUsername(String username);
}
