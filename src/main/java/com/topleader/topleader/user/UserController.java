package com.topleader.topleader.user;


import com.topleader.topleader.user.userinfo.UserInfo;
import com.topleader.topleader.user.userinfo.UserInfoController;
import com.topleader.topleader.user.userinfo.UserInfoService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/latest/user")
@AllArgsConstructor
public class UserController {

    private final UserDetailService userDetailService;

    private final InvitationService invitationService;

    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public UserDto addUser(@RequestBody @Valid AddUserRequest request) {
        var user = new User().setUsername(request.getUsername())
                .setPassword(passwordEncoder.encode(request.getPassword()))
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setTimeZone(request.getTimeZone())
                .setStatus(request.getStatus())
                .setEnabled(true);

        var saved = userDetailService.save(user);
        if(sendInvite(request.getStatus())) {
            invitationService.sendInvite(request);
        }
        return UserDto.fromUser(saved);
    }

    @PutMapping("/{username}")
    public UserDto updateUser(@PathVariable String username, @RequestBody @Valid UpdateUserRequest request) {
        var user = userDetailService.getUser(username);
        user.setStatus(request.getStatus());
        return UserDto.fromUser(userDetailService.save(user));
    }

    private boolean sendInvite(User.Status status) {
        return User.Status.AUTHORIZED == status|| User.Status.PAID == status;
    }

    @Data
    public static class UpdateUserRequest {

        @NotNull
        private User.Status status;
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
        private String username;

        @NotEmpty
        private String timeZone;

        @NotNull
        private User.Status status;

//        @Pattern(regexp = "[a-z]{2}")
        private String locale;
    }


    public record UserDto (
            String username,
            String firstName,
            String lastName,
            String timeZone,
            User.Status status

    ) {
        public static UserDto fromUser(User user) {
            return new UserDto(
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getTimeZone(),
                    user.getStatus()
            );

        }
    }
}
