package com.topleader.topleader.user.settings;

import com.topleader.topleader.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@Sql(scripts = {"/user_settings/user-settings.sql"})
class UserSettingsControllerIT extends IntegrationTest {
    @Test
    @WithUserDetails("jakub.svezi@dummy.com")
    void updateUserSettings() throws Exception {
        mvc.perform(put("/api/latest/user-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "firstName": "changed first name",
                                   "lastName": "changed last name",
                                   "manager": "jakub.manager@dummy.com",
                                   "position": "new position",
                                   "aspiredCompetency": "changed aspired competency",
                                   "aspiredPosition": "changed aspired position"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("changed first name")))
                .andExpect(jsonPath("$.lastName", is("changed last name")))
                .andExpect(jsonPath("$.manager", is("jakub.manager@dummy.com")))
                .andExpect(jsonPath("$.position", is("new position")))
                .andExpect(jsonPath("$.aspiredCompetency", is("changed aspired competency")))
                .andExpect(jsonPath("$.aspiredPosition", is("changed aspired position")))

        ;
    }

    @Test
    @WithUserDetails("jakub.svezi@dummy.com")
    @Sql(scripts = {"/user_settings/user-settings.sql", "/user_settings/user-manager.sql"})
    void fetchUserSettings() throws Exception {
        mvc.perform(get("/api/latest/user-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("jakub.svezi@dummy.com")))
                .andExpect(jsonPath("$.firstName", is("Jakub")))
                .andExpect(jsonPath("$.lastName", is("Svezi")))
                .andExpect(jsonPath("$.position", is("test position")))
                .andExpect(jsonPath("$.companyId", is(1)))
                .andExpect(jsonPath("$.businessStrategy", is("Dummy business strategy")))
                .andExpect(jsonPath("$.manager", is("jakub.manager@dummy.com")))
                .andExpect(jsonPath("$.aspiredCompetency", is("aspired competency")))
                .andExpect(jsonPath("$.aspiredPosition", is("aspired position")))

        ;
    }


    @Test
    @Sql(scripts = {"/sql/user/user-test.sql", "/sql/user/user-manager.sql"})
    @WithMockUser(username = "user.one@dummy.com", authorities = "USER")
    void listManagers() throws Exception {
        mvc.perform(get("/api/latest/user-settings/managers"))
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