package com.topleader.topleader.feedback.entity;


import com.topleader.topleader.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Accessors(chain = true)
@ToString(of={"id", "title", "description", "validTo"})
public class FeedbackForm {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feedback_form_id_seq")
    @SequenceGenerator(name = "feedback_form_id_seq", sequenceName = "feedback_form_id_seq", allocationSize = 1)
    private Long id;

    private String title;

    private String description;

    private LocalDateTime validTo;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username")
    private User user;

    @OneToMany(mappedBy = "form", cascade = {CascadeType.MERGE}, orphanRemoval = true)
    private List<FeedbackFormQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "form", cascade = {CascadeType.MERGE}, orphanRemoval = true)
    private List<FeedbackFormAnswer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "form", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Recipient> recipients = new ArrayList<>();



}
