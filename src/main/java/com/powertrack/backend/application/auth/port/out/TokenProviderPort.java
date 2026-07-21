package com.powertrack.backend.application.auth.port.out;

import com.powertrack.backend.domain.user.User;

/**
 * Aísla al caso de uso de la librería JWT concreta (jjwt vive en infrastructure).
 */
public interface TokenProviderPort {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);
}
