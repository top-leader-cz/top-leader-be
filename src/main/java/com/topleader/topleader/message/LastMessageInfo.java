package com.topleader.topleader.message;

import java.time.LocalDateTime;


/**
 * @author Daniel Slavik
 */
public interface LastMessageInfo {
    LocalDateTime getLastMessageTime();
    String getLastMessageData();
}

