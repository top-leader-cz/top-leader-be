package com.topleader.topleader.user.userinsight;


import com.topleader.topleader.ai.AiClient;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.userinfo.UserInfo;
import com.topleader.topleader.user.userinfo.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserInsightService {

    private final UserInsightRepository userInsightRepository;

    private final UserInfoRepository userInfoRepository;

    private final UserRepository userRepository;

    private final AiClient aiClient;


    public void setUserInsight(UserInfo userInfo) {
        var strengths = userInfo.getTopStrengths();
        var values = userInfo.getValues();
        var username = userInfo.getUsername();

        var user = userRepository.findById(username).orElseThrow();
        var userInsight = userInsightRepository.findById(username).orElse(new UserInsight());
        userInsight.setUsername(username);
        userInsight.setLeadershipStyleAnalysis(aiClient.findLeaderShipStyle(user.getLocale(), strengths, values));
        userInsight.setAnimalSpiritGuide(aiClient.findAnimalSpirit(user.getLocale(), strengths, values));
        userInsightRepository.save(userInsight);
    }

    public Optional<UserInsight> getInsight(String username) {
        return userInsightRepository.findById(username);
    }

    public UserInsight generateTips(String username) {
        var user = userRepository.findById(username).orElseThrow();
        var userInfo = userInfoRepository.findById(username).orElseThrow();
        var strengths = userInfo.getTopStrengths();
        var values = userInfo.getValues();

        var locale = user.getLocale();
        var userInsight = userInsightRepository.findById(username).orElse(new UserInsight());
        if(hasFilledOutStrengthsAndValues(userInfo) && shouldGenerateTips(userInsight) ) {
            userInsight.setUsername(username);
            userInsight.setLeadershipTip(aiClient.findLeadershipTip(locale, strengths, values));
            userInsight.setPersonalGrowthTip(aiClient.findPersonalGrowthTip(locale, strengths, values));
            userInsight.setTipsGeneratedAt(LocalDateTime.now());
            return userInsightRepository.save(userInsight);
        }
        return userInsight;
    }

    private boolean shouldGenerateTips(UserInsight userInsight) {
        return (StringUtils.isBlank(userInsight.getLeadershipTip()) || StringUtils.isBlank(userInsight.getPersonalGrowthTip())) ||
                userInsight.getTipsGeneratedAt().isBefore(LocalDate.now().atStartOfDay());
    }

    private boolean hasFilledOutStrengthsAndValues(UserInfo userInfo) {
        return !CollectionUtils.isEmpty(userInfo.getStrengths()) && !CollectionUtils.isEmpty(userInfo.getValues());
    }

}
