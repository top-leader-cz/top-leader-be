package com.topleader.topleader.user.userinsight.article;

import com.topleader.topleader.user.session.domain.UserArticle;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@ToString(of = {"id", "username"})
@Table("article")
@Accessors(chain = true)
public class Article {

    @Id
    private Long id;

    private String username;

    private UserArticle content;

    private LocalDateTime createdAt;
}
