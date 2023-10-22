package com.topleader.topleader.util.user;

import com.topleader.topleader.util.common.user.UserUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserUtilTest {
    @Test
    void fromEmail() {
        Assertions.assertThat(UserUtils.fromEmail("jakub.last@text.com"))
                .extracting("username", "firstName", "lastName")
                .containsExactly("jakub.last@text.com", "jakub", "last");

        Assertions.assertThat(UserUtils.fromEmail("jakublast@text.com"))
                .extracting("username", "firstName", "lastName")
                .containsExactly("jakublast@text.com", "jakublast", "jakublast");
    }
}
