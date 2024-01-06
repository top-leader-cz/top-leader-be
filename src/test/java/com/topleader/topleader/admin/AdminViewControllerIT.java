/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/admin/admin-view-data.sql")
class AdminViewControllerIT extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testConfirmTopUp() throws Exception {

        mvc.perform(post("/api/latest/admin/users/user1/confirm-requested-credits"))
            .andExpect(status().isOk());

        final var fetchedUser = userRepository.findById("user1").orElseThrow();
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getCredit()).isEqualTo(150);
        assertThat(fetchedUser.getPaidCredit()).isEqualTo(100);
        assertThat(fetchedUser.getSumRequestedCredit()).isEqualTo(1050);
        assertThat(fetchedUser.getRequestedCredit()).isZero();

    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testCreateUser() throws Exception {
        final var createUserRequestDto = new AdminViewController.CreateUserRequestDto(
            "newuser", "John", "Doe", "UTC", 1L,
            true, Set.of(User.Authority.USER, User.Authority.ADMIN), User.Status.AUTHORIZED, "en"
        );

        mvc.perform(post("/api/latest/admin/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(createUserRequestDto)))
            .andExpect(status().isOk());

        final var fetchedUser = userRepository.findById("newuser").orElseThrow();
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getUsername()).isEqualTo(createUserRequestDto.username());
        assertThat(fetchedUser.getFirstName()).isEqualTo(createUserRequestDto.firstName());
        assertThat(fetchedUser.getLastName()).isEqualTo(createUserRequestDto.lastName());
        assertThat(fetchedUser.getTimeZone()).isEqualTo(createUserRequestDto.timeZone());
        assertThat(fetchedUser.getCompanyId()).isEqualTo(createUserRequestDto.companyId());
        assertThat(fetchedUser.getIsTrial()).isEqualTo(createUserRequestDto.isTrial());
        assertThat(fetchedUser.getAuthorities()).containsExactlyInAnyOrderElementsOf(createUserRequestDto.authorities());

    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testUpdateUser() throws Exception {
        final var updatedUser = new AdminViewController.UpdateUserRequestDto(
            "John", "UpdatedDoe", "PST", 2L,
            true, Set.of(User.Authority.USER, User.Authority.ADMIN), User.Status.AUTHORIZED, "updatedCoach", 150, "updatedCoach", "en"
        );
        mvc.perform(post("/api/latest/admin/users/user4")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updatedUser)))
            .andExpect(status().isOk());

        final var fetchedUser = userRepository.findById("user4").orElseThrow();
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getFirstName()).isEqualTo(updatedUser.firstName());
        assertThat(fetchedUser.getLastName()).isEqualTo(updatedUser.lastName());
        assertThat(fetchedUser.getTimeZone()).isEqualTo(updatedUser.timeZone());
        assertThat(fetchedUser.getCompanyId()).isEqualTo(updatedUser.companyId());
        assertThat(fetchedUser.getIsTrial()).isEqualTo(updatedUser.isTrial());
        assertThat(fetchedUser.getAuthorities()).containsExactlyInAnyOrderElementsOf(updatedUser.authorities());
        assertThat(fetchedUser.getCoach()).isEqualTo(updatedUser.coach());
        assertThat(fetchedUser.getCredit()).isEqualTo(updatedUser.credit());
        assertThat(fetchedUser.getFreeCoach()).isEqualTo(updatedUser.freeCoach());

        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("top-leader");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("user4");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Unlock Your Potential with TopLeader!");
        Assertions.assertThat(body)
                .contains("John UpdatedDoe,")
                .contains("http://app-test-ur=\r\nl/#/api/public/set-password/")
                .contains("Unlock Your ");

        Assertions.assertThat(userRepository.findById("user4")).isNotEmpty();
    }
    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testUpdateUser_nullCoachAndCompany() throws Exception {
        final var updatedUser = new AdminViewController.UpdateUserRequestDto(
            "John", "UpdatedDoe", "PST", null,
            true, Set.of(User.Authority.USER, User.Authority.ADMIN), User.Status.AUTHORIZED, null, 150, null, "en"
        );
        mvc.perform(post("/api/latest/admin/users/user1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updatedUser)))
            .andExpect(status().isOk());

        final var fetchedUser = userRepository.findById("user1").orElseThrow();
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getFirstName()).isEqualTo(updatedUser.firstName());
        assertThat(fetchedUser.getLastName()).isEqualTo(updatedUser.lastName());
        assertThat(fetchedUser.getTimeZone()).isEqualTo(updatedUser.timeZone());
        assertThat(fetchedUser.getCompanyId()).isNull();
        assertThat(fetchedUser.getIsTrial()).isEqualTo(updatedUser.isTrial());
        assertThat(fetchedUser.getAuthorities()).containsExactlyInAnyOrderElementsOf(updatedUser.authorities());
        assertThat(fetchedUser.getCoach()).isNull();
        assertThat(fetchedUser.getCredit()).isEqualTo(updatedUser.credit());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void adminListTest() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("size", "1")
                .param("sort", "username,asc")
            )
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json("""
                {
                  "content": [
                    {
                      "username": "coach1",
                      "firstName": "Jane",
                      "lastName": "Smith",
                      "authorities": [
                        "COACH",
                        "USER"
                      ],
                      "timeZone": "GMT",
                      "status": "PENDING",
                      "companyId": 2,
                      "companyName": "Company 2",
                      "coach": "coach1",
                      "coachFirstName": "Jane",
                      "coachLastName": "Smith",
                      "credit": 150,
                      "requestedCredit": 75,
                      "sumRequestedCredit": 2000,
                      "paidCredit": 0,
                      "requestedBy": "god",
                      "hrs": "hr1, hr2",
                      "isTrial": true
                    }
                  ],
                  "pageable": {
                    "sort": {
                      "empty": false,
                      "sorted": true,
                      "unsorted": false
                    },
                    "offset": 0,
                    "pageNumber": 0,
                    "pageSize": 1,
                    "unpaged": false,
                    "paged": true
                  },
                  "totalPages": 7,
                  "totalElements": 7,
                  "last": false,
                  "size": 1,
                  "number": 0,
                  "sort": {
                    "empty": false,
                    "sorted": true,
                    "unsorted": false
                  },
                  "numberOfElements": 1,
                  "first": true,
                  "empty": false
                }
                """))
        ;
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByLastName() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("lastName", "Doe"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].lastName").value("Doe"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByTimeZone() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("timeZone", "UTC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].timeZone").value("UTC"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByStatus() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("status", "AUTHORIZED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].status").value("AUTHORIZED"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByCompanyId() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("companyId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].companyId").value(1));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByCredit() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("credit", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].credit").value(100));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByRequestedCredit() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("requestedCredit", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].requestedCredit").value(50));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByIsTrial() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("isTrial", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].isTrial").value(true));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByPaidCredit() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("paidCredit", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].paidCredit").value(100));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByRequestedBy() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("requestedBy", "somebody"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].requestedBy").value("somebody"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testFilterByHrs() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("hrs", "user3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].hrs").value("user3"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    @Sql(scripts = {"/sql/admin/admin-view-data.sql", "/sql/admin/admin-feedback.sql" })
    void deleteUser() throws Exception {

        mvc.perform(delete("/api/latest/admin/users/user1"))
                .andExpect(status().isOk());


        Assertions.assertThat(userRepository.findById("user1")).isEmpty();
    }

}
