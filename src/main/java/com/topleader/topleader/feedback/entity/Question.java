package com.topleader.topleader.feedback.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;
import com.topleader.topleader.common.entity.BaseEntity;


@Data
@EqualsAndHashCode(callSuper=false)
@Accessors(chain = true)
@Table("fb_question")
public class Question extends BaseEntity {
    private String key;

    private boolean defaultQuestion;
}
