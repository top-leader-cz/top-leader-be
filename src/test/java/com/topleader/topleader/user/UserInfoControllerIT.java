package com.topleader.topleader.user;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.StrengthStoredData;
import com.topleader.topleader.history.data.ValuesStoredData;
import java.util.List;

import com.topleader.topleader.user.userinfo.UserInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = {"/sql/user_info/user-info-test.sql"})
class UserInfoControllerIT extends IntegrationTest {

    @Autowired
    private DataHistoryRepository dataHistoryRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void getEmptyDetailTest() throws Exception {

        mvc.perform(get("/api/latest/user-info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.timeZone", is("UTC")))
            .andExpect(jsonPath("$.userRoles", hasSize(1)))
            .andExpect(jsonPath("$.userRoles", hasItems("USER")))
            .andExpect(jsonPath("$.strengths", hasSize(0)))
            .andExpect(jsonPath("$.values", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.notes", nullValue()))
        ;
    }

    @Test
    @WithMockUser(username = "user2", authorities = "USER")
    void getNotEmptyDetailTest() throws Exception {

        mvc.perform(get("/api/latest/user-info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user2")))
            .andExpect(jsonPath("$.timeZone", is("UTC")))
            .andExpect(jsonPath("$.strengths", hasSize(2)))
            .andExpect(jsonPath("$.strengths", hasItems("s1", "s2")))
            .andExpect(jsonPath("$.values", hasSize(2)))
            .andExpect(jsonPath("$.values", hasItems("v1", "v2")))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(2)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasItems("a1", "a2")))
            .andExpect(jsonPath("$.notes", is("cool note")))
        ;
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void setTimezoneTest() throws Exception {

        mvc.perform(post("/api/latest/user-info/timezone")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "timezone": "CST"
                     }
                                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.timeZone", is("CST")))
            .andExpect(jsonPath("$.strengths", hasSize(0)))
            .andExpect(jsonPath("$.values", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.notes", nullValue()))
        ;
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void setStrengthsTest() throws Exception {

        mvc.perform(post("/api/latest/user-info/strengths")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "data": ["v1", "v2"]
                     }
                                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.strengths", hasSize(2)))
            .andExpect(jsonPath("$.strengths", hasItems("v1", "v2")))
            .andExpect(jsonPath("$.values", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.notes", nullValue()))
        ;

        final var userInfoData = userInfoRepository.findById("user").orElseThrow();

        assertIterableEquals(List.of("v1", "v2"), userInfoData.getStrengths());

        final var data = dataHistoryRepository.findAllByUsernameAndType("user", DataHistory.Type.STRENGTHS);

        assertEquals(1, data.size());
        final var storedHistoryData = data.get(0);

        assertEquals(DataHistory.Type.STRENGTHS, storedHistoryData.getType());
        assertEquals("user", storedHistoryData.getUsername());
        assertEquals(StrengthStoredData.class, storedHistoryData.getData().getClass());
        assertIterableEquals(List.of("v1", "v2"), ((StrengthStoredData)storedHistoryData.getData()).getStrengths());
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void setValuesTest() throws Exception {

        mvc.perform(post("/api/latest/user-info/values")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "data": ["v1", "v2"]
                     }
                                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.values", hasSize(2)))
            .andExpect(jsonPath("$.values", hasItems("v1", "v2")))
            .andExpect(jsonPath("$.strengths", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.notes", nullValue()))
        ;

        final var userInfoData = userInfoRepository.findById("user").orElseThrow();

        assertIterableEquals(List.of("v1", "v2"), userInfoData.getValues());

        final var data = dataHistoryRepository.findAllByUsernameAndType("user", DataHistory.Type.VALUES);

        assertEquals(1, data.size());
        final var storedHistoryData = data.get(0);

        assertEquals(DataHistory.Type.VALUES, storedHistoryData.getType());
        assertEquals("user", storedHistoryData.getUsername());
        assertEquals(ValuesStoredData.class, storedHistoryData.getData().getClass());
        assertIterableEquals(List.of("v1", "v2"), ((ValuesStoredData)storedHistoryData.getData()).getValues());
    }
}