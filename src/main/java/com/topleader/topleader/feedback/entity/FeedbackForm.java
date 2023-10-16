package com.topleader.topleader.feedback.entity;


import com.topleader.topleader.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private LocalDateTime validTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username")
    private User user;

    @OneToMany(mappedBy = "feedbackForm", cascade = { CascadeType.ALL }, orphanRemoval = true)
    private List<FeedbackFormQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "feedbackForm", cascade = { CascadeType.ALL }, orphanRemoval = true)
    private List<FeedbackFormAnswer> answers = new ArrayList<>();

    @OneToMany(mappedBy="form", cascade = {CascadeType.PERSIST, CascadeType.PERSIST})
    private List<Recipient> recipients = new ArrayList<>();



}
