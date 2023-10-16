package com.topleader.topleader.feedback.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity(name = "fb_answer")
@ToString(of={"key"})
public class Answer {

    @Id
    private String  key;

    @ManyToOne
    @JoinColumn(name = "question_key", insertable = false, updatable = false)
    private Question question;


}
