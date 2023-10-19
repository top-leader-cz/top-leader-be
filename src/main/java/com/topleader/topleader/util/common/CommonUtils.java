package com.topleader.topleader.util.common;


import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class CommonUtils {

    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}
