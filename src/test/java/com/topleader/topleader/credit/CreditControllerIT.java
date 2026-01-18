/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.credit;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.credit.history.CreditHistory;
import com.topleader.topleader.credit.history.CreditHistoryRepository;
import com.topleader.topleader.user.UserRepository;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = {"/sql/credit/scheduled-session-payment-test.sql"})
class CreditControllerIT extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditHistoryRepository creditHistoryRepository;

    @Test
    @WithMockUser(authorities = "JOB")
    void processPaymentTest() throws Exception {

        mvc.perform(post("/api/protected/jobs/payments"))
            .andExpect(status().isOk())
        ;

        final var coach = userRepository.findByUsername("coach").orElseThrow();

        assertThat(coach.getCredit(), is(0));

        final var client1 = userRepository.findByUsername("client1").orElseThrow();

        assertThat(client1.getCredit(), is(80));
        final var client2 = userRepository.findByUsername("client1").orElseThrow();

        assertThat(client2.getCredit(), is(80));
        final var client3 = userRepository.findByUsername("client1").orElseThrow();

        assertThat(client3.getCredit(), is(80));

        final var creditHistory = creditHistoryRepository.findAll();

        assertThat(creditHistory, hasSize(12));

        final var received = creditHistory.stream()
            .filter(c -> c.getType().equals(CreditHistory.Type.RECEIVED))
            .toList();

        assertThat(received, hasSize(6));
        assertThat(received.stream().allMatch(e -> e.getUsername().equals("coach")), is(true));
        assertThat(received.stream().allMatch(e -> e.getCredit().equals(90)), is(true));

        final var paid = creditHistory.stream()
            .filter(c -> c.getType().equals(CreditHistory.Type.PAID))
            .toList();

        assertThat(paid, hasSize(6));

        final var clients = paid.stream().map(CreditHistory::getUsername).collect(Collectors.toSet());

        assertThat(clients, hasItems("client1", "client2", "client3"));
    }
}
