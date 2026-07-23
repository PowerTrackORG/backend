package com.powertrack.backend.application.auth.exception;

/**
 * Mensaje deliberadamente genérico, mismo criterio que {@link InvalidCredentialsException}:
 * no distingue "expiró", "firma inválida" o "es un access token, no refresh".
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token inválido o expirado");
    }
}
