/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.userinfo;

import com.topleader.topleader.user.User;
import com.topleader.topleader.util.converter.SetConverter;
import jakarta.persistence.*;

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
@ToString
@Entity
@Accessors(chain = true)
@NoArgsConstructor
public class UserInfo {

    @Id
    private String username;

    @Convert(converter = SetConverter.class)
    private List<String> strengths;

    @Convert(converter = SetConverter.class)
    private List<String> values;

    @Convert(converter = SetConverter.class)
    private List<String> areaOfDevelopment;

    private String notes;

    @Column(length = 1000)
    private String longTermGoal;

    @Column(length = 2000)
    private String motivation;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username")
    private User user;

    public enum Status {
        AUTHORIZED, PENDING, PAID

   }
}
