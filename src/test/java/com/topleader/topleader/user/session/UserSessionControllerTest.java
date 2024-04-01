package com.topleader.topleader.user.session;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class UserSessionControllerTest {

    @Test
    void name() {
        Assertions.assertThat(new UserSessionController(null, null, null, null)
                        .split("1. seek feedback 2. at last 2 months"))
                .containsExactlyInAnyOrder("seek feedback", "at last 2 months");

    }
}
