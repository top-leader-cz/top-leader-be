package com.topleader.topleader.feedback.api;

import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.Recipient;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class FeedbackForms {

    private long id;

    private String title;

    private LocalDateTime createdAt;

    private boolean draft;

    private List<RecipientDto> recipients;


    public static List<FeedbackForms> of(List<FeedbackForm> forms, Map<Long, List<Recipient>> recipientsByFormId) {
        return forms.stream()
                .map(f -> {
                    var recipients = recipientsByFormId.getOrDefault(f.getId(), List.of()).stream()
                            .map(r -> new RecipientDto(r.getId(), r.getRecipient(), r.isSubmitted()))
                            .toList();
                    return new FeedbackForms()
                            .setId(f.getId())
                            .setTitle(f.getTitle())
                            .setCreatedAt(f.getCreatedAt())
                            .setRecipients(recipients)
                            .setDraft(f.isDraft());
                        }
                )
                .toList();
    }

}
