package com.topleader.topleader.user.credit;

import com.topleader.topleader.user.User;

public record RequestCreditsDto(
        String firstName,
        String lastName,
        String username,
        Integer credit,
        Integer requestedCredit,
        Integer scheduledCredit,
        Integer paidCredit
) {

    public static RequestCreditsDto from(User user) {
        return new RequestCreditsDto(
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getCredit(),
                user.getRequestedCredit(),
                user.getScheduledCredit(),
                user.getPaidCredit());
    }

}
