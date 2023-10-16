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

    @EmbeddedId
    private RecipientId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("formId")
    private FeedbackForm form;

    @Column(name = "recipient", insertable=false, updatable=false)
    private String recipient;

    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecipientId implements Serializable {

        @Column(name = "form_id")
        private Long formId;

        @Column(name = "recipient")
        private String recipient;
    }
}
