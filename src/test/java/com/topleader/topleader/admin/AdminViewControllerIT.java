/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.coach.CoachRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import java.util.Locale;
import java.util.Set;

import com.topleader.topleader.common.util.image.ArticleImageService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import tools.jackson.databind.json.JsonMapper;

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
    private JsonMapper jsonMapper;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    ArticleImageService articleImageService;

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void listUser() throws Exception {
        articleImageService.generateImage("imgate with hardworking in steal factory");
        var result = mvc.perform(get("/api/latest/admin/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expected = TestUtils.readFileAsString("admin/admin-list-response.json");

        TestUtils.assertJsonEquals(result, expected);
    }

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
            "NewUser@gmail.com", "John", "Doe", "UTC", 1L,
            true, Set.of(User.Authority.USER, User.Authority.ADMIN), User.Status.AUTHORIZED, "en", "$", 165, Set.of("ACC")
        );

        mvc.perform(post("/api/latest/admin/users")
                .contentType("application/json")
                .content(jsonMapper.writeValueAsString(createUserRequestDto)))
            .andExpect(status().isOk());

        final var fetchedUser = userRepository.findById("newuser@gmail.com").orElseThrow();
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getUsername()).isEqualTo(createUserRequestDto.username().toLowerCase(Locale.ROOT));
        assertThat(fetchedUser.getFirstName()).isEqualTo(createUserRequestDto.firstName());
        assertThat(fetchedUser.getLastName()).isEqualTo(createUserRequestDto.lastName());
        assertThat(fetchedUser.getTimeZone()).isEqualTo(createUserRequestDto.timeZone());
        assertThat(fetchedUser.getCompanyId()).isEqualTo(createUserRequestDto.companyId());
        assertThat(fetchedUser.getAuthorities()).containsExactlyInAnyOrderElementsOf(createUserRequestDto.authorities());

        var coach = coachRepository.findById("newuser@gmail.com").orElseThrow();
        assertThat(coach.getRate()).isEqualTo("$");
        assertThat(coach.getInternalRate()).isEqualTo(165);
        assertThat(coach.getCertificate()).isEqualTo(Set.of("ACC"));

    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testUpdateUser() throws Exception {
        final var updatedUser = new AdminViewController.UpdateUserRequestDto(
            "John", "UpdatedDoe", "PST", 2L,
            true, Set.of(User.Authority.USER, User.Authority.ADMIN), User.Status.AUTHORIZED, "updatedCoach", 150, "updatedCoach", "en",
            Set.of("$$"), null, null, Set.of("ACC")
        );
        mvc.perform(post("/api/latest/admin/users/user4")
                .contentType("application/json")
                .content(jsonMapper.writeValueAsString(updatedUser)))
            .andExpect(status().isOk());

        final var fetchedUser = userRepository.findById("user4").orElseThrow();
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getFirstName()).isEqualTo(updatedUser.firstName());
        assertThat(fetchedUser.getLastName()).isEqualTo(updatedUser.lastName());
        assertThat(fetchedUser.getTimeZone()).isEqualTo(updatedUser.timeZone());
        assertThat(fetchedUser.getCompanyId()).isEqualTo(updatedUser.companyId());
        assertThat(fetchedUser.getAuthorities()).containsExactlyInAnyOrderElementsOf(updatedUser.authorities());
        assertThat(fetchedUser.getCoach()).isEqualTo(updatedUser.coach());
        assertThat(fetchedUser.getCredit()).isEqualTo(updatedUser.credit());
        assertThat(fetchedUser.getFreeCoach()).isEqualTo(updatedUser.freeCoach());
        assertThat(fetchedUser.getAllowedCoachRates()).isEqualTo(updatedUser.allowedCoachRates());


        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
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
    void testResentInvitationToUser() throws Exception {

        mvc.perform(post("/api/latest/admin/users/user4/resent-invitation")
                .contentType("application/json")
                .content(jsonMapper.writeValueAsString(new AdminViewController.ResentInvitationRequestDto("en"))))
            .andExpect(status().isOk());

        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("user4");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Unlock Your Potential with TopLeader!");
        Assertions.assertThat(body)
                .contains("Bob Brown,")
                .contains("http://app-test-ur=\r\nl/#/api/public/set-password/")
                .contains("Unlock Your ");

        Assertions.assertThat(userRepository.findById("user4")).isNotEmpty();
    }
    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void testUpdateUser_nullCoachAndCompany() throws Exception {
        final var updatedUser = new AdminViewController.UpdateUserRequestDto(
            "John", "UpdatedDoe", "PST", null,
            true, Set.of(User.Authority.USER, User.Authority.ADMIN), User.Status.AUTHORIZED, null, 150, null, "en",
            null, null, null, Set.of("ACC")
        );
        mvc.perform(post("/api/latest/admin/users/user1")
                .contentType("application/json")
                .content(jsonMapper.writeValueAsString(updatedUser)))
            .andExpect(status().isOk());

        final var fetchedUser = userRepository.findById("user1").orElseThrow();
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getFirstName()).isEqualTo(updatedUser.firstName());
        assertThat(fetchedUser.getLastName()).isEqualTo(updatedUser.lastName());
        assertThat(fetchedUser.getTimeZone()).isEqualTo(updatedUser.timeZone());
        assertThat(fetchedUser.getCompanyId()).isNull();
        assertThat(fetchedUser.getAuthorities()).containsExactlyInAnyOrderElementsOf(updatedUser.authorities());
        assertThat(fetchedUser.getCoach()).isNull();
        assertThat(fetchedUser.getCredit()).isEqualTo(updatedUser.credit());
        assertThat(fetchedUser.getAllowedCoachRates()).isEmpty();

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
                      "locale": "en",
                      "allowedCoachRates": "$, $$"
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
    void adminListInternalRateTest() throws Exception {
        mvc.perform(get("/api/latest/admin/users")
                .param("username", "user1")
                .param("size", "1")
                .param("sort", "username,asc")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "content": [
                    {
                      "username": "user1",
                      "firstName": "John",
                      "lastName": "Doe",
                      "authorities": [
                        "USER"
                      ],
                      "timeZone": "UTC",
                      "status": "AUTHORIZED",
                      "companyId": 1,
                      "companyName": "Company 1",
                      "coach": "coach1",
                      "coachFirstName": "Jane",
                      "coachLastName": "Smith",
                      "credit": 100,
                      "requestedCredit": 50,
                      "sumRequestedCredit": 1000,
                      "paidCredit": 100,
                      "scheduledCredit": 0,
                      "hrs": "user3",
                      "requestedBy": "god",
                      "freeCoach": null,
                      "locale": "en",
                      "allowedCoachRates": null,
                      "rate": "$$$",
                      "internalRate": 275
                    }
                  ],
                  "pageable": {
                    "pageNumber": 0,
                    "pageSize": 1,
                    "sort": {
                      "empty": false,
                      "unsorted": false,
                      "sorted": true
                    },
                    "offset": 0,
                    "unpaged": false,
                    "paged": true
                  },
                  "last": true,
                  "totalPages": 1,
                  "totalElements": 1,
                  "first": true,
                  "size": 1,
                  "number": 0,
                  "sort": {
                    "empty": false,
                    "unsorted": false,
                    "sorted": true
                  },
                  "numberOfElements": 1,
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

        Assertions.assertThat(userRepository.findById("user1").orElseThrow().getStatus()).isEqualTo(User.Status.CANCELED);
    }
}
