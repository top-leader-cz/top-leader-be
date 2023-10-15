package com.topleader.topleader.feedback.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Accessors(chain = true)
public class FeedbackForm {

    @Id
    private Long id;

    private String title;

    private String description;

    private String link;

    private LocalDateTime validTo;

    @OneToMany(mappedBy = "feedbackForm", cascade = { CascadeType.ALL }, orphanRemoval = true)
    private Set<FeedbackFormQuestion> questions = new HashSet<>();


    @OneToMany(mappedBy = "feedbackForm", cascade = { CascadeType.ALL }, orphanRemoval = true)
    private Set<FeedbackFormAnswer> answers = new HashSet<>();

    @OneToMany(mappedBy="feedbackForm", cascade = {CascadeType.PERSIST, CascadeType.PERSIST})
    private Set<Recipient> recipients;

}
