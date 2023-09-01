/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;


/**
 * @author Daniel Slavik
 */
@Service
@AllArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public List<ChatInfoDto> getUserChatInfo(String username) {
        final var unreadMessages = messageRepository.findAllByUserToAndDisplayed(username, Boolean.FALSE).stream()
            .collect(groupingBy(Message::getUserFrom, Collectors.maxBy(Comparator.comparing(Message::getCreatedAt))));
        final var unreadCountMap = messageRepository.getUnreadMessagesCount(username).stream()
            .collect(toMap(UnreadMessagesCount::getUserFrom, UnreadMessagesCount::getUnread));

        return Stream.concat(
                unreadMessages.keySet().stream(),
                unreadCountMap.keySet().stream()
            ).collect(Collectors.toSet()).stream()
            .map(userFrom -> new ChatInfoDto(
                userFrom,
                unreadCountMap.get(userFrom),
                unreadMessages.getOrDefault(userFrom, Optional.empty()).map(Message::getMessageData).orElse(null))
            )
            .toList();


    }

    @Transactional
    public Page<Message> findUserMessages(String username, String addressee, Pageable pageable) {
        markAllMessagesAsDisplayed(username, addressee);
        final var addresses = Set.of(username, addressee);

        return messageRepository.findByUserFromInAndUserToIn(addresses, addresses, pageable);
    }

    @Transactional
    public void sendMessage(String username, String addressee, String messageData) {

        final var time = LocalDateTime.now();

        messageRepository.save(
            new Message()
                .setUserFrom(username)
                .setUserTo(addressee)
                .setMessageData(messageData)
                .setCreatedAt(time)
                .setDisplayed(true)
        );

    }

    public void markAllMessagesAsDisplayed(String username, String addressee) {
        messageRepository.setAllUserMessagesAsDisplayed(username, addressee);
    }

    public record ChatInfoDto(String username, Long unreadMessageCount, String lastMessage) {
    }
}
