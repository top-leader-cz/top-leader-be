package com.topleader.topleader.hr;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Data
@Table("hr_view")
@Accessors(chain = true)
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

    private List<String> strengths = new ArrayList<>();

    private List<String> areaOfDevelopment = new ArrayList<>();

    public List<String> getTopStrengths() {
        var strengthSize = strengths.size();
        return strengths.subList(0, Math.min(strengthSize, 5));
    }
}
