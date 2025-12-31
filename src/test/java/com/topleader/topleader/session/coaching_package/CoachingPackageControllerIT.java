/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/sql/coaching_package/coaching-package-test.sql")
class CoachingPackageControllerIT extends IntegrationTest {

    @Autowired
    private CoachingPackageRepository coachingPackageRepository;

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void listPackages_asHr_returnsCompanyPackages() throws Exception {
        mvc.perform(get("/api/latest/companies/1/coaching-packages"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].companyId").value(1))
                .andExpect(jsonPath("$[0].poolType").value("CORE"))
                .andExpect(jsonPath("$[0].totalUnits").value(100))
                .andExpect(jsonPath("$[0].metrics.totalUnits").value(100))
                .andExpect(jsonPath("$[0].metrics.allocatedUnits").value(0))
                .andExpect(jsonPath("$[0].metrics.remainingUnits").value(100))
                .andExpect(jsonPath("$[1].poolType").value("MASTER"))
                .andExpect(jsonPath("$[1].totalUnits").value(50));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void listPackages_asHr_deniedForOtherCompany() throws Exception {
        mvc.perform(get("/api/latest/companies/2/coaching-packages"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminUser", authorities = {"ADMIN"})
    void listPackages_asAdmin_canAccessAnyCompany() throws Exception {
        mvc.perform(get("/api/latest/companies/2/coaching-packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].companyId").value(2))
                .andExpect(jsonPath("$[0].totalUnits").value(200));
    }

    @Test
    @WithMockUser(username = "adminUser", authorities = {"ADMIN"})
    void createPackage_asAdmin_success() throws Exception {
        mvc.perform(post("/api/latest/companies/1/coaching-packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "poolType": "CORE",
                                    "totalUnits": 75,
                                    "validFrom": "2025-01-01T00:00:00",
                                    "validTo": "2025-12-31T23:59:59",
                                    "contextRef": "test-context"
                                }
                                """))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.companyId").value(1))
                .andExpect(jsonPath("$.poolType").value("CORE"))
                .andExpect(jsonPath("$.totalUnits").value(75))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.contextRef").value("test-context"))
                .andExpect(jsonPath("$.createdBy").value("adminUser"))
                .andExpect(jsonPath("$.metrics.totalUnits").value(75))
                .andExpect(jsonPath("$.metrics.remainingUnits").value(75));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void getPackage_asHr_success() throws Exception {
        mvc.perform(get("/api/latest/coaching-packages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.companyId").value(1))
                .andExpect(jsonPath("$.poolType").value("CORE"));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void getPackage_asHr_deniedForOtherCompany() throws Exception {
        mvc.perform(get("/api/latest/coaching-packages/3"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminUser", authorities = {"ADMIN"})
    void updatePackage_deactivate_success() throws Exception {
        mvc.perform(patch("/api/latest/coaching-packages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "INACTIVE",
                                    "totalUnits": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.updatedBy").value("adminUser"));

        var updated = coachingPackageRepository.findById(1L).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CoachingPackage.PackageStatus.INACTIVE);
    }

    @Test
    @WithMockUser(username = "adminUser", authorities = {"ADMIN"})
    void updatePackage_asAdmin_canUpdateAnyCompany() throws Exception {
        mvc.perform(patch("/api/latest/coaching-packages/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "INACTIVE",
                                    "totalUnits": 200
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(username = "regularUser", authorities = {"USER"})
    void listPackages_asRegularUser_denied() throws Exception {
        mvc.perform(get("/api/latest/companies/1/coaching-packages"))
                .andExpect(status().isForbidden());
    }
}
