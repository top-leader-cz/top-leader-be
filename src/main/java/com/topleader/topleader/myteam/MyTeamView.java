/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.myteam;

import com.topleader.topleader.util.converter.SetConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;
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
@Entity
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class MyTeamView {

    @Id
    private String id;

    private String username;

    private String manager;

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

    @Convert(converter = SetConverter.class)
    private List<String> strengths = new ArrayList<>();

    @Convert(converter = SetConverter.class)
    private List<String> areaOfDevelopment = new ArrayList<>();

    public List<String> getTopStrengths() {
        var strengthSize = strengths.size();
        return strengths.subList(0, Math.min(strengthSize, 5));
    }
}
