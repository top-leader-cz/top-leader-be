package com.topleader.topleader.hr;

import com.topleader.topleader.common.util.common.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter
@ToString
@NoArgsConstructor
@Accessors(chain = true)
@Table("hr_view")
public class HrView {

    @Id
    private String username;

    private String firstName;

    private String lastName;

    private String coach;

    private String coachFirstName;

    private String coachLastName;

    private Long companyId;

    private Integer credit;

    private Integer requestedCredit;

    private Integer sumRequestedCredit;

    private Integer paidCredit;

    private Integer scheduledCredit;

    private String longTermGoal;

    private String strengths;

    private String areaOfDevelopment;

    public List<String> getStrengthsList() {
        return strengths != null ? JsonUtils.fromJsonStringToList(strengths) : new ArrayList<>();
    }

    public List<String> getAreaOfDevelopmentList() {
        return areaOfDevelopment != null ? JsonUtils.fromJsonStringToList(areaOfDevelopment) : new ArrayList<>();
    }

    public List<String> getTopStrengths() {
        var list = getStrengthsList();
        return list.subList(0, Math.min(list.size(), 5));
    }
}
