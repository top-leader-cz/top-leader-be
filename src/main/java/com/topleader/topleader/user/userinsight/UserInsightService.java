package com.topleader.topleader.user.userinsight;


import com.topleader.topleader.common.ai.AiClient;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.userinfo.UserInfo;
import com.topleader.topleader.user.userinfo.UserInfoRepository;
import com.topleader.topleader.common.util.common.user.UserUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
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
        var userInsight = userInsightRepository.findById(username).orElse(new UserInsight().setUsername(username));
        userInsight.setLeadershipPending(true).setAnimalSpiritPending(true);
        var savedUserInsight = userInsightRepository.save(userInsight);
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
        log.info("User insight set successfully: {}", username);
    }

    public UserInsight getInsight(String username) {
        return userInsightRepository.findById(username).orElse(new UserInsight().setUsername(username));
    }

    public UserInsight save(UserInsight userInsight) {
        return userInsightRepository.saveAndFlush(userInsight);
    }

    @Async
    @Transactional
    public void generateTipsAsync(String username) {
        generateTips(username);
    }

    public void generateTips(String username) {
        log.info("Generating tips for user: {}", username);
        var userInfo = userInfoRepository.findById(username).orElse(new UserInfo().setUsername(username));
        var userInsight = userInsightRepository.findById(username).orElse(new UserInsight().setUsername(username));

        if (hasFilledOutStrengthsAndValues(userInfo) && shouldGenerateTips(userInsight)) {
            userInsight.setDailyTipsPending(true);
            var savedUserInsight = userInsightRepository.save(userInsight);
            var user = userRepository.findById(username).orElseThrow();
            var strengths = userInfo.getTopStrengths();
            var values = userInfo.getValues();
            var locale = user.getLocale();
            savedUserInsight.setUsername(username);
//            savedUserInsight.setLeadershipTip(aiClient.findLeadershipTip(UserUtils.localeToLanguage(locale), strengths, values));
            savedUserInsight.setTipsGeneratedAt(LocalDateTime.now());
            savedUserInsight.setDailyTipsPending(false);
            userInsightRepository.save(savedUserInsight);
        }
        log.info("tips generated successfully: {}", username);
    }

    private boolean shouldGenerateTips(UserInsight userInsight) {
        return (StringUtils.isBlank(userInsight.getLeadershipTip()) || StringUtils.isBlank(userInsight.getPersonalGrowthTip())) ||
                userInsight.getTipsGeneratedAt().isBefore(LocalDate.now().atStartOfDay());
    }

    private boolean hasFilledOutStrengthsAndValues(UserInfo userInfo) {
        return !CollectionUtils.isEmpty(userInfo.getStrengths()) && !CollectionUtils.isEmpty(userInfo.getValues());
    }


}
