package com.topleader.topleader.util.common;

import com.topleader.topleader.feedback.api.FeedbackData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void dataTest() {
        var v = LocalDateTime.now().isBefore(LocalDateTime.parse("2024-01-06T00:00").plusDays(1));
        Assertions.assertThat(JsonUtils.toJson(new FeedbackData().setValidTo(LocalDateTime.now()))).isNotEmpty();
    }
}