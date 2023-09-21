package com.topleader.topleader.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        var user = userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return User.withUsername(user.getUsername())
                .disabled(com.topleader.topleader.user.User.Status.PENDING == user.getStatus())
                .password(user.getPassword())
                .authorities(user.getAuthorities().stream().map(Enum::name).toArray(String[]::new))
                .build();

    }


    public com.topleader.topleader.user.User getUser(String username) {
        return userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }


    public com.topleader.topleader.user.User save(com.topleader.topleader.user.User user) {
       return userRepository.save(user);
    }
}


