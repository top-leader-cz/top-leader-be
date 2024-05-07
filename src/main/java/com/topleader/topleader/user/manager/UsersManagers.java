package com.topleader.topleader.user.manager;

import com.topleader.topleader.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Entity
@Accessors(chain = true)
public class UsersManagers {

    @EmbeddedId
    private UserManagerId id;

    @ManyToOne
    @MapsId("userUsername")
    private User user;

    @ManyToOne
    @MapsId("managerUsername")
    private User manager;

    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserManagerId implements Serializable {

        private String userUsername;

        private String managerUsername;
    }
}
