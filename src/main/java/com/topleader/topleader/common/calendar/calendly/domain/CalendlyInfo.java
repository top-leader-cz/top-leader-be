package com.topleader.topleader.common.calendar.calendly.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Accessors(chain = true)
@Table("calendly_info")
public class CalendlyInfo {

    @Id
    private String username;

    private String refreshToken;

    private String ownerUrl;
}
