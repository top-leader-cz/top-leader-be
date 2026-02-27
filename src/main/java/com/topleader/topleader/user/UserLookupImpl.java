package com.topleader.topleader.user;

import com.topleader.topleader.common.email.UserLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserLookupImpl implements UserLookup {

    private final UserRepository userRepository;

    @Override
    public Optional<EmailUser> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(u -> new EmailUser(u.getEmail(), u.getFirstName(), u.getLastName(), u.getLocale()));
    }
}
