package com.topleader.topleader.feedback.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;


@Data
@Entity(name = "fb_question")
@Accessors(chain = true)
@ToString(of={"key"})
@EqualsAndHashCode(of = {"key"})
public class Question {

    @Id
    private String key;

    private boolean defaultQuestion;

}
