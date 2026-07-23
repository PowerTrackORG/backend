package com.powertrack.backend.application.auth.port.out;

import com.powertrack.backend.domain.user.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Aísla al caso de uso de la librería JWT concreta (jjwt vive en infrastructure).
 */
public interface TokenProviderPort {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    /**
     * Devuelve el {@code userId} si {@code refreshToken} es válido, no expiró y es
     * efectivamente de tipo "refresh" (no un access token reutilizado). Vacío en
     * cualquier otro caso — sin distinguir el motivo exacto, mismo criterio
     * anti-enumeración que {@code InvalidCredentialsException}.
     */
    Optional<UUID> validateRefreshTokenAndGetUserId(String refreshToken);
}
