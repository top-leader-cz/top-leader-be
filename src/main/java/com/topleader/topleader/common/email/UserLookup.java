package com.topleader.topleader.common.email;

import java.util.Optional;

public interface UserLookup {

    Optional<EmailUser> findByUsername(String username);

    record EmailUser(String email, String firstName, String lastName, String locale) {}
}
