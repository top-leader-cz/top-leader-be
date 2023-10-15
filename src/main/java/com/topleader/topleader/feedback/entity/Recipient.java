package com.topleader.topleader.feedback.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Entity(name = "fb_recipient")
@Accessors(chain = true)
public class Recipient {

    @Id
    private Long id;

    private String recipient;

    @ManyToOne
    @JoinColumn(name = "form_id", insertable = false, updatable = false)
    private FeedbackForm feedbackForm;
}
