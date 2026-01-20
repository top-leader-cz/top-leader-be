package com.topleader.topleader.feedback;

import com.topleader.topleader.feedback.api.FeedbackData;
import com.topleader.topleader.feedback.api.FeedbackFormRequest;
import com.topleader.topleader.feedback.api.RecipientDto;
import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.feedback.repository.FeedbackFormQuestionRepository;
import com.topleader.topleader.feedback.repository.RecipientRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackControllerTest {

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private FeedbackFormQuestionRepository feedbackFormQuestionRepository;

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedbackController feedbackController;

    @Test
    void getFeedbackData_shouldHandleDuplicateRecipients() {
        // Given
        var formId = 1L;
        var username = "test@example.com";
        var recipientEmail = "recipient@example.com";

        var form = new FeedbackForm()
                .setId(formId)
                .setUsername(username);

        var user = new User()
                .setUsername(username)
                .setFirstName("Test")
                .setLastName("User");

        // Recipients in request - one with ID (existing), one without ID (duplicate)
        var request = new FeedbackFormRequest()
                .setId(formId)
                .setTitle("Test Form")
                .setDescription("Description")
                .setUsername(username)
                .setValidTo(LocalDateTime.now())
                .setQuestions(List.of())
                .setRecipients(List.of(
                        new RecipientDto(323L, recipientEmail, false),
                        new RecipientDto(null, recipientEmail, false)
                ))
                .setLocale("en")
                .setDraft(false);

        // Existing recipient in database
        var existingRecipient = new Recipient()
                .setId(323L)
                .setFormId(formId)
                .setRecipient(recipientEmail)
                .setToken("token123")
                .setSubmitted(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(recipientRepository.findByFormId(formId)).thenReturn(List.of(existingRecipient));

        // When
        var result = feedbackController.getFeedbackData(request, form);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecipients()).hasSize(1);
        assertThat(result.getRecipients().get(0).recipient()).isEqualTo(recipientEmail);
        assertThat(result.getRecipients().get(0).id()).isEqualTo(323L);
        assertThat(result.getRecipients().get(0).token()).isEqualTo("token123");
    }

    @Test
    void getFeedbackData_shouldPreferRecipientWithId() {
        // Given
        var formId = 1L;
        var username = "test@example.com";
        var recipientEmail = "recipient@example.com";

        var form = new FeedbackForm()
                .setId(formId)
                .setUsername(username);

        var user = new User()
                .setUsername(username)
                .setFirstName("Test")
                .setLastName("User");

        // Request with duplicate recipients - different order (null ID first)
        var request = new FeedbackFormRequest()
                .setId(formId)
                .setTitle("Test Form")
                .setDescription("Description")
                .setUsername(username)
                .setValidTo(LocalDateTime.now())
                .setQuestions(List.of())
                .setRecipients(List.of(
                        new RecipientDto(null, recipientEmail, false),
                        new RecipientDto(323L, recipientEmail, false)
                ))
                .setLocale("en")
                .setDraft(false);

        var existingRecipient = new Recipient()
                .setId(323L)
                .setFormId(formId)
                .setRecipient(recipientEmail)
                .setToken("token123")
                .setSubmitted(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(recipientRepository.findByFormId(formId)).thenReturn(List.of(existingRecipient));

        // When
        var result = feedbackController.getFeedbackData(request, form);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecipients()).hasSize(1);
        // Should prefer the one with ID
        assertThat(result.getRecipients().get(0).id()).isEqualTo(323L);
    }

    @Test
    void getFeedbackData_shouldHandleMultipleDifferentRecipients() {
        // Given
        var formId = 1L;
        var username = "test@example.com";

        var form = new FeedbackForm()
                .setId(formId)
                .setUsername(username);

        var user = new User()
                .setUsername(username)
                .setFirstName("Test")
                .setLastName("User");

        // Multiple different recipients
        var request = new FeedbackFormRequest()
                .setId(formId)
                .setTitle("Test Form")
                .setDescription("Description")
                .setUsername(username)
                .setValidTo(LocalDateTime.now())
                .setQuestions(List.of())
                .setRecipients(List.of(
                        new RecipientDto(1L, "recipient1@example.com", false),
                        new RecipientDto(2L, "recipient2@example.com", false),
                        new RecipientDto(null, "recipient3@example.com", false)
                ))
                .setLocale("en")
                .setDraft(false);

        var recipient1 = new Recipient()
                .setId(1L)
                .setFormId(formId)
                .setRecipient("recipient1@example.com")
                .setToken("token1")
                .setSubmitted(false);

        var recipient2 = new Recipient()
                .setId(2L)
                .setFormId(formId)
                .setRecipient("recipient2@example.com")
                .setToken("token2")
                .setSubmitted(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(recipientRepository.findByFormId(formId)).thenReturn(List.of(recipient1, recipient2));

        // When
        var result = feedbackController.getFeedbackData(request, form);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecipients()).hasSize(2);
        assertThat(result.getRecipients())
                .extracting(FeedbackData.Recipient::recipient)
                .containsExactlyInAnyOrder("recipient1@example.com", "recipient2@example.com");
    }
}
