package com.topleader.topleader.common.domain;

import jakarta.validation.constraints.NotNull;

public record CreditRequestDto(@NotNull Integer credit) {
}
