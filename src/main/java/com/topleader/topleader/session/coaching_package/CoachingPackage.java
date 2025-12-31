/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Entity
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "coaching_package")
public class CoachingPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "coaching_package_id_seq")
    @SequenceGenerator(name = "coaching_package_id_seq", sequenceName = "coaching_package_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pool_type", nullable = false)
    private PoolType poolType;

    @Column(name = "total_units", nullable = false)
    private Integer totalUnits;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageStatus status = PackageStatus.ACTIVE;

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

    public enum PoolType {
        CORE,
        MASTER
    }

    public enum PackageStatus {
        ACTIVE,
        INACTIVE
    }
}
