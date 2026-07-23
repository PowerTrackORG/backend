package com.powertrack.backend.application.auth.port.in;

public interface RefreshAccessTokenUseCase {

    AuthResult refresh(String refreshToken);
}
