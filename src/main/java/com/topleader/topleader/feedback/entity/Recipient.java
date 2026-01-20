package com.topleader.topleader.feedback.entity;


import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Accessors(chain = true)
@Table("fb_recipient")
public class Recipient {

    @Id
    private Long id;

    private Long formId;

    private String recipient;

    private String token;

    private boolean submitted;

}
