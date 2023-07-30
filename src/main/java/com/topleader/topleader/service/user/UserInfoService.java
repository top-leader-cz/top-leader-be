/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.service.user;

import com.topleader.topleader.entity.history.DataHistory;
import com.topleader.topleader.entity.history.data.StrengthStoredData;
import com.topleader.topleader.entity.user.UserInfo;
import com.topleader.topleader.repository.history.DataHistoryRepository;
import com.topleader.topleader.repository.user.UserInfoRepository;
import java.util.Set;
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

    public UserInfo setStrengths(String username, Set<String> strengths) {
        return Function.<String>identity()
            .andThen(user -> dataHistoryRepository.save(new DataHistory()
                .setUsername(user)
                .setType(DataHistory.Type.STRENGTHS)
                .setData(new StrengthStoredData().setStrengths(strengths))
            ))
            .andThen(DataHistory::getUsername)
            .andThen(this::find)
            .andThen(info -> info.setStrengths(strengths))
            .andThen(userInfoRepository::save)
            .apply(username);
    }

    private static UserInfo createEmpty(String username) {
        return new UserInfo()
            .setUsername(username)
            .setStrengths(Set.of())
            .setValues(Set.of())
            .setAreaOfDevelopment(Set.of());
    }


}
