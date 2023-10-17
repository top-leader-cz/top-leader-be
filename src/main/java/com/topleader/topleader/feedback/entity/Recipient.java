package com.topleader.topleader.feedback.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Entity(name = "fb_recipient")
@Accessors(chain = true)
public class Recipient {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("formId")
    private FeedbackForm form;

    @Column(name = "recipient", insertable=false, updatable=false)
    private String recipient;

    private String token;

    private boolean submitted;

}
