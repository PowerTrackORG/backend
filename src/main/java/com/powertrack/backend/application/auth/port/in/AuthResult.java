package com.powertrack.backend.application.auth.port.in;

import java.util.UUID;

public record AuthResult(UUID userId, String email, String accessToken, String refreshToken) {
}
