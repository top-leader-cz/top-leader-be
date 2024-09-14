package com.topleader.topleader.feedback.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "fb_recipient")
@Accessors(chain = true)
@ToString(of={"id", "form", "recipient", "token", "submitted"})
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fb_recipient_id_seq")
    @SequenceGenerator(name = "fb_recipient_id_seq", sequenceName = "fb_recipient_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private FeedbackForm form;

    @Column(name = "recipient")
    private String recipient;

    private String token;

    private boolean submitted;

}
