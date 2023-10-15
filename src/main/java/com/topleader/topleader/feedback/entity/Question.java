package com.topleader.topleader.feedback.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity(name = "fb_question")
@Accessors(chain = true)
public class Question {

    @Id
    private String key;

    @Enumerated(EnumType.STRING)
    private Type type;

//    @OneToMany(mappedBy = "question", cascade = { CascadeType.ALL }, orphanRemoval = true)
//    private Set<FeedbackFormQuestion> forms = new HashSet<>();

    @OneToMany(mappedBy="question", cascade = {CascadeType.PERSIST, CascadeType.PERSIST})
    private Set<Answer> answers;

      public enum Type {
        PARAGRAPH,
        SCALE
    }
}
