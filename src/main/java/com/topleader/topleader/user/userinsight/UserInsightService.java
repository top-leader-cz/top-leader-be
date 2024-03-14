package com.topleader.topleader.user.userinsight;


import com.topleader.topleader.ai.AiClient;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.userinfo.UserInfo;
import com.topleader.topleader.user.userinfo.UserInfoRepository;
import com.topleader.topleader.util.common.user.UserUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class UserInsightService {

    private final UserInsightRepository userInsightRepository;

    private final UserInfoRepository userInfoRepository;

    private final UserRepository userRepository;

    private final AiClient aiClient;


    @Async
    public void setUserInsight(UserInfo userInfo) {
        var strengths = userInfo.getTopStrengths();
        var values = userInfo.getValues();
        var username = userInfo.getUsername();

        var user = userRepository.findById(username).orElseThrow();
        var userInsight = userInsightRepository.findById(username).orElse(new UserInsight());
        userInsight.setUsername(username);
        var savedUserInsight = userInsightRepository.save(userInsight.setLeadershipPending(true).setAnimalSpiritPending(true));
        var leaderShip = CompletableFuture.supplyAsync(() -> aiClient.findLeaderShipStyle(UserUtils.localeToLanguage(user.getLocale()), strengths, values));
        var animalSpirit = CompletableFuture.supplyAsync(() -> aiClient.findAnimalSpirit(UserUtils.localeToLanguage(user.getLocale()), strengths, values));
        var leaderPersona = CompletableFuture.supplyAsync(() -> aiClient.findLeaderPersona(UserUtils.localeToLanguage(user.getLocale()), strengths, values));

        savedUserInsight.setLeadershipStyleAnalysis(leaderShip.join());
        savedUserInsight.setAnimalSpiritGuide(animalSpirit.join());
        savedUserInsight.setWorldLeaderPersona(leaderPersona.join());

        savedUserInsight.setLeadershipPending(false);
        savedUserInsight.setAnimalSpiritPending(false);
        userInsightRepository.save(savedUserInsight);
        generateTips(username);
    }

    public Optional<UserInsight> getInsight(String username) {
        return userInsightRepository.findById(username);
    }

    @Async
    public void generateTips(String username) {
        var userInfo = userInfoRepository.findById(username).orElse(new UserInfo());
        var userInsight = userInsightRepository.findById(username).orElse(new UserInsight());

        if (hasFilledOutStrengthsAndValues(userInfo) && shouldGenerateTips(userInsight)) {
            var savedUserInsight = userInsightRepository.save(userInsight.setDailyTipsPending(true));
            var user = userRepository.findById(username).orElseThrow();
            var strengths = userInfo.getTopStrengths();
            var values = userInfo.getValues();
            var locale = user.getLocale();
            savedUserInsight.setUsername(username);
            savedUserInsight.setLeadershipTip(aiClient.findLeadershipTip(UserUtils.localeToLanguage(locale), strengths, values));
            savedUserInsight.setPersonalGrowthTip(aiClient.findPersonalGrowthTip(UserUtils.localeToLanguage(locale), strengths, values));
            savedUserInsight.setTipsGeneratedAt(LocalDateTime.now());
            savedUserInsight.setDailyTipsPending(false);
            userInsightRepository.save(savedUserInsight);
        }
    }

    private boolean shouldGenerateTips(UserInsight userInsight) {
        return (StringUtils.isBlank(userInsight.getLeadershipTip()) || StringUtils.isBlank(userInsight.getPersonalGrowthTip())) ||
                userInsight.getTipsGeneratedAt().isBefore(LocalDate.now().atStartOfDay());
    }

    private boolean hasFilledOutStrengthsAndValues(UserInfo userInfo) {
        return !CollectionUtils.isEmpty(userInfo.getStrengths()) && !CollectionUtils.isEmpty(userInfo.getValues());
    }


}
