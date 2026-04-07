package com.topleader.topleader.program.manager;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/manager/manager-program-test.sql")
class ManagerProgramControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(username = "mgr1", authorities = "MANAGER")
    void listManagedPrograms_returnsOnlyAssignedParticipantsGroupedByProgram() throws Exception {
        var result = mvc.perform(get("/api/latest/manager/programs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                [
                    {
                        "programId": 1,
                        "programName": "Alpha Program",
                        "participants": [
                            {
                                "username": "user1",
                                "firstName": "Anna",
                                "lastName": "Adams",
                                "status": "ACTIVE",
                                "attendanceCount": 3,
                                "practiceCompletionRate": 0.75
                            }
                        ]
                    },
                    {
                        "programId": 2,
                        "programName": "Bravo Program",
                        "participants": [
                            {
                                "username": "user3",
                                "firstName": "Cara",
                                "lastName": "Clark",
                                "status": "INVITED",
                                "attendanceCount": 0,
                                "practiceCompletionRate": 0.0
                            }
                        ]
                    }
                ]
                """);
    }

    @Test
    @WithMockUser(username = "mgr2", authorities = "MANAGER")
    void listManagedPrograms_anotherManagerSeesOwnParticipants() throws Exception {
        var result = mvc.perform(get("/api/latest/manager/programs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                [
                    {
                        "programId": 1,
                        "programName": "Alpha Program",
                        "participants": [
                            {
                                "username": "user2",
                                "status": "AT_RISK",
                                "attendanceCount": 1,
                                "practiceCompletionRate": 0.0
                            }
                        ]
                    }
                ]
                """);
    }

    @Test
    @WithMockUser(username = "mgr_other", authorities = "MANAGER")
    void listManagedPrograms_managerInOtherCompanySeesNothing() throws Exception {
        var result = mvc.perform(get("/api/latest/manager/programs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, "[]");
    }

    @Test
    @WithMockUser(username = "user1", authorities = "USER")
    void listManagedPrograms_nonManagerForbidden() throws Exception {
        mvc.perform(get("/api/latest/manager/programs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void listManagedPrograms_hrRoleForbidden() throws Exception {
        mvc.perform(get("/api/latest/manager/programs"))
                .andExpect(status().isForbidden());
    }
}
