package com.topleader.topleader.util.common;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.collection.List;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class CommonUtils {

    public static final DateTimeFormatter TOP_LEADER_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:sa");

    public String generateToken() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
    }


}
