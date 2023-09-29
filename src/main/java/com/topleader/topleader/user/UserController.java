package com.topleader.topleader.user;


import com.topleader.topleader.user.token.TokenService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/latest/user")
@AllArgsConstructor
public class UserController {

    private final UserDetailService userDetailService;

    private final InvitationService invitationService;

    private final TokenService tokenService;

    @Secured({"ADMIN", "HR"})
    @PostMapping
    public UserDto addUser(@RequestBody @Valid AddUserRequest request) {
        var user = new User().setUsername(request.getUsername())
                .setAuthorities(request.getAuthorities())
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setTimeZone(request.getTimeZone())
                .setStatus(request.getStatus());

        var saved = userDetailService.save(user);
        if(sendInvite(User.Status.PENDING, request.getStatus())) {
           invitationService.sendInvite(request);
        }
        return UserDto.fromUser(saved);
    }

    @Secured({"ADMIN", "HR"})
    @PutMapping("/{username}")
    public UserDto updateUser(@PathVariable String username, @RequestBody @Valid UpdateUserRequest request) {
        var user = userDetailService.getUser(username);
        var oldStatus = user.getStatus();
        user.setStatus(request.getStatus());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setTimeZone(request.getTimeZone());
        user.setAuthorities(request.getAuthorities());

        if(sendInvite(oldStatus, request.getStatus())) {
            request.setUsername(username);
            invitationService.sendInvite(request);
        }
        return UserDto.fromUser(userDetailService.save(user));
    }

    private boolean sendInvite(User.Status oldStatus, User.Status newStatus) {
        return User.Status.PENDING == oldStatus && (User.Status.AUTHORIZED == newStatus || User.Status.PAID == newStatus);
    }

    public interface UserRequest {
        String getFirstName();

        String getLastName();

        String getUsername();

        String getLocale();
    }

    @Data
    public static class UpdateUserRequest implements UserRequest {

        @NotEmpty
        private String firstName;

        @NotEmpty
        private String lastName;

        private String username;

        @NotNull
        private User.Status status;

        @NotEmpty
        private String timeZone;

        @NotEmpty
        private Set<User.Authority> authorities;

        @Pattern(regexp = "[a-z]{2}")
        private String locale;
    }

    @Data
    public static class AddUserRequest implements UserRequest {
        @NotEmpty
        private String firstName;

        @NotEmpty
        private String lastName;

        @NotEmpty
        private Set<User.Authority> authorities;

        @NotEmpty
        private String username;

        @NotEmpty
        private String timeZone;

        @NotNull
        private User.Status status;

        @Pattern(regexp = "[a-z]{2}")
        private String locale;
    }


    public record UserDto (
            String username,
            String firstName,
            String lastName,
            String timeZone,
            User.Status status,

            Set<User.Authority> authorities

    ) {
        public static UserDto fromUser(User user) {
            return new UserDto(
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getTimeZone(),
                    user.getStatus(),
                    user.getAuthorities()
            );

        }
    }


}
