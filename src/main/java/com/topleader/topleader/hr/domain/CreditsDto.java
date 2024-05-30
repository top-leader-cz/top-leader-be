package com.topleader.topleader.hr.domain;

import com.topleader.topleader.hr.HrView;

import java.util.List;

public record CreditsDto(
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

    public static List<CreditsDto> from(List<HrView> users) {
        return users.stream().map(CreditsDto::from).toList();
    }

    public static CreditsDto from(HrView user) {
        return new CreditsDto(
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
