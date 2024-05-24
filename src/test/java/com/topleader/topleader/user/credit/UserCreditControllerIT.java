package com.topleader.topleader.user.credit;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = "/sql/hr/hr-users.sql")
class UserCreditControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(username = "user1", authorities = "USER")
    void getCredits() throws Exception {
        mvc.perform(get("/api/latest/user-credits"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"username\":\"user1\",\"credit\":50,\"requestedCredit\":10,\"scheduledCredit\":20,\"paidCredit\":222}"));
    }


    @Test
    @WithMockUser(username = "user1", authorities = "USER")
    void requestCredits() throws Exception {

        mvc.perform(post("/api/latest/user-credits").contentType(MediaType.APPLICATION_JSON).content("""

                                {
                            "credit": 1000
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json("{\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"username\":\"user1\",\"credit\":50,\"requestedCredit\":1000,\"scheduledCredit\":20,\"paidCredit\":222}"));

        var receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("top-leader");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("info@topleader.io");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Credits requested in the TopLeader platform");
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(body).contains("User: user1 Amount: 1000");
    }
}
