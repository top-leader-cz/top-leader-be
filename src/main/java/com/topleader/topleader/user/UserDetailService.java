package com.topleader.topleader.user;


import java.util.Locale;

import com.topleader.topleader.common.exception.ApiValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.UNABLE_TO_DELETE;


@Slf4j
@Service
@AllArgsConstructor
public class UserDetailService implements UserDetailsService {

    private static final String FIELD_USERNAME = "username";

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        var sinInName = username.toLowerCase(Locale.ROOT);
        var user = userRepository.findByEmail(sinInName)
                .or(() -> userRepository.findByUsername(sinInName))
            .orElseThrow(() -> new UsernameNotFoundException(username));

        return User.withUsername(user.getUsername())
            .disabled(com.topleader.topleader.user.User.Status.PENDING == user.getStatus()
                    || com.topleader.topleader.user.User.Status.CANCELED == user.getStatus())
            .password(user.getPassword())
            .authorities(user.getAuthorities().stream().map(Enum::name).toArray(String[]::new))
            .build();

    }


    public Optional<com.topleader.topleader.user.User> getUser(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<com.topleader.topleader.user.User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean userExist(String username) {
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .isPresent();
    }


    public com.topleader.topleader.user.User save(com.topleader.topleader.user.User user) {
        log.info("Saving user: {}", user);
        return userRepository.save(user);
    }

    public com.topleader.topleader.user.User find(String username) {
        return userRepository.findByUsername(username).orElse(com.topleader.topleader.user.User.empty());
    }

    public void delete(String username) {
        log.info("Deleting user: {}", username);
        userRepository.findByUsername(username)
                .map(u -> u.setStatus(com.topleader.topleader.user.User.Status.CANCELED))
                .ifPresentOrElse(userRepository::save,
                        () -> {
                            throw new ApiValidationException(UNABLE_TO_DELETE, FIELD_USERNAME, username, "not found");
                        });

    }
}


