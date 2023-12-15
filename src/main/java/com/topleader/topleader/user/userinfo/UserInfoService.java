/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.userinfo;

import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.StrengthStoredData;
import com.topleader.topleader.history.data.ValuesStoredData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * @author Daniel Slavik
 */
@Service
@AllArgsConstructor
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;

    private final DataHistoryRepository dataHistoryRepository;

    public UserInfo find(String username) {
        return userInfoRepository.findById(username).orElse(createEmpty(username));
    }

    public UserInfo setAreaOfDevelopmentAndLongTermGoal(String username, List<String> areaOfDevelopment, String longTermGoal ) {
        return Function.<String>identity()
            .andThen(this::find)
            .andThen(info -> info.setAreaOfDevelopment(areaOfDevelopment).setLongTermGoal(longTermGoal))
            .andThen(userInfoRepository::save)
            .apply(username);
    }

    public UserInfo setNotes(String username, String notes) {
        return Function.<String>identity()
            .andThen(this::find)
            .andThen(info -> info.setNotes(notes))
            .andThen(userInfoRepository::save)
            .apply(username);
    }

    public UserInfo setStrengths(String username, List<String> strengths) {
        return Function.<String>identity()
            .andThen(user -> dataHistoryRepository.save(new DataHistory()
                .setUsername(user)
                .setType(DataHistory.Type.STRENGTHS)
                .setData(new StrengthStoredData().setStrengths(strengths))
                .setCreatedAt(LocalDateTime.now())
            ))
            .andThen(DataHistory::getUsername)
            .andThen(this::find)
            .andThen(info -> info.setStrengths(strengths))
            .andThen(userInfoRepository::save)
            .apply(username);
    }

    public UserInfo setValues(String username, List<String> values) {
        return Function.<String>identity()
            .andThen(user -> dataHistoryRepository.save(new DataHistory()
                .setUsername(user)
                .setType(DataHistory.Type.VALUES)
                .setData(new ValuesStoredData().setValues(values))
                .setCreatedAt(LocalDateTime.now())
            ))
            .andThen(DataHistory::getUsername)
            .andThen(this::find)
            .andThen(info -> info.setValues(values))
            .andThen(userInfoRepository::save)
            .apply(username);
    }

    public UserInfo setSessionData(
        String username,
        List<String> areaOfDevelopment,
        String longTermGoal,
        String motivation
    ) {
        return Function.<String>identity()
            .andThen(this::find)
            .andThen(info -> info
                .setAreaOfDevelopment(areaOfDevelopment)
                .setLongTermGoal(longTermGoal)
                .setMotivation(motivation)
            )
            .andThen(userInfoRepository::save)
            .apply(username);
    }

    public UserInfo setLastRestriction(
        String username,
        String reflection
    ) {
        return Function.<String>identity()
            .andThen(this::find)
            .andThen(info -> info
                .setLastReflection(reflection)
            )
            .andThen(userInfoRepository::save)
            .apply(username);
    }



    private static UserInfo createEmpty(String username) {
        return new UserInfo()
            .setUsername(username)
            .setStrengths(List.of())
            .setValues(List.of())
            .setAreaOfDevelopment(List.of());
    }


}
