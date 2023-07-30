package com.topleader.topleader.rest.user;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = {"/sql/user_info/user-info-test.sql"})
class UserInfoControllerIT extends IntegrationTest {


    @Test
    @WithMockUser(username = "user")
    void getDetailTest() throws Exception {



        mvc.perform(get("/api/latest/user-info")
                //.with(httpBasic("user","pass"))
            )
            .andExpect(status().isOk());
    }
}