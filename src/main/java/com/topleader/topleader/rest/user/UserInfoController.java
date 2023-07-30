/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.rest.user;

import com.topleader.topleader.entity.user.UserInfo;
import com.topleader.topleader.service.user.UserInfoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.security.Principal;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping
    public UserInfoDto getUserInfo(@AuthenticationPrincipal Principal principal) {
        return UserInfoDto.from(userInfoService.find(principal.getName()));
    }

    @PostMapping("/strengths")
    public UserInfoDto setStrengths(@AuthenticationPrincipal Principal principal, @RequestBody @Valid SetDataRequestDto request) {
        return UserInfoDto.from(userInfoService.setStrengths(principal.getName(), request.data()));
    }

    public record UserInfoDto(
        String username,
        Set<String> strengths,
        Set<String> values,
        Set<String> areaOfDevelopment,
        String notes
    ) {
        public static UserInfoDto from(UserInfo info) {
            return new UserInfoDto(
                info.getUsername(),
                info.getStrengths(),
                info.getValues(),
                info.getAreaOfDevelopment(),
                info.getNotes()
            );
        }
    }

    public record SetDataRequestDto(@NotEmpty Set<String> data) {}
}
