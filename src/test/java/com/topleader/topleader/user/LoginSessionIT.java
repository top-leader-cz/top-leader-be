package com.topleader.topleader.user;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class LoginSessionIT extends IntegrationTest {

    @Autowired
    @SuppressWarnings("rawtypes")
    private SessionRepository sessionRepository;

    @Test
    void sessionWithSingleAuthoritySerializesAndDeserializes() {
        var session = saveSessionWithAuthorities("user1", "USER");

        var loaded = sessionRepository.findById(session.getId());

        assertSecurityContext(loaded, "user1", "USER");
    }

    @Test
    void sessionWithMultipleAuthoritiesSerializesAndDeserializes() {
        // Exercises TreeSet + UnmodifiableSortedSet + User$AuthorityComparator in JDK serialization
        var session = saveSessionWithAuthorities("coach1", "USER", "COACH");

        var loaded = sessionRepository.findById(session.getId());

        assertSecurityContext(loaded, "coach1", "USER", "COACH");
    }

    @Test
    void sessionWithThreeAuthoritiesSerializesAndDeserializes() {
        var session = saveSessionWithAuthorities("admin1", "USER", "COACH", "ADMIN");

        var loaded = sessionRepository.findById(session.getId());

        assertSecurityContext(loaded, "admin1", "USER", "COACH", "ADMIN");
    }

    @Test
    void deletedSessionIsNotFound() {
        var session = saveSessionWithAuthorities("user2", "USER");

        sessionRepository.deleteById(session.getId());

        assertThat(sessionRepository.findById(session.getId())).isNull();
    }

    @SuppressWarnings("unchecked")
    private Session saveSessionWithAuthorities(String username, String... authorities) {
        var grantedAuthorities = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .map(a -> (GrantedAuthority) a)
                .toList();

        var principal = User.withUsername(username)
                .password("pass")
                .authorities(grantedAuthorities)
                .build();

        var auth = new UsernamePasswordAuthenticationToken(principal, null, grantedAuthorities);
        var secContext = new SecurityContextImpl(auth);

        Session session = sessionRepository.createSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, secContext);
        sessionRepository.save(session);

        return session;
    }

    @SuppressWarnings("unchecked")
    private void assertSecurityContext(Session session, String expectedUsername, String... expectedAuthorities) {
        assertThat(session).isNotNull();

        var context = (SecurityContextImpl) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(context).isNotNull();
        assertThat(context.getAuthentication().getName()).isEqualTo(expectedUsername);
        assertThat(context.getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder((Object[]) expectedAuthorities);
    }
}
