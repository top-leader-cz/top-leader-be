package com.topleader.topleader.hr;

import com.topleader.topleader.user.RoleConverter;
import com.topleader.topleader.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;


@Data
@Entity
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

}
