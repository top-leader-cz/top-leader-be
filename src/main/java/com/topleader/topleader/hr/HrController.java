/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr;

import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/latest/hr-users")
@AllArgsConstructor
public class HrController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    @Secured("HR")
    @GetMapping
    public List<HrUserDto> listUsers(@AuthenticationPrincipal UserDetails user) {

        return userRepository.findById(user.getUsername()).map(User::getCompanyId).map(userRepository::findAllByCompanyId).map(HrUserDto::from).orElseGet(() -> {
            log.info("User {} is not part of any company. Returning an empty list.", user.getUsername()); return List.of();
        });
    }

    @Secured("HR")
    @Transactional
    @PostMapping
    public HrUserDto inviteUser(
        @AuthenticationPrincipal UserDetails user, @RequestBody UserInvitationRequestDto request
    ) {

        final var hrUser = userRepository.findById(user.getUsername()).orElseThrow();

        final var companyId = Optional.of(hrUser).map(User::getCompanyId).orElseThrow(() -> new ApiValidationException("companyId", "User is not part of any company"));


        if (userRepository.findById(request.email()).isPresent()) {
            throw new ApiValidationException("email", "Already used");
        }

        return HrUserDto.from(userRepository.save(
            new User()
                .setUsername(request.email())
                .setPassword(passwordEncoder.encode(UUID.randomUUID().toString()))
                .setEnabled(false)
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setCompanyId(companyId)
                .setAuthorities(Set.of(User.Authority.USER))
                .setCredit(0)
                .setTimeZone(hrUser.getTimeZone())
                .setState(User.State.INVITED)));
    }

    @Secured("HR")
    @Transactional
    @PostMapping("/{username}/credit-request")
    public HrUserDto requestCredits(
        @AuthenticationPrincipal UserDetails user, @PathVariable String username, @RequestBody CreditRequestDto request
    ) {

        final var companyId = userRepository.findById(user.getUsername()).map(User::getCompanyId).orElseThrow(NotFoundException::new);

        return HrUserDto.from(
            userRepository.findById(username)
                .filter(u -> companyId.equals(u.getCompanyId()))
                .map(u -> u.setRequestedCredit(request.credit()))
                .map(userRepository::save)
                .orElseThrow(NotFoundException::new)
        );
    }

    public record UserInvitationRequestDto(@NotEmpty String email, @NotEmpty String firstName, @NotEmpty String lastName, Boolean isTrial

    ) {
    }

    public record CreditRequestDto(@NotNull Integer credit) {
    }

    public record HrUserDto(String username, String coach, Integer credit, Integer requestedCredit, User.State state) {

        public static List<HrUserDto> from(List<User> users) {
            return users.stream().map(HrUserDto::from).toList();
        }

        public static HrUserDto from(User user) {
            return new HrUserDto(user.getUsername(), user.getCoach(), user.getCredit(), user.getRequestedCredit(), user.getState());
        }

    }
}
