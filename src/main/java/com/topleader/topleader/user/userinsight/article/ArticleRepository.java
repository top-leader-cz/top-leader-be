package com.topleader.topleader.user.userinsight.article;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ArticleRepository extends ListCrudRepository<Article, Long> {

    @Query("SELECT * FROM article WHERE username = :username")
    List<Article> findByUsername(String username);

    @Modifying
    @Query("DELETE FROM article WHERE username = :username")
    void deleteAllByUsername(String username);
}
