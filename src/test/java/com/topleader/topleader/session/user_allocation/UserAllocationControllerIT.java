/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation;

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

@Sql(scripts = "/sql/user_allocation/user-allocation-test.sql")
class UserAllocationControllerIT extends IntegrationTest {

    @Autowired
    private UserAllocationRepository userAllocationRepository;

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void listAllocations_asHr_success() throws Exception {
        mvc.perform(get("/api/latest/coaching-packages/1/allocations"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value("user1"))
                .andExpect(jsonPath("$[0].allocatedUnits").value(10))
                .andExpect(jsonPath("$[1].userId").value("user2"))
                .andExpect(jsonPath("$[1].allocatedUnits").value(20));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void listAllocations_asHr_deniedForOtherCompany() throws Exception {
        mvc.perform(get("/api/latest/coaching-packages/3/allocations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void createAllocation_success() throws Exception {
        mvc.perform(post("/api/latest/coaching-packages/1/allocations/user3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "allocatedUnits": 15,
                                    "status": "ACTIVE",
                                    "contextRef": "test-context"
                                }
                                """))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.userId").value("user3"))
                .andExpect(jsonPath("$.allocatedUnits").value(15))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.contextRef").value("test-context"));

        var allocation = userAllocationRepository.findByPackageIdAndUserId(1L, "user3").orElseThrow();
        assertThat(allocation.getAllocatedUnits()).isEqualTo(15);
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void createAllocation_alreadyExists_fails() throws Exception {
        // user1 already has an allocation
        mvc.perform(post("/api/latest/coaching-packages/1/allocations/user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "allocatedUnits": 15
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$[0].errorCode").value("ALLOCATION_ALREADY_EXISTS"));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void updateAllocation_success() throws Exception {
        mvc.perform(put("/api/latest/coaching-packages/1/allocations/user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "allocatedUnits": 25
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.allocatedUnits").value(25));

        var allocation = userAllocationRepository.findByPackageIdAndUserId(1L, "user1").orElseThrow();
        assertThat(allocation.getAllocatedUnits()).isEqualTo(25);
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void createAllocation_exceedsCapacity_fails() throws Exception {
        // Package has 100 total, 30 already allocated (10+20), trying to add 80 more
        mvc.perform(post("/api/latest/coaching-packages/1/allocations/user3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "allocatedUnits": 80
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void createAllocation_inactivePackage_fails() throws Exception {
        // Package 2 is INACTIVE
        mvc.perform(post("/api/latest/coaching-packages/2/allocations/user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "allocatedUnits": 5
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void bulkAllocate_success() throws Exception {
        mvc.perform(post("/api/latest/coaching-packages/1/allocations:bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "items": [
                                        { "userId": "user1", "allocatedUnits": 5 },
                                        { "userId": "user3", "allocatedUnits": 10 }
                                    ]
                                }
                                """))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.updated").value(2))
                .andExpect(jsonPath("$.items.length()").value(2));

        var allocation1 = userAllocationRepository.findByPackageIdAndUserId(1L, "user1").orElseThrow();
        assertThat(allocation1.getAllocatedUnits()).isEqualTo(5);

        var allocation3 = userAllocationRepository.findByPackageIdAndUserId(1L, "user3").orElseThrow();
        assertThat(allocation3.getAllocatedUnits()).isEqualTo(10);
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void bulkAllocate_exceedsCapacity_fails() throws Exception {
        // Package has 100 total, 30 already allocated, trying to bulk allocate 80 more
        mvc.perform(post("/api/latest/coaching-packages/1/allocations:bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "items": [
                                        { "userId": "user3", "allocatedUnits": 80 }
                                    ]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void updateAllocationStatus_deactivate_success() throws Exception {
        mvc.perform(patch("/api/latest/allocations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        var allocation = userAllocationRepository.findById(1L).orElseThrow();
        assertThat(allocation.getStatus()).isEqualTo(UserAllocation.AllocationStatus.INACTIVE);
    }

    @Test
    @WithMockUser(username = "adminUser", authorities = {"ADMIN"})
    void adminCanAccessAnyCompanyPackage() throws Exception {
        mvc.perform(get("/api/latest/coaching-packages/3/allocations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "regularUser", authorities = {"USER"})
    void regularUser_denied() throws Exception {
        mvc.perform(get("/api/latest/coaching-packages/1/allocations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void updateAllocation_belowConsumed_fails() throws Exception {
        // user2 has allocatedUnits=20 and consumedUnits=5
        // Trying to set allocatedUnits to 3 (below consumed) should fail
        mvc.perform(put("/api/latest/coaching-packages/1/allocations/user2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "allocatedUnits": 3
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$[0].errorCode").value("ALLOCATED_BELOW_CONSUMED"));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void updateAllocation_exactlyAtConsumed_success() throws Exception {
        // user2 has consumedUnits=5, setting allocatedUnits=5 should succeed
        mvc.perform(put("/api/latest/coaching-packages/1/allocations/user2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "allocatedUnits": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allocatedUnits").value(5))
                .andExpect(jsonPath("$.consumedUnits").value(5));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void consumeUnit_success() throws Exception {
        // user1 has allocatedUnits=10 and consumedUnits=0
        mvc.perform(post("/api/latest/coaching-packages/1/allocations/user1:consume"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.consumedUnits").value(1))
                .andExpect(jsonPath("$.allocatedUnits").value(10));

        var allocation = userAllocationRepository.findByPackageIdAndUserId(1L, "user1").orElseThrow();
        assertThat(allocation.getConsumedUnits()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void consumeUnit_exceedsAllocated_fails() throws Exception {
        // First, set user1 allocation to exactly consumedUnits
        userAllocationRepository.findByPackageIdAndUserId(1L, "user1").ifPresent(a -> {
            a.setAllocatedUnits(1);
            a.setConsumedUnits(1);
            userAllocationRepository.save(a);
        });

        // Now try to consume another unit - should fail
        mvc.perform(post("/api/latest/coaching-packages/1/allocations/user1:consume"))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$[0].errorCode").value("NO_UNITS_AVAILABLE"));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void consumeUnit_inactiveAllocation_fails() throws Exception {
        // Deactivate user1's allocation first
        userAllocationRepository.findByPackageIdAndUserId(1L, "user1").ifPresent(a -> {
            a.setStatus(UserAllocation.AllocationStatus.INACTIVE);
            userAllocationRepository.save(a);
        });

        mvc.perform(post("/api/latest/coaching-packages/1/allocations/user1:consume"))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$[0].errorCode").value("PACKAGE_INACTIVE"));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void bulkAllocate_belowConsumed_fails() throws Exception {
        // user2 has consumedUnits=5, trying to set allocatedUnits=2 should fail
        mvc.perform(post("/api/latest/coaching-packages/1/allocations:bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "items": [
                                        { "userId": "user2", "allocatedUnits": 2 }
                                    ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$[0].errorCode").value("ALLOCATED_BELOW_CONSUMED"));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void createAllocation_inactiveAllocationConsumedUnitsCountTowardsCapacity() throws Exception {
        // Package has 100 total, 30 allocated (10+20)
        // Deactivate user1's allocation (10 allocated, 0 consumed) - this frees up 10 units
        userAllocationRepository.findByPackageIdAndUserId(1L, "user1").ifPresent(a -> {
            a.setStatus(UserAllocation.AllocationStatus.INACTIVE);
            userAllocationRepository.save(a);
        });

        // Now available should be 80 (100 - 20 active - 0 inactive consumed)
        // Try to allocate 75 - should succeed
        mvc.perform(post("/api/latest/coaching-packages/1/allocations/user3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "allocatedUnits": 75
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allocatedUnits").value(75));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void createAllocation_inactiveAllocationWithConsumedUnitsBlocksCapacity() throws Exception {
        // Package has 100 total, 30 allocated (10+20), user2 has 5 consumed
        // Deactivate user2's allocation - consumed units (5) still count!
        userAllocationRepository.findByPackageIdAndUserId(1L, "user2").ifPresent(a -> {
            a.setStatus(UserAllocation.AllocationStatus.INACTIVE);
            userAllocationRepository.save(a);
        });

        // Now used capacity: 10 (user1 active) + 5 (user2 inactive consumed) = 15
        // Available: 100 - 15 = 85
        // Try to allocate 90 - should fail
        mvc.perform(post("/api/latest/coaching-packages/1/allocations/user3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "allocatedUnits": 90
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$[0].errorCode").value("CAPACITY_EXCEEDED"));

        // But 85 should succeed
        mvc.perform(post("/api/latest/coaching-packages/1/allocations/user3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "allocatedUnits": 85
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allocatedUnits").value(85));
    }
}
