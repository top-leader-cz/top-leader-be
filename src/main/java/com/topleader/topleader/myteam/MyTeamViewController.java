/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.myteam;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/latest/my-team")
@AllArgsConstructor
public class MyTeamViewController {

    private final MyTeamViewRepository repository;

    @Secured({"MANAGER"})
    @GetMapping
    public Page<MyTeamViewDto> listUsers(@AuthenticationPrincipal UserDetails user, Pageable pageable) {

        return repository.findAllByManager(user.getUsername(), pageable)
            .map(MyTeamViewDto::from);
    }

    public record MyTeamViewDto(
        String firstName,
        String lastName,
        String username,
        String coach,
        String coachFirstName,
        String coachLastName,
        Integer credit,
        Integer requestedCredit,
        Integer scheduledCredit,
        Integer paidCredit,
        String longTermGoal,
        List<String> areaOfDevelopment,
        List<String> strengths
    ) {


        public static MyTeamViewDto from(MyTeamView user) {
            return new MyTeamViewDto(
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getCoach(),
                user.getCoachFirstName(),
                user.getCoachLastName(),
                user.getCredit(),
                user.getRequestedCredit(),
                user.getScheduledCredit(),
                user.getPaidCredit(),
                user.getLongTermGoal(),
                user.getAreaOfDevelopment(),
                user.getTopStrengths()
            );
        }
    }
}
