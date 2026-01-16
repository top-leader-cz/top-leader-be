/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Entity
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "user_allocation", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_allocation_package_user", columnNames = {"package_id", "user_id"})
})
public class UserAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_allocation_id_seq")
    @SequenceGenerator(name = "user_allocation_id_seq", sequenceName = "user_allocation_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "allocated_units", nullable = false)
    private Integer allocatedUnits = 0;

    @Column(name = "consumed_units", nullable = false)
    private Integer consumedUnits = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationStatus status = AllocationStatus.ACTIVE;

    @Column(name = "context_ref", length = 4000)
    private String contextRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AllocationStatus {
        ACTIVE,
        INACTIVE
    }
}
