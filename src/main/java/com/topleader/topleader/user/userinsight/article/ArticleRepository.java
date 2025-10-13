package com.topleader.topleader.user.userinsight.article;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("select a from Article a where a.username = :username")
    List<Article> findByUsername(String username);

    @Transactional
    @Modifying
    void deleteAllByUsername(String username);
}