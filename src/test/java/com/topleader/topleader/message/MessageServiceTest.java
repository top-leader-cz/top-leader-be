package com.topleader.topleader.message;

import com.topleader.topleader.TestProxy;
import com.topleader.topleader.common.email.Emailing;
import com.topleader.topleader.common.email.Templating;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MessageServiceTest {

    record TestContext(
            MessageService service,
            TestProxy<MessageRepository> messageRepo,
            TestProxy<Emailing> emailService
    ) {}

    @SuppressWarnings("unchecked")
    private TestContext createService(List<String> undisplayedUsers, User... users) {
        var messageRepo = TestProxy.of(MessageRepository.class)
                .stub("findUndisplayed", undisplayedUsers)
                .stub("setNotified", args -> null)
                .build();

        var userRepo = TestProxy.of(UserRepository.class)
                .stub("findByUsername", args -> {
                    for (var user : users) {
                        if (user.getUsername().equals(args[0])) return Optional.of(user);
                    }
                    return Optional.empty();
                })
                .build();

        Templating velocityService = (params, template) -> "<html>Email body</html>";

        var emailProxy = TestProxy.of(Emailing.class)
                .stub("sendEmail", args -> null)
                .build();

        var service = new MessageService(
                messageRepo.proxy(), null, null, null,
                userRepo.proxy(), velocityService, emailProxy.proxy(), null
        );
        ReflectionTestUtils.setField(service, "appUrl", "https://test.com");
        ReflectionTestUtils.setField(service, "defaultLocale", "en");
        ReflectionTestUtils.setField(service, "supportedInvitations", List.of("en", "cs"));

        return new TestContext(service, messageRepo, emailProxy);
    }

    @Test
    void processNotDisplayedMessages_shouldNotCallSetNotified_whenNoUsersToNotify() {
        var ctx = createService(List.of());

        ctx.service().processNotDisplayedMessages();

        assertThat(ctx.messageRepo().wasNotCalled("setNotified")).isTrue();
        assertThat(ctx.emailService().wasNotCalled("sendEmail")).isTrue();
    }

    @Test
    void processNotDisplayedMessages_shouldCallSetNotified_whenUsersToNotifyExist() {
        var testUser = new User()
            .setUsername("testuser")
            .setEmail("test@example.com")
            .setFirstName("Test")
            .setLastName("User")
            .setLocale("en");

        var ctx = createService(List.of("testuser"), testUser);

        ctx.service().processNotDisplayedMessages();

        var emailCalls = ctx.emailService().invocationsOf("sendEmail");
        assertThat(emailCalls).hasSize(1);
        assertThat(emailCalls.get(0)[0]).isEqualTo("test@example.com");
        assertThat(emailCalls.get(0)[1]).isEqualTo("New Message Alert on TopLeader Platform");
        assertThat(emailCalls.get(0)[2]).isEqualTo("<html>Email body</html>");

        var notifyCalls = ctx.messageRepo().invocationsOf("setNotified");
        assertThat(notifyCalls).hasSize(1);
        assertThat((List<String>) notifyCalls.get(0)[0]).containsExactly("testuser");
    }

    @Test
    void processNotDisplayedMessages_shouldStillNotifyWhenUserRepositoryReturnsEmpty() {
        var ctx = createService(List.of("nonexistentuser"));

        ctx.service().processNotDisplayedMessages();

        assertThat(ctx.emailService().wasNotCalled("sendEmail")).isTrue();
        // setNotified is still called with the list even though emails weren't sent
        var notifyCalls = ctx.messageRepo().invocationsOf("setNotified");
        assertThat(notifyCalls).hasSize(1);
        assertThat((List<String>) notifyCalls.get(0)[0]).containsExactly("nonexistentuser");
    }
}
