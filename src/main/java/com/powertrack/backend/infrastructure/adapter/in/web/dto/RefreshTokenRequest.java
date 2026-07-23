package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(@NotBlank String refreshToken) {
}
