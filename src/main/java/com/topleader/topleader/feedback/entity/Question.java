package com.topleader.topleader.feedback.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity(name = "fb_question")
@Accessors(chain = true)
@ToString(of={"key", "type"})
public class Question {

    @Id
    private String key;

    @OneToMany(mappedBy="question", cascade = {CascadeType.ALL, CascadeType.PERSIST})
    private List<Answer> answers;

}
