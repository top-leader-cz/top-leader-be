package com.topleader.topleader.user.userinsight.article;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("select a from Article a where a.username = :username")
    List<Article> findByUsername(String username);
}