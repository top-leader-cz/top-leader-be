/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daniel Slavik
 */
@RestController
@RequestMapping("/api/latest/user-info")
@AllArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    private final UserRepository userRepository;


    @GetMapping
    public UserInfoDto getUserInfo(@AuthenticationPrincipal UserDetails user) {
        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            userRepository.findById(user.getUsername()).orElseThrow()
        );
    }

    @PostMapping("/strengths")
    public UserInfoDto setStrengths(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid ListDataRequestDto request) {
        return UserInfoDto.from(
            userInfoService.setStrengths(user.getUsername(), request.data()),
            userRepository.findById(user.getUsername()).orElseThrow()
        );
    }

    @PostMapping("/values")
    public UserInfoDto setValues(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid ListDataRequestDto request) {
        return UserInfoDto.from(
            userInfoService.setValues(user.getUsername(), request.data()),
            userRepository.findById(user.getUsername()).orElseThrow()
        );
    }

    @PostMapping("/timezone")
    public UserInfoDto setValues(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetTimezoneRequestDto request) {

        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            userRepository.findById(user.getUsername())
                .map(u -> u.setTimeZone(request.timezone()))
                .map(userRepository::save)
                .orElseThrow()
        );
    }

    public record UserInfoDto(
        String username,
        Set<User.Authority> userRoles,
        String timeZone,
        List<String> strengths,
        List<String> values,
        List<String> areaOfDevelopment,
        String notes
    ) {
        public static UserInfoDto from(UserInfo info, User user) {
            return new UserInfoDto(
                info.getUsername(),
                user.getAuthorities(),
                user.getTimeZone(),
                info.getStrengths(),
                info.getValues(),
                info.getAreaOfDevelopment(),
                info.getNotes()
            );
        }
    }

    public record ListDataRequestDto(@NotEmpty List<String> data) {
    }

    public record SetTimezoneRequestDto(@Size(min = 1, max = 20) String timezone) {
    }
}
