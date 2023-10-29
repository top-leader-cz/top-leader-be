package com.topleader.topleader.user.token;


import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;


@Accessors(chain = true)
@Data
@Entity
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "token_id_seq")
    @SequenceGenerator(name = "token_id_seq", sequenceName = "token_id_seq", allocationSize = 1)
    private Long id;

    private String username;

    private String token;

    @Enumerated(EnumType.STRING)
    private Type type;

    public enum Type {
        SET_PASSWORD

    }
}
