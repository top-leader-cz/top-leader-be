/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        assertThat(fetchedUser.getRequestedCredit()).isZero();

    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testCreateUser() throws Exception {
        final var createUserRequestDto = new AdminViewController.CreateUserRequestDto(
            "newuser", "John", "Doe", "UTC", 1L,
            true, Set.of(User.Authority.USER, User.Authority.ADMIN)
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
            true, Set.of(User.Authority.USER, User.Authority.ADMIN), User.Status.AUTHORIZED, "updatedCoach", 150
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
        assertThat(fetchedUser.getCompanyId()).isEqualTo(updatedUser.companyId());
        assertThat(fetchedUser.getIsTrial()).isEqualTo(updatedUser.isTrial());
        assertThat(fetchedUser.getAuthorities()).containsExactlyInAnyOrderElementsOf(updatedUser.authorities());
        assertThat(fetchedUser.getCoach()).isEqualTo(updatedUser.coach());
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
}