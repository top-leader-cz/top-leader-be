package com.topleader.topleader.coach.favorite;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class FavoriteCoachService {

    private final FavoriteCoachRepository favoriteCoachRepository;

    @Transactional
    public void saveFavoriteCoaches(String username, List<String> coachUsername) {
        coachUsername.forEach(coach -> favoriteCoachRepository.save(new FavoriteCoach().setUsername(username).setCoachUsername(coach)));
    }

    public List<String> getFavoriteCoaches(String username) {
        return favoriteCoachRepository.findByUsername(username).stream()
                .map(FavoriteCoach::getCoachUsername)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeCoach(String username, String coachUsername) {
        favoriteCoachRepository.deleteByUsernameAndCoachUsername(username, coachUsername);
    }
}
