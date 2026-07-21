package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.auth.port.in.AuthResult;

import java.util.UUID;

public record AuthResponse(UUID userId, String email, String accessToken, String refreshToken) {

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.userId(), result.email(), result.accessToken(), result.refreshToken());
    }
}
