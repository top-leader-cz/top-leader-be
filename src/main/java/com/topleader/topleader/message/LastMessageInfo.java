/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import java.time.LocalDateTime;


/**
 * @author Daniel Slavik
 */
public interface LastMessageInfo {
    LocalDateTime getLastMessageTime();
    String getLastMessageData();
}

