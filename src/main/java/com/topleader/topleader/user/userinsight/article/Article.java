package com.topleader.topleader.user.userinsight.article;

import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.common.util.common.JsonUtils;
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

    private String content;

    private LocalDateTime createdAt;

    public UserArticle getContent() {
        return content != null ? JsonUtils.fromJsonString(content, UserArticle.class) : null;
    }

    public Article setContent(UserArticle userArticle) {
        this.content = userArticle != null ? JsonUtils.toJsonString(userArticle) : null;
        return this;
    }
}
