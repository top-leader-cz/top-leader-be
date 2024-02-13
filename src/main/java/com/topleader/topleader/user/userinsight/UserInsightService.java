package com.topleader.topleader.user.userinsight;


import com.topleader.topleader.ai.AiClient;
import com.topleader.topleader.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserInsightService {

    private final UserInsightRepository userInsightRepository;

    private final UserRepository userRepository;

    private final AiClient aiClient;

    public void setLeadershipStyleAnalysis(String username, List<String> strengths, List<String> values) {
        var user = userRepository.findById(username).orElseThrow();
        userInsightRepository.findById(username).ifPresent(userInsight -> {
            userInsight.setLeadershipStyleAnalysis(aiClient.findLeaderShipStyle(user.getLocale(), strengths, values));
            userInsight.setAnimalSpiritGuide(aiClient.findLeaderShipStyle(user.getLocale(), strengths, values));
            userInsightRepository.save(userInsight);
        });
    }

    public Optional<UserInsight> getInsight(String username) {
        return userInsightRepository.findById(username);
    }

}
