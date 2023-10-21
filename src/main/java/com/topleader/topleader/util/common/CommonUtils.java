package com.topleader.topleader.util.common;


import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@UtilityClass
public class CommonUtils {

    public static final DateTimeFormatter TOP_LEADER_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:sa");

    public String generateToken() {
        return UUID.randomUUID().toString();
    }


}
