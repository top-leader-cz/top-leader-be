/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Entity
@Accessors(chain = true)
@NoArgsConstructor
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private boolean publicProfile;

    @Column(length = 1000)
    private String webLink;

    @Column(length = 1000)
    private String bio;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> languages;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> fields;

    private LocalDate experienceSince;

    private String rate;

    private Integer rateOrder;

    private Integer internalRate;

    private String linkedinProfile;

    private boolean freeSlots;

    private int priority;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<PrimaryRole> primaryRoles;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username", insertable = false, updatable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<String> certificate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<String> baseLocations;

    @Column(length = 255)
    private String travelWillingness;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<String> deliveryFormat;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<String> serviceType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<String> topics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<String> diagnosticTools;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<String> industryExperience;

    @Column(name = "user_references")
    private String references;

    public enum CertificateType {
        ACC,
        PCC,
        MCC,
        MBTI,
        GALLUP_STRENGTHSFINDER,
        DISC,
        HOGAN,
        LPI,
        FEEDBACK_360,
        SHL,
        INSIGHTS_DISCOVERY,
        OTHER
    }

    public enum PrimaryRole {
        COACH,
        MENTOR,
        TRAINER,
        FACILITATOR,
        CONSULTANT,
        SPEAKER
    }

    public enum TravelWillingness {
        NO,
        WITHIN_CITY,
        WITHIN_COUNTRY,
        WITHIN_REGION,
        WITHIN_EUROPE,
        GLOBALLY,
        OTHER
    }

    public enum DeliveryFormat {
        ONLINE,
        ONSITE,
        HYBRID,
        OTHER
    }

    public enum ServiceType {
        ONE_TO_ONE,
        EXECUTIVE,
        TEAM,
        CAREER,
        GROUP,
        PEER_MENTORING,
        WORKSHOPS,
        LONGER_PROGRAMS,
        OTHER
    }

    public enum Topic {
        AI_ADOPTION_FOR_LEADERS,
        AI_GOVERNANCE_AND_ETHICS,
        BUSINESS,
        CAREER,
        CHANGE,
        COACHING_SKILLS_FOR_MANAGERS,
        COMMUNICATION,
        CONFIDENCE,
        CONFLICT,
        CROSS_CULTURAL_LEADERSHIP,
        CULTURAL_DIFFERENCES,
        DECISION_MAKING,
        DELEGATION,
        DIVERSITY,
        EMOTIONAL_INTELLIGENCE,
        ENTREPRENEURSHIP,
        EXECUTIVE,
        FACILITATION,
        FEEDBACK_CULTURE,
        FITNESS,
        HEALTH,
        IMPOSTER_SYNDROME,
        INFLUENCING_SKILLS,
        INNOVATION_AND_CREATIVITY,
        LEADERSHIP,
        LEADING_WITHOUT_AUTHORITY,
        LIFE,
        MANAGEMENT,
        MENTAL_FITNESS,
        MENTORSHIP,
        NEGOTIATIONS,
        ORGANIZATIONAL_DEVELOPMENT,
        PERFORMANCE,
        PSYCHOLOGICAL_SAFETY,
        RELATIONSHIPS,
        REMOTE_LEADERSHIP,
        RESILIENCE,
        SALES,
        SELF_CRITICISM,
        SELF_LEADERSHIP,
        STRATEGIC_THINKING,
        STRESS_MANAGEMENT,
        TEAMS,
        TIME_MANAGEMENT,
        TRANSFORMATIONS,
        WELLBEING,
        WOMEN,
        OTHER
    }

    public enum DiagnosticTool {
        MBTI,
        GALLUP_STRENGTHSFINDER,
        DISC,
        HOGAN,
        LPI,
        FEEDBACK_360,
        SHL,
        INSIGHTS_DISCOVERY,
        OTHER
    }

    public enum IndustryExperience {
        TECH,
        FINANCE,
        PHARMA,
        RETAIL,
        MANUFACTURING,
        LOGISTICS,
        TELECOMMUNICATIONS,
        PUBLIC_SECTOR,
        GENERAL_CROSS_INDUSTRY,
        OTHER
    }

    public String getUserEmail() {
        return Optional.ofNullable(user)
                .map(User::getEmail)
                .orElse(username);
    }
}
