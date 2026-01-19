package com.topleader.topleader.user.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;
import com.topleader.topleader.common.entity.BaseEntity;


@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper=false)
@Table("token")
public class Token extends BaseEntity {
    private String username;

    private String token;

    private Type type;

    public enum Type {
        SET_PASSWORD
    }
}
