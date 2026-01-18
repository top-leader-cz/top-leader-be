/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.manager.ManagerRepository;
import com.topleader.topleader.user.manager.UserManagerRepository;
import com.topleader.topleader.user.manager.UsersManagers;
import com.topleader.topleader.user.token.TokenRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/hr/hr-users.sql")
class HrControllerIT extends IntegrationTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    ManagerRepository managerRepository;

    @Autowired
    UserManagerRepository userManagerRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;


    @Test
    @WithMockUser(username = "hrUser", authorities = "HR")
    void testListUsersEndpoint() throws Exception {

        mvc.perform(get("/api/latest/hr-users"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                [
                  {
                    "firstName": "John",
                    "lastName": "Doe",
                    "username": "hrUser",
                    "coach": "Coach1",
                    "coachFirstName": null,
                    "coachLastName": null,
                    "credit": 100,
                    "requestedCredit": 0,
                    "scheduledCredit": 10,
                    "paidCredit": 111,
                    "longTermGoal": null,
                    "areaOfDevelopment": [],
                    "strengths": []
                  },
                  {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "username": "user1",
                    "coach": "Coach2",
                    "coachFirstName": "Coach",
                    "coachLastName": "Borek",
                    "credit": 50,
                    "requestedCredit": 10,
                    "scheduledCredit": 20,
                    "paidCredit": 222,
                    "longTermGoal": "Goal1",
                    "areaOfDevelopment": [
                      "aod"
                    ],
                    "strengths": [
                      "s1",
                      "s2",
                      "s3",
                      "s4",
                      "s5"
                    ]
                  }
                ]
                """));
    }

    @Test
    @Sql(scripts = {"/sql/user/user-test.sql", "/sql/user/user-manager.sql"})
    @WithMockUser(username = "manager.one@dummy.com", authorities = "HR")
    void getUser() throws Exception {
        mvc.perform(get("/api/latest/hr-users/manager.one@dummy.com")).andExpect(status().isOk()).andExpect(content().contentType("application/json")).andDo(print()).andExpect(content().json("""
                                {
                                "username":"manager.one@dummy.com",
                                "firstName":"manager",
                                "lastName":"one",
                                "timeZone":"Europe/Prague",
                                "status":"AUTHORIZED",
                                "authorities":["USER","MANAGER"],
                                "position": "dummy position",
                                "manager":null,
                                "isManager":true,
                                "aspiredCompetency": "test competency"
                                }
                                """));

    }

    @Test
    @WithMockUser(username = "hrUser", authorities = "HR")
    void testRequestCredits() throws Exception {

        mvc.perform(post("/api/latest/hr-users/user1/credit-request").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "credit": 1000
                }
                """)).andExpect(status().isOk()).andExpect(content().contentType("application/json")).andExpect(content().json("{\"username\":\"user1\",\"coach\":\"Coach2\",\"credit\":50,\"requestedCredit\":1000}"));

        var receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("info@topleader.io");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Credits requested in the TopLeader platform");
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(body).contains("Hr: hrUser for username: user1 Amount: 1000");
    }

    @Test
    @WithMockUser(username = "user2", authorities = "USER")
    void testRequestCreditsNoRights() throws Exception {

        mvc.perform(post("/api/latest/hr-users/user1/credit-request").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "credit": 1000
                }
                """)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = "HR")
    void addUser() throws Exception {
        mvc.perform(post("/api/latest/hr-users").contentType(MediaType.APPLICATION_JSON).content("""
                {
                   "firstName": "Jakub",
                   "lastName": "Svezi",
                   "username":  "jakub.svezi@dummy.com",
                   "authorities": [ "USER" ],
                   "locale": "cs",
                   "timeZone": "Europe/Prague",
                   "status": "AUTHORIZED",
                   "isManager": true,
                   "position": "test position",
                   "aspiredCompetency" : "created competency"
                }
                """)).andExpect(status()
                .isOk())
                .andExpect(jsonPath("$.username", is("jakub.svezi@dummy.com")))
                .andExpect(jsonPath("$.firstName", is("Jakub")))
                .andExpect(jsonPath("$.lastName", is("Svezi")))
                .andExpect(jsonPath("$.timeZone", is("Europe/Prague")))
                .andExpect(jsonPath("$.status", is("AUTHORIZED")))
                .andExpect(jsonPath("$.authorities", hasItems("USER")))
                .andExpect(jsonPath("$.position", is("test position")))
                .andExpect(jsonPath("$.aspiredCompetency", is("created competency")));

        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("jakub.svezi@dummy.com");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Odemkněte svůj potenciál s TopLeader!");
        Assertions.assertThat(body).contains("Jakub Svezi,").contains("http://app-test-url/#/api/public/set-password/").contains("Odemkn=C4=9Bte");


        Optional<User> user = userRepository.findByUsername("jakub.svezi@dummy.com");
        Assertions.assertThat(user).isNotEmpty();
        Assertions.assertThat(managerRepository.findAll()).extracting("username").containsExactly("jakub.svezi@dummy.com");
    }

    @Test
    @Sql(scripts = "/sql/hr/hr-users.sql")
    @WithMockUser(username = "user", authorities = "USER")
    void addUser403() throws Exception {

        mvc.perform(post("/api/latest/hr-users").contentType(MediaType.APPLICATION_JSON).content("""
                {
                   "firstName": "Jakub",
                   "lastName": "Svezi",
                   "username":  "jakub.svezi@dummy.com",
                   "authorities": [ "USER" ],
                   "locale": "cs",
                   "timeZone": "Europe/Prague",
                   "status": "AUTHORIZED"
                }
                """)).andExpect(status().is(403));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = "HR")
    @Sql(scripts = {"/sql/user/user-test.sql", "/sql/hr/hr-users.sql", "/sql/user/user-manager.sql"})
    void updateUser() throws Exception {
        mvc.perform(put("/api/latest/hr-users/hrUser").contentType(MediaType.APPLICATION_JSON).content("""
                   {
                   "firstName": "Jakub1",
                   "lastName": "Svezi2",
                   "authorities": [ "USER" ],
                   "timeZone": "Europe/Paris",
                   "status": "PAID",
                   "locale": "cs",
                   "manager": "manager.one@dummy.com",
                   "position": "test position",
                   "aspiredCompetency" : "updated competency"
                }
                """)).andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("hrUser")))
                .andExpect(jsonPath("$.firstName", is("Jakub1")))
                .andExpect(jsonPath("$.lastName", is("Svezi2")))
                .andExpect(jsonPath("$.timeZone", is("Europe/Paris")))
                .andExpect(jsonPath("$.status", is("PAID")))
                .andExpect(jsonPath("$.position", is("test position")))
                .andExpect(jsonPath("$.aspiredCompetency", is("updated competency")));

        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("hrUser");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Odemkněte svůj potenciál s TopLeader!");
        Assertions.assertThat(body).contains("Jakub1 Svezi2,").contains("http://app-test-url/#/api/public/set-password/");

        Assertions.assertThat(userRepository.findByUsername("hrUser")).isNotEmpty();

        Assertions.assertThat(userManagerRepository.findByUserUsername("hrUser"))
                .extracting(UsersManagers::getManagerUsername)
                .containsExactly("manager.one@dummy.com");

    }

    @Test
    @Sql(scripts = {"/sql/user/user-test.sql", "/sql/user/user-manager.sql"})
    @WithMockUser(username = "user.one@dummy.com", authorities = "HR")
    void listManagers() throws Exception {
        mvc.perform(get("/api/latest/hr-users/managers"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(content().json("""
                        [
                        {"username":"manager.one@dummy.com",
                        "firstName":"manager",
                        "lastName":"one"
                        },
                        {
                        "username":"manager.two@dummy.com",
                        "firstName":"manager",
                        "lastName":"two"
                        }
                        ]                       
                                """));
    }

}
