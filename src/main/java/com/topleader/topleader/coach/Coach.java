package com.topleader.topleader.coach;

import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.common.util.common.JsonbValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Table("coach")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @PersistenceCreator)
public class Coach {

    @Id
    private Long id;

    private String username;

    private boolean publicProfile;

    private String webLink;

    private String bio;

    private JsonbValue languages;

    private JsonbValue fields;

    private LocalDate experienceSince;

    private String rate;

    private Integer rateOrder;

    private Integer internalRate;

    private String linkedinProfile;

    private boolean freeSlots;

    private int priority;

    private JsonbValue primaryRoles;

    private JsonbValue certificate;

    private JsonbValue baseLocations;

    private String travelWillingness;

    private JsonbValue deliveryFormat;

    private JsonbValue serviceType;

    private JsonbValue topics;

    private JsonbValue diagnosticTools;

    private JsonbValue industryExperience;

    private String userReferences;


    public enum PrimaryRole {
        COACH, MENTOR, TRAINER, FACILITATOR, CONSULTANT, SPEAKER
    }


    private static final TypeReference<Set<PrimaryRole>> PRIMARY_ROLES_TYPE = new TypeReference<>() {};

    public List<String> getLanguagesList() { return JsonbValue.toStringList(languages); }
    public Coach setLanguagesList(List<String> v) { this.languages = JsonbValue.fromList(v); return this; }

    public List<String> getFieldsList() { return JsonbValue.toStringList(fields); }
    public Coach setFieldsList(List<String> v) { this.fields = JsonbValue.fromList(v); return this; }

    public Set<PrimaryRole> getPrimaryRolesSet() { return JsonbValue.toSet(primaryRoles, PRIMARY_ROLES_TYPE); }
    public Coach setPrimaryRolesSet(Set<PrimaryRole> v) { this.primaryRoles = JsonbValue.fromSet(v); return this; }

    public Set<String> getCertificateSet() { return JsonbValue.toStringSet(certificate); }
    public Coach setCertificateSet(Set<String> v) { this.certificate = JsonbValue.fromSet(v); return this; }

    public Set<String> getBaseLocationsSet() { return JsonbValue.toStringSet(baseLocations); }
    public Coach setBaseLocationsSet(Set<String> v) { this.baseLocations = JsonbValue.fromSet(v); return this; }

    public Set<String> getDeliveryFormatSet() { return JsonbValue.toStringSet(deliveryFormat); }
    public Coach setDeliveryFormatSet(Set<String> v) { this.deliveryFormat = JsonbValue.fromSet(v); return this; }

    public Set<String> getServiceTypeSet() { return JsonbValue.toStringSet(serviceType); }
    public Coach setServiceTypeSet(Set<String> v) { this.serviceType = JsonbValue.fromSet(v); return this; }

    public Set<String> getTopicsSet() { return JsonbValue.toStringSet(topics); }
    public Coach setTopicsSet(Set<String> v) { this.topics = JsonbValue.fromSet(v); return this; }

    public Set<String> getDiagnosticToolsSet() { return JsonbValue.toStringSet(diagnosticTools); }
    public Coach setDiagnosticToolsSet(Set<String> v) { this.diagnosticTools = JsonbValue.fromSet(v); return this; }

    public Set<String> getIndustryExperienceSet() { return JsonbValue.toStringSet(industryExperience); }
    public Coach setIndustryExperienceSet(Set<String> v) { this.industryExperience = JsonbValue.fromSet(v); return this; }

    public String getReferences() {
        return userReferences;
    }

    public Coach setReferences(String references) {
        this.userReferences = references;
        return this;
    }

}
