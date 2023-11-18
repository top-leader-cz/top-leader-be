package com.topleader.topleader.user;


import com.topleader.topleader.user.exception.UserValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/latest/user")
@AllArgsConstructor
public class UserController {

    private final UserDetailService userDetailService;

    private final InvitationService invitationService;

    @Secured({"ADMIN"})
    @PostMapping
    public UserDto addUser(@AuthenticationPrincipal UserDetails loggedUser, @RequestBody @Valid AddUserRequest request) {
        var user = new User().setUsername(request.username())
            .setAuthorities(request.authorities())
            .setFirstName(request.firstName())
            .setLastName(request.lastName())
            .setTimeZone(request.timeZone())
            .setRequestedBy(loggedUser.getUsername())
            .setStatus(request.status());

        userDetailService.getUser(request.username())
                .ifPresent(u -> {
                    throw new UserValidationException("User already  exits! Username: " + u.getUsername());
                });

        var saved = userDetailService.save(user);
        if (sendInvite(User.Status.PENDING, request.status())) {
            invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(user, request.locale()));
        }
        return UserDto.fromUser(saved);
    }

    @Secured({"ADMIN"})
    @PutMapping("/{username}")
    public UserDto updateUser(@PathVariable String username, @RequestBody @Valid UpdateUserRequest request) {
        var user = userDetailService.getUser(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        var oldStatus = user.getStatus();
        user.setStatus(request.status);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setTimeZone(request.timeZone());
        user.setAuthorities(request.authorities());

        final var updatedUser = userDetailService.save(user);

        if (sendInvite(oldStatus, request.status())) {
            invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(updatedUser, request.locale()));
        }
        return UserDto.fromUser(updatedUser);
    }

    private boolean sendInvite(User.Status oldStatus, User.Status newStatus) {
        return User.Status.PENDING == oldStatus && (User.Status.AUTHORIZED == newStatus || User.Status.PAID == newStatus);
    }

    public record UpdateUserRequest(
        @NotEmpty
        String firstName,

        @NotEmpty
        String lastName,

        String username,

        @NotNull
        User.Status status,

        @NotEmpty
        String timeZone,

        @NotEmpty
        Set<User.Authority> authorities,

        @Pattern(regexp = "[a-z]{2}")
        String locale

    ) {
    }


    public record AddUserRequest(
        @NotEmpty
        String firstName,

        @NotEmpty
        String lastName,

        @NotEmpty
        Set<User.Authority> authorities,

        @NotEmpty
        String username,

        @NotEmpty
        String timeZone,

        @NotNull
        User.Status status,

        @Pattern(regexp = "[a-z]{2}")
        String locale
    ) {
    }


    public record UserDto(
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
