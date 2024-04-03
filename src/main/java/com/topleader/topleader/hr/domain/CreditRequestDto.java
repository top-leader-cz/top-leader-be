package com.topleader.topleader.hr.domain;

import jakarta.validation.constraints.NotNull;

public record CreditRequestDto(@NotNull Integer credit) {
}