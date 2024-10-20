package com.topleader.topleader.coach.favorite;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/latest/coach-favorite")
@Secured({"COACH", "USER", "ADMIN", "HR"})
public class FavoriteCoachController {

    private final FavoriteCoachService favoriteCoachService;

    @PostMapping
    public void saveFavoriteCoach(@AuthenticationPrincipal UserDetails loggedUser, @RequestBody List<String> coaches) {
        favoriteCoachService.saveFavoriteCoaches(loggedUser.getUsername(), coaches);
    }

    @GetMapping
    public List<String> fetchFavoriteCoaches(@AuthenticationPrincipal UserDetails loggedUser) {
        return favoriteCoachService.getFavoriteCoaches(loggedUser.getUsername());
    }

}
