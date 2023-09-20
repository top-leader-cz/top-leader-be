package com.topleader.topleader.user;


import com.topleader.topleader.user.userinfo.UserInfo;
import com.topleader.topleader.user.userinfo.UserInfoService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/latest/user")
@AllArgsConstructor
public class UserController {

    private final UserDetailService userDetailService;

    private final InvitationService invitationService;

    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public void addMember(@RequestBody @Valid AddUserRequest request) {
        var user = new User().setUsername(request.getEmail())
                .setPassword(passwordEncoder.encode(request.getPassword()))
                .setTimeZone(request.getTimezone())
                .setEnabled(true)
                .setUserInfo(new UserInfo().setUsername(request.getEmail()).setStatus(request.getStatus()));

        userDetailService.addUser(user);
        if(sendInvite(request.getStatus())) {
            invitationService.sendInvite(request);
        }
    }

    private boolean sendInvite(UserInfo.Status status) {
        return UserInfo.Status.AUTHORIZED == status|| UserInfo.Status.PAID == status;
    }

    @Data
    public static class AddUserRequest {
        @NotEmpty
        private String firstName;

        @NotEmpty
        private String lastName;

        @NotEmpty
        private String password;

        @NotEmpty
        private String email;

        @NotEmpty
        private String timezone;

        @NotNull
        private UserInfo.Status status;

        private String locale;


    }
}
