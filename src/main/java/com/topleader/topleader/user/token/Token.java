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

    private String type;

    public Type getTypeEnum() {
        return type != null ? Type.valueOf(type) : null;
    }

    public Token setTypeEnum(Type type) {
        this.type = type != null ? type.name() : null;
        return this;
    }

    public Token setType(Type type) {
        return setTypeEnum(type);
    }

    public enum Type {
        SET_PASSWORD
    }
}
