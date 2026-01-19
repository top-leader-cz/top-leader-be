package com.topleader.topleader.user.token;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Accessors(chain = true)
@Data
@Table("token")
public class Token {

    @Id
    private Long id;

    private String username;

    private String token;

    private Type type;

    public enum Type {
        SET_PASSWORD
    }
}
