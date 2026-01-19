package com.topleader.topleader.user.userinsight.article;

import com.topleader.topleader.user.session.domain.UserArticle;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import com.topleader.topleader.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(of = {"id", "username"})
@Table("article")
@Accessors(chain = true)
public class Article extends BaseEntity {
    private String username;

    private UserArticle content;

    private LocalDateTime createdAt;
}
