/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.common.util.common.JsonUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Table("coach")
@Accessors(chain = true)
@NoArgsConstructor
public class Coach {

    @Id
    private String username;

    private boolean publicProfile;

    private String webLink;

    private String bio;

    @MappedCollection(idColumn = "coach_username")
    private List<CoachLanguage> coachLanguages = new ArrayList<>();

    @MappedCollection(idColumn = "coach_username")
    private List<CoachField> coachFields = new ArrayList<>();

    private LocalDate experienceSince;

    private String rate;

    private Integer rateOrder;

    private Integer internalRate;

    private String linkedinProfile;

    private boolean freeSlots;

    private int priority;

    private String primaryRoles;

    private String certificate;

    private String baseLocations;

    private String travelWillingness;

    private String deliveryFormat;

    private String serviceType;

    private String topics;

    private String diagnosticTools;

    private String industryExperience;

    private String userReferences;

    // Helper methods for languages
    public List<String> getLanguages() {
        return coachLanguages.stream()
                .map(CoachLanguage::getCoachLanguages)
                .collect(Collectors.toList());
    }

    public Coach setLanguages(List<String> languages) {
        this.coachLanguages = languages != null
                ? languages.stream().map(CoachLanguage::new).collect(Collectors.toList())
                : new ArrayList<>();
        return this;
    }

    // Helper methods for fields
    public List<String> getFields() {
        return coachFields.stream()
                .map(CoachField::getCoachFields)
                .collect(Collectors.toList());
    }

    public Coach setFields(List<String> fields) {
        this.coachFields = fields != null
                ? fields.stream().map(CoachField::new).collect(Collectors.toList())
                : new ArrayList<>();
        return this;
    }

    // JSON field helpers
    public Set<PrimaryRole> getPrimaryRolesSet() {
        return primaryRoles != null
                ? JsonUtils.fromJsonString(primaryRoles, new TypeReference<Set<PrimaryRole>>() {})
                : new HashSet<>();
    }

    public Coach setPrimaryRolesSet(Set<PrimaryRole> roles) {
        this.primaryRoles = roles != null ? JsonUtils.toJsonString(roles) : null;
        return this;
    }

    public Set<String> getCertificateSet() {
        return certificate != null
                ? JsonUtils.fromJsonString(certificate, new TypeReference<Set<String>>() {})
                : new HashSet<>();
    }

    public Coach setCertificateSet(Set<String> certs) {
        this.certificate = certs != null ? JsonUtils.toJsonString(certs) : null;
        return this;
    }

    public Set<String> getBaseLocationsSet() {
        return baseLocations != null
                ? JsonUtils.fromJsonString(baseLocations, new TypeReference<Set<String>>() {})
                : new HashSet<>();
    }

    public Coach setBaseLocationsSet(Set<String> locations) {
        this.baseLocations = locations != null ? JsonUtils.toJsonString(locations) : null;
        return this;
    }

    public Set<String> getDeliveryFormatSet() {
        return deliveryFormat != null
                ? JsonUtils.fromJsonString(deliveryFormat, new TypeReference<Set<String>>() {})
                : new HashSet<>();
    }

    public Coach setDeliveryFormatSet(Set<String> formats) {
        this.deliveryFormat = formats != null ? JsonUtils.toJsonString(formats) : null;
        return this;
    }

    public Set<String> getServiceTypeSet() {
        return serviceType != null
                ? JsonUtils.fromJsonString(serviceType, new TypeReference<Set<String>>() {})
                : new HashSet<>();
    }

    public Coach setServiceTypeSet(Set<String> types) {
        this.serviceType = types != null ? JsonUtils.toJsonString(types) : null;
        return this;
    }

    public Set<String> getTopicsSet() {
        return topics != null
                ? JsonUtils.fromJsonString(topics, new TypeReference<Set<String>>() {})
                : new HashSet<>();
    }

    public Coach setTopicsSet(Set<String> topicSet) {
        this.topics = topicSet != null ? JsonUtils.toJsonString(topicSet) : null;
        return this;
    }

    public Set<String> getDiagnosticToolsSet() {
        return diagnosticTools != null
                ? JsonUtils.fromJsonString(diagnosticTools, new TypeReference<Set<String>>() {})
                : new HashSet<>();
    }

    public Coach setDiagnosticToolsSet(Set<String> tools) {
        this.diagnosticTools = tools != null ? JsonUtils.toJsonString(tools) : null;
        return this;
    }

    public Set<String> getIndustryExperienceSet() {
        return industryExperience != null
                ? JsonUtils.fromJsonString(industryExperience, new TypeReference<Set<String>>() {})
                : new HashSet<>();
    }

    public Coach setIndustryExperienceSet(Set<String> industries) {
        this.industryExperience = industries != null ? JsonUtils.toJsonString(industries) : null;
        return this;
    }

    public String getReferences() {
        return userReferences;
    }

    public Coach setReferences(String references) {
        this.userReferences = references;
        return this;
    }

    public enum CertificateType {
        ACC, PCC, MCC, MBTI, GALLUP_STRENGTHSFINDER, DISC, HOGAN, LPI, FEEDBACK_360, SHL, INSIGHTS_DISCOVERY, OTHER
    }

    public enum PrimaryRole {
        COACH, MENTOR, TRAINER, FACILITATOR, CONSULTANT, SPEAKER
    }

    public enum TravelWillingness {
        NO, WITHIN_CITY, WITHIN_COUNTRY, WITHIN_REGION, WITHIN_EUROPE, GLOBALLY, OTHER
    }

    public enum DeliveryFormat {
        ONLINE, ONSITE, HYBRID, OTHER
    }

    public enum ServiceType {
        ONE_TO_ONE, EXECUTIVE, TEAM, CAREER, GROUP, PEER_MENTORING, WORKSHOPS, LONGER_PROGRAMS, OTHER
    }

    public enum Topic {
        AI_ADOPTION_FOR_LEADERS, AI_GOVERNANCE_AND_ETHICS, BUSINESS, CAREER, CHANGE, COACHING_SKILLS_FOR_MANAGERS,
        COMMUNICATION, CONFIDENCE, CONFLICT, CROSS_CULTURAL_LEADERSHIP, CULTURAL_DIFFERENCES, DECISION_MAKING,
        DELEGATION, DIVERSITY, EMOTIONAL_INTELLIGENCE, ENTREPRENEURSHIP, EXECUTIVE, FACILITATION, FEEDBACK_CULTURE,
        FITNESS, HEALTH, IMPOSTER_SYNDROME, INFLUENCING_SKILLS, INNOVATION_AND_CREATIVITY, LEADERSHIP,
        LEADING_WITHOUT_AUTHORITY, LIFE, MANAGEMENT, MENTAL_FITNESS, MENTORSHIP, NEGOTIATIONS,
        ORGANIZATIONAL_DEVELOPMENT, PERFORMANCE, PSYCHOLOGICAL_SAFETY, RELATIONSHIPS, REMOTE_LEADERSHIP,
        RESILIENCE, SALES, SELF_CRITICISM, SELF_LEADERSHIP, STRATEGIC_THINKING, STRESS_MANAGEMENT, TEAMS,
        TIME_MANAGEMENT, TRANSFORMATIONS, WELLBEING, WOMEN, OTHER
    }

    public enum DiagnosticTool {
        MBTI, GALLUP_STRENGTHSFINDER, DISC, HOGAN, LPI, FEEDBACK_360, SHL, INSIGHTS_DISCOVERY, OTHER
    }

    public enum IndustryExperience {
        TECH, FINANCE, PHARMA, RETAIL, MANUFACTURING, LOGISTICS, TELECOMMUNICATIONS, PUBLIC_SECTOR,
        GENERAL_CROSS_INDUSTRY, OTHER
    }
}
