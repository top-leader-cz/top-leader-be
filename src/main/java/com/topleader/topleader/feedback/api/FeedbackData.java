package com.topleader.topleader.feedback.api;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.PrimitiveIterator;


@Data
@Accessors(chain = true)
public class FeedbackData {

    private long formId;

    private LocalDateTime validTo;

    private List<Recipient> recipients;

    private String locale;

    private String firstName;

    private String lastName;

    public record Recipient(Long id, String recipient, String token) {}




}
