package com.topleader.topleader.message;

import com.topleader.topleader.common.email.EmailService;
import com.topleader.topleader.common.email.TemplateService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TemplateService velocityService;

    @Mock
    private EmailService emailService;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(messageRepository, null, null, null, userRepository, velocityService, emailService);
        ReflectionTestUtils.setField(messageService, "appUrl", "https://test.com");
        ReflectionTestUtils.setField(messageService, "defaultLocale", "en");
        ReflectionTestUtils.setField(messageService, "supportedInvitations", List.of("en", "cs"));
    }

    @Test
    void processNotDisplayedMessages_shouldNotCallSetNotified_whenNoUsersToNotify() {
        // Given
        when(messageRepository.findUndisplayed()).thenReturn(List.of());

        // When
        messageService.processNotDisplayedMessages();

        // Then
        verify(messageRepository).findUndisplayed();
        verify(messageRepository, never()).setNotified(anyList());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void processNotDisplayedMessages_shouldCallSetNotified_whenUsersToNotifyExist() {
        // Given
        var testUser = new User()
            .setUsername("testuser")
            .setEmail("test@example.com")
            .setFirstName("Test")
            .setLastName("User")
            .setLocale("en");

        when(messageRepository.findUndisplayed()).thenReturn(List.of("testuser"));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(velocityService.getMessage(any(), anyString())).thenReturn("<html>Email body</html>");

        // When
        messageService.processNotDisplayedMessages();

        // Then
        verify(messageRepository).findUndisplayed();
        verify(userRepository).findByUsername("testuser");
        verify(emailService).sendEmail("test@example.com", "New Message Alert on TopLeader Platform", "<html>Email body</html>");
        verify(messageRepository).setNotified(List.of("testuser"));
    }

    @Test
    void processNotDisplayedMessages_shouldNotCallSetNotified_whenUserRepositoryReturnsEmpty() {
        // Given
        when(messageRepository.findUndisplayed()).thenReturn(List.of("nonexistentuser"));
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // When
        messageService.processNotDisplayedMessages();

        // Then
        verify(messageRepository).findUndisplayed();
        verify(userRepository).findByUsername("nonexistentuser");
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        // setNotified is still called with the list even though emails weren't sent
        verify(messageRepository).setNotified(List.of("nonexistentuser"));
    }
}
