/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import com.topleader.topleader.notification.Notification;
import com.topleader.topleader.notification.NotificationService;
import com.topleader.topleader.notification.context.MessageNotificationContext;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.topleader.topleader.util.common.user.UserUtils.getUserTimeZoneId;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;


/**
 * @author Daniel Slavik
 */
@Service
@AllArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    private final UserChatRepository userChatRepository;

    private final LastMessageRepository lastMessageRepository;

    private final NotificationService notificationService;

    private final UserRepository userRepository;

    public List<ChatInfoDto> getUserChatInfo(String username) {

        final var allChats = userChatRepository.findAllForUser(username).stream()
            .collect(toMap(chat -> chat.getUser1().equals(username) ? chat.getUser2() : chat.getUser1(), UserChat::getChatId));

        final var lastMessages = Optional.of(lastMessageRepository.findAllByChatIdIn(allChats.values()))
            .filter(not(List::isEmpty))
            .map(c ->
                messageRepository.findAllById(c.stream().map(LastMessage::getMessageId).collect(Collectors.toSet())).stream()
                    .collect(toMap(Message::getChatId, Function.identity()))
            ).orElse(Map.of());

        final var userZoneId = getUserTimeZoneId(userRepository.findById(username));

        final var userInfos = userRepository.findAllById(allChats.keySet()).stream()
            .collect(toMap(User::getUsername, UserInfoDto::from));

        final var unreadCountMap = messageRepository.getUnreadMessagesCount(username).stream()
            .collect(toMap(UnreadMessagesCount::getUserFrom, UnreadMessagesCount::getUnread));

        return allChats.entrySet().stream()
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

    @Transactional
    public Page<Message> findUserMessages(String username, String addressee, Pageable pageable) {

        return userChatRepository.findUserChat(username, addressee)
            .map(userChat -> {
                markAllMessagesAsDisplayed(username, addressee);
                return messageRepository.findAllByChatId(userChat.getChatId(), pageable);
            })
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
                .setDisplayed(true)
        );

        lastMessageRepository.save(
            new LastMessage()
                .setChatId(chat.getChatId())
                .setMessageId(message.getId())
        );

        notificationService.addNotification(
            new NotificationService.CreateNotificationRequest(
                addressee,
                Notification.Type.MESSAGE,
                new MessageNotificationContext()
                    .setFromUser(username)
                    .setMessage(messageData)
            )
        );

    }

    public void markAllMessagesAsDisplayed(String username, String addressee) {
        messageRepository.setAllUserMessagesAsDisplayed(username, addressee);
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
