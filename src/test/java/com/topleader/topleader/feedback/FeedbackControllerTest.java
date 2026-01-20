package com.topleader.topleader.feedback;

import com.topleader.topleader.feedback.api.FeedbackData;
import com.topleader.topleader.feedback.api.FeedbackFormRequest;
import com.topleader.topleader.feedback.api.RecipientDto;
import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.FeedbackFormQuestion;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.feedback.repository.FeedbackFormQuestionRepository;
import com.topleader.topleader.feedback.repository.RecipientRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FeedbackControllerTest {

    private StubUserRepository userRepository;
    private StubRecipientRepository recipientRepository;
    private FeedbackController feedbackController;

    @BeforeEach
    void setUp() {
        userRepository = new StubUserRepository();
        recipientRepository = new StubRecipientRepository();
        var feedbackFormQuestionRepository = new StubFeedbackFormQuestionRepository();
        feedbackController = new FeedbackController(
                null, // feedbackService - not used in tested method
                feedbackFormQuestionRepository,
                recipientRepository,
                userRepository
        );
    }

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

        userRepository.setUser(user);
        recipientRepository.setRecipients(List.of(existingRecipient));

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

        userRepository.setUser(user);
        recipientRepository.setRecipients(List.of(existingRecipient));

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

        userRepository.setUser(user);
        recipientRepository.setRecipients(List.of(recipient1, recipient2));

        // When
        var result = feedbackController.getFeedbackData(request, form);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecipients()).hasSize(2);
        assertThat(result.getRecipients())
                .extracting(FeedbackData.Recipient::recipient)
                .containsExactlyInAnyOrder("recipient1@example.com", "recipient2@example.com");
    }

    // Stub implementations for testing

    private static class StubUserRepository implements UserRepository {
        private User user;

        void setUser(User user) {
            this.user = user;
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.ofNullable(user);
        }

        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public List<User> findAllByUsernameIn(Collection<String> usernames) { return List.of(); }
        @Override public Set<String> findAllowedCoachRates(String username) { return Set.of(); }
        @Override public void deleteAllowedCoachRates(String username) { }
        @Override public void insertAllowedCoachRate(String username, String rateName) { }
        @Override public Optional<User> findByUsernameOrEmail(String username) { return Optional.empty(); }
        @Override public <S extends User> S save(S entity) { return entity; }
        @Override public <S extends User> List<S> saveAll(Iterable<S> entities) { return new ArrayList<>(); }
        @Override public Optional<User> findById(Long id) { return Optional.empty(); }
        @Override public boolean existsById(Long id) { return false; }
        @Override public List<User> findAll() { return List.of(); }
        @Override public List<User> findAllById(Iterable<Long> ids) { return List.of(); }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long id) { }
        @Override public void delete(User entity) { }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { }
        @Override public void deleteAll(Iterable<? extends User> entities) { }
        @Override public void deleteAll() { }
    }

    private static class StubRecipientRepository implements RecipientRepository {
        private List<Recipient> recipients = new ArrayList<>();

        void setRecipients(List<Recipient> recipients) {
            this.recipients = recipients;
        }

        @Override
        public List<Recipient> findByFormId(long formId) {
            return recipients;
        }

        @Override public Optional<Recipient> findByFormIdAndRecipientAndToken(long formId, String recipient, String token) { return Optional.empty(); }
        @Override public void deleteByFormId(long formId) { }
        @Override public void deleteByFormIdAndIdNotIn(long formId, List<Long> keepIds) { }
        @Override public <S extends Recipient> S save(S entity) { return entity; }
        @Override public <S extends Recipient> Iterable<S> saveAll(Iterable<S> entities) { return new ArrayList<>(); }
        @Override public Optional<Recipient> findById(Long id) { return Optional.empty(); }
        @Override public boolean existsById(Long id) { return false; }
        @Override public Iterable<Recipient> findAll() { return recipients; }
        @Override public Iterable<Recipient> findAllById(Iterable<Long> ids) { return List.of(); }
        @Override public long count() { return recipients.size(); }
        @Override public void deleteById(Long id) { }
        @Override public void delete(Recipient entity) { }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { }
        @Override public void deleteAll(Iterable<? extends Recipient> entities) { }
        @Override public void deleteAll() { }
    }

    private static class StubFeedbackFormQuestionRepository implements FeedbackFormQuestionRepository {
        @Override public List<FeedbackFormQuestion> findByFeedbackFormId(long feedbackFormId) { return List.of(); }
        @Override public Optional<FeedbackFormQuestion> findByFeedbackFormIdAndQuestionKey(long feedbackFormId, String questionKey) { return Optional.empty(); }
        @Override public void deleteByFeedbackFormId(long feedbackFormId) { }
        @Override public <S extends FeedbackFormQuestion> S save(S entity) { return entity; }
        @Override public <S extends FeedbackFormQuestion> Iterable<S> saveAll(Iterable<S> entities) { return new ArrayList<>(); }
        @Override public Optional<FeedbackFormQuestion> findById(Long id) { return Optional.empty(); }
        @Override public boolean existsById(Long id) { return false; }
        @Override public Iterable<FeedbackFormQuestion> findAll() { return List.of(); }
        @Override public Iterable<FeedbackFormQuestion> findAllById(Iterable<Long> ids) { return List.of(); }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long id) { }
        @Override public void delete(FeedbackFormQuestion entity) { }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { }
        @Override public void deleteAll(Iterable<? extends FeedbackFormQuestion> entities) { }
        @Override public void deleteAll() { }
    }
}
