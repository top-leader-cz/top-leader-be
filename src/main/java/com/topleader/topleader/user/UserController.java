package com.topleader.topleader.user;


import com.topleader.topleader.exception.ApiValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.exception.ErrorCodeConstants.EMAIL_USED;
import static com.topleader.topleader.exception.ErrorCodeConstants.NOT_PART_OF_COMPANY;
import static com.topleader.topleader.util.user.UserDetailUtils.isHr;


@Slf4j
@RestController
@RequestMapping("/api/latest/user")
@AllArgsConstructor
public class UserController {

    private final UserDetailService userDetailService;

    private final InvitationService invitationService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Secured({"ADMIN", "HR"})
    @PostMapping
    public UserDto addUser(@AuthenticationPrincipal UserDetails loggedUser, @RequestBody @Valid AddUserRequest request) {

        var user = new User();
        if (isHr(loggedUser)) {
            final var hrUser = userRepository.findById(loggedUser.getUsername()).orElseThrow();

            final var companyId = Optional.of(hrUser).map(User::getCompanyId).orElseThrow(() ->
                new ApiValidationException(NOT_PART_OF_COMPANY, "user", hrUser.getUsername(), "User is not part of any company")
            );

            user
                .setUsername(request.username())
                .setPassword(passwordEncoder.encode(UUID.randomUUID().toString()))
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setCompanyId(companyId)
                .setAuthorities(Set.of(User.Authority.USER))
                .setCredit(0)
                .setTimeZone(request.timeZone())
                .setRequestedBy(user.getUsername())
                .setStatus(User.Status.PENDING);
        } else {
            user.setUsername(request.username())
                .setAuthorities(request.authorities())
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setTimeZone(request.timeZone())
                .setRequestedBy(loggedUser.getUsername())
                .setStatus(request.status());

        }

        if (userRepository.findById(request.username).isPresent()) {
            throw new ApiValidationException(EMAIL_USED, "username", request.username(), "Already used");
        }

        var saved = userDetailService.save(user);
        if (sendInvite(User.Status.PENDING, request.status())) {
            invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(user, request.locale()));
        }
        return UserDto.fromUser(saved);
    }

    @Secured({"ADMIN", "HR"})
    @PutMapping("/{username}")
    public UserDto updateUser(
        @AuthenticationPrincipal UserDetails loggedUser,
        @PathVariable String username,
        @RequestBody @Valid UpdateUserRequest request
    ) {

        if (isHr(loggedUser)) {
            final var hr = userRepository.findById(loggedUser.getUsername()).orElseThrow();

            final var user = userRepository.findById(username)
                .filter(u -> Objects.equals(hr.getCompanyId(), u.getCompanyId()))
                .orElseThrow(() -> new ApiValidationException(NOT_PART_OF_COMPANY, "username", username, "User is not part of any company"));

            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            user.setTimeZone(request.timeZone());
            return UserDto.fromUser(userDetailService.save(user));
        }

        final var user = userDetailService.getUser(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
        final var oldStatus = user.getStatus();
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
