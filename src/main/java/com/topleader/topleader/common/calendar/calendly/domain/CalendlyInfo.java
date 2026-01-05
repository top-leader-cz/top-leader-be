package com.topleader.topleader.common.calendar.calendly.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Data
@Accessors(chain = true)
public class CalendlyInfo {

    @Id
    private String username;

    private String refreshToken;

    private String ownerUrl;
}
