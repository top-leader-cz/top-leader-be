package com.topleader.topleader.user.badge;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Month;
import java.util.Map;

@Data
@Accessors(chain = true)
public class BadgeResponse {

    boolean completedSession;

    boolean completedShortTermGoal;

    boolean watchedVideo;

    Map<Month, Boolean> badges;

}
