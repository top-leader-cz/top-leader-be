package com.topleader.topleader.program;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Table("program_option")
public class ProgramOption {

    @Id
    private String key;

    private String category;

    private boolean alwaysOn;
}
