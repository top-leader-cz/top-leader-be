package com.topleader.topleader.user;

import com.topleader.topleader.exception.ApiValidationException;
import java.util.List;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.topleader.topleader.exception.ErrorCodeConstants.UNABLE_TO_DELETE;

@Slf4j
@Service
@AllArgsConstructor
public class UserDetailService implements UserDetailsService {

    private static final String FIELD_USERNAME = "username";

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        var user = userRepository.findById(username.toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new UsernameNotFoundException(username));

        return User.withUsername(user.getUsername())
            .disabled(com.topleader.topleader.user.User.Status.PENDING == user.getStatus()
                    || com.topleader.topleader.user.User.Status.CANCELED == user.getStatus())
            .password(user.getPassword())
            .authorities(user.getAuthorities().stream().map(Enum::name).toArray(String[]::new))
            .build();

    }


    public Optional<com.topleader.topleader.user.User> getUser(String username) {
        return userRepository.findById(username);
    }

    public com.topleader.topleader.user.User save(com.topleader.topleader.user.User user) {
        log.info("Saving user: {}", user);
        return userRepository.save(user);
    }

    public com.topleader.topleader.user.User find(String username) {
        return userRepository.findById(username).orElse(com.topleader.topleader.user.User.empty());
    }

    public void delete(String username) {
        log.info("Deleting user: {}", username);
        userRepository.findById(username)
                .map(u -> u.setStatus(com.topleader.topleader.user.User.Status.CANCELED))
                .ifPresentOrElse(userRepository::save,
                        () -> {
                            throw new ApiValidationException(UNABLE_TO_DELETE, FIELD_USERNAME, username, "not found");
                        });

    }
}


