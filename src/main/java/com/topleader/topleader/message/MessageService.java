/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import com.topleader.topleader.common.email.Emailing;
import com.topleader.topleader.common.email.Templating;
import com.topleader.topleader.common.notification.Notification;
import com.topleader.topleader.common.notification.NotificationService;
import com.topleader.topleader.common.notification.context.MessageNotificationContext;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.topleader.topleader.common.util.common.user.UserUtils.getUserTimeZoneId;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;


/**
 * @author Daniel Slavik
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Map<String, String> subjects = Map.of(
            "en", "New Message Alert on TopLeader Platform",
            "cs", "Nová zpráva na platformě TopLeader",
            "fr", "Alerte Nouveau Message sur la Plateforme TopLeader",
            "de", "Neue Nachrichtenbenachrichtigung auf der TopLeader Plattform");

    private final MessageRepository messageRepository;

    private final UserChatRepository userChatRepository;

    private final LastMessageRepository lastMessageRepository;

    private final NotificationService notificationService;

    private final UserRepository userRepository;

    private final Templating velocityService;

    private final Emailing emailService;

    @Value("${top-leader.app-url}")
    private String appUrl;

    @Value("${top-leader.default-locale}")
    private String defaultLocale;

    @Value("${top-leader.supported-invitations}")
    private List<String> supportedInvitations;


    public List<ChatInfoDto> getUserChatInfo(String username) {

        final var allChats = userChatRepository.findAllForUser(username).stream()
            .collect(toMap(chat -> chat.getUser1().equals(username) ? chat.getUser2() : chat.getUser1(), UserChat::getChatId));

        if (allChats.isEmpty()) {
            return List.of();
        }

        final var lastMessages = Optional.of(lastMessageRepository.findAllByChatIdIn(allChats.values()))
            .filter(not(List::isEmpty))
            .map(c ->
                StreamSupport.stream(messageRepository.findAllById(c.stream().map(LastMessage::getMessageId).collect(Collectors.toSet())).spliterator(), false)
                    .collect(toMap(Message::getChatId, Function.identity()))
            ).orElse(Map.of());

        final var userZoneId = getUserTimeZoneId(userRepository.findByUsername(username));

        final var userInfos = userRepository.findAllByUsernameIn(allChats.keySet()).stream()
            .collect(toMap(User::getUsername, UserInfoDto::from));

        final var unreadCountMap = messageRepository.getUnreadMessagesCount(username).stream()
            .collect(toMap(UnreadMessagesCount::userfrom, UnreadMessagesCount::unread));

        return allChats.entrySet().stream()
            .filter(e -> lastMessages.containsKey(e.getValue())) // Only chats with messages
            .map(e -> new ChatInfoDto(
                e.getKey(),
                unreadCountMap.getOrDefault(e.getKey(), 0L),
                lastMessages.get(e.getValue()).getMessageData(),
                lastMessages.get(e.getValue()).getCreatedAt().atZone(userZoneId)
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime(),
                userInfos.getOrDefault(e.getKey(), UserInfoDto.EMPTY).firstName(),
                userInfos.getOrDefault(e.getKey(), UserInfoDto.EMPTY).lastName()
            ))
            .toList();
    }

    public Page<Message> findUserMessages(String username, String addressee, Pageable pageable) {
        markAllMessagesAsDisplayed(username);
        return userChatRepository.findUserChat(username, addressee)
            .map(userChat -> messageRepository.findAllByChatId(userChat.getChatId(), pageable))
            .orElse(Page.empty());
    }

    @Transactional
    public void sendMessage(String username, String addressee, String messageData) {

        final var chat = userChatRepository.findUserChat(username, addressee)
            .orElseGet(() -> userChatRepository.save(new UserChat().setUser1(username).setUser2(addressee)));

        final var time = LocalDateTime.now();

        final var message = messageRepository.save(
            new Message()
                .setChatId(chat.getChatId())
                .setUserFrom(username)
                .setUserTo(addressee)
                .setMessageData(messageData)
                .setCreatedAt(time)
                .setDisplayed(false)
        );

        lastMessageRepository.upsert(chat.getChatId(), message.getId());

        final var user = userRepository.findByUsername(username).orElseThrow();

        notificationService.addNotification(
            new NotificationService.CreateNotificationRequest(
                addressee,
                Notification.Type.MESSAGE,
                new MessageNotificationContext()
                    .setFromUser(user.getUsername())
                    .setFromUserFirstName(user.getFirstName())
                    .setFromUserLastName(user.getLastName())
                    .setMessage(messageData)
            )
        );

    }

    public void markAllMessagesAsDisplayed(String username) {
        messageRepository.setAllUserMessagesAsDisplayed(username);
    }

    public void processNotDisplayedMessages() {
        var usersToNotify = messageRepository.findUndisplayed();
        log.info("User to receive message display notifications: {}", usersToNotify);

        usersToNotify.forEach(user ->
                userRepository.findByUsername(user).ifPresent(userToNotify -> {
                    var params = Map.of("firstName", userToNotify.getFirstName(), "lastName", userToNotify.getLastName(), "link", appUrl);
                    var emailBody = velocityService.getMessage(new HashMap<>(params), parseTemplateName(userToNotify.getLocale()));
                    emailService.sendEmail(userToNotify.getEmail(), subjects.getOrDefault(userToNotify.getLocale(), defaultLocale), emailBody);
                })
        );

        if (!usersToNotify.isEmpty()) {
            messageRepository.setNotified(usersToNotify);
        }
    }

    public String parseTemplateName(String locale) {
        return "templates/message/notification-" +parseLocale(locale) + ".html";
    }

    public String parseLocale(String locale) {
        return supportedInvitations.contains(locale) ? locale : defaultLocale;
    }

    public record ChatInfoDto(
        String username,
        Long unreadMessageCount,
        String lastMessage,
        LocalDateTime createdAt,
        String firstName,
        String LastName
    ) {
    }

    public record UserInfoDto(String firstName, String lastName) {
        public static UserInfoDto EMPTY = new UserInfoDto(null, null);
        public static UserInfoDto from(User u) {
            return new UserInfoDto(u.getFirstName(), u.getLastName());
        }
    }
}
