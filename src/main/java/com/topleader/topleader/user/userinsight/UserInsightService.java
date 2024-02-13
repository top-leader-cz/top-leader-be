package com.topleader.topleader.user.userinsight;


import com.topleader.topleader.ai.AiClient;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.userinfo.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserInsightService {

    private final UserInsightRepository userInsightRepository;

    private final UserRepository userRepository;

    private final AiClient aiClient;


    public void setUserInsight(UserInfo userInfo) {
        var strengthSize = userInfo.getStrengths().size();
        var strengths = userInfo.getStrengths().subList(0, Math.max(5, strengthSize));
        var values = userInfo.getValues();
        var username = userInfo.getUsername();

        var user = userRepository.findById(username).orElseThrow();
        var userInsight = userInsightRepository.findById(username).orElse(new UserInsight());
        userInsight.setLeadershipStyleAnalysis(aiClient.findLeaderShipStyle(user.getLocale(), strengths, values));
        userInsight.setAnimalSpiritGuide(aiClient.findAnimalSpirit(user.getLocale(), strengths, values));
        userInsightRepository.save(userInsight);
    }

    public Optional<UserInsight> getInsight(String username) {
        return userInsightRepository.findById(username);
    }

}
