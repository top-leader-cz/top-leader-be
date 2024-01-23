/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/messages")
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public List<UserChatInfoDto> getUserChatInfo(@AuthenticationPrincipal UserDetails user) {
        return messageService.getUserChatInfo(user.getUsername()).stream()
            .map(UserChatInfoDto::from)
            .toList();
    }

    @GetMapping("/{addressee}")
    public Page<MessageDto> getUserChat(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable String addressee, Pageable pageable
    ) {
        return messageService.findUserMessages(user.getUsername(), addressee, pageable)
            .map(MessageDto::from);
    }

    @PostMapping
    public void sendMessage(
        @AuthenticationPrincipal UserDetails user,
        @Valid @RequestBody MessageRequestDto requestDto
    ) {
        messageService.sendMessage(user.getUsername(), requestDto.userTo(), requestDto.messageData());
    }

    public record UserChatInfoDto(
        String username,
        Long unreadMessageCount,
        String lastMessage,
        LocalDateTime createdAt,
        String firstName,
        String lastName
    ) {
        public static UserChatInfoDto from(MessageService.ChatInfoDto chat) {
            return new UserChatInfoDto(
                chat.username(),
                chat.unreadMessageCount(),
                chat.lastMessage(),
                chat.createdAt(),
                chat.firstName(),
                chat.LastName()
            );
        }
    }

    public record MessageRequestDto(
        @NotBlank
        String userTo,
        @Size(min = 1, message = "Message length needs to be at least 1 character")
        @Size(max = 3000, message = "Message length can be max 3000 characters")
        String messageData
    ) {
    }

    public record MessageDto(
        Long id,
        String username,
        String addressee,
        String messageData,
        Boolean displayed,
        LocalDateTime createdAt
    ) {

        public static MessageDto from(Message m) {
            return new MessageDto(
                m.getId(),
                m.getUserFrom(),
                m.getUserTo(),
                m.getMessageData(),
                m.getDisplayed(),
                m.getCreatedAt()
            );
        }
    }
}
