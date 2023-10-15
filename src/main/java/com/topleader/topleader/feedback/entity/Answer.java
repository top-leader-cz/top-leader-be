package com.topleader.topleader.feedback.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity(name = "fb_answer")
public class Answer {

    @Id
    private String  key;

    @ManyToOne
    @JoinColumn(name = "question_key", insertable = false, updatable = false)
    private Question question;


}
