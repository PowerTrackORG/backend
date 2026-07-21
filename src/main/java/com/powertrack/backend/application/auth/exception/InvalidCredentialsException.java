package com.powertrack.backend.application.auth.exception;

/**
 * Mensaje deliberadamente genérico: se usa tanto para email inexistente como para
 * password incorrecta, para no filtrar a un atacante si un email está registrado o no.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email o contraseña inválidos");
    }
}
