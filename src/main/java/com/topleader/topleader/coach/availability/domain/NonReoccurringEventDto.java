package com.topleader.topleader.coach.availability.domain;

import java.time.LocalDateTime;

public record NonReoccurringEventDto(LocalDateTime from, LocalDateTime to) {
}
