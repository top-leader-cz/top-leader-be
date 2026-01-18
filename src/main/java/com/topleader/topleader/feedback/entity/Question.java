package com.topleader.topleader.feedback.entity;


import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Accessors(chain = true)
@Table("fb_question")
public class Question {

    @Id
    private Long id;

    private String key;

    private boolean defaultQuestion;
}
