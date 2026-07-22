package com.powertrack.backend.application.user.exception;

import java.util.UUID;

/**
 * Se lanza cuando el userId extraído de un JWT válido ya no corresponde a un usuario
 * existente (caso extremo: el usuario fue eliminado entre la emisión del token y su uso).
 * No debería ocurrir en la práctica, pero el puerto devuelve {@code Optional} y este caso
 * se debe manejar explícitamente en vez de lanzar un {@code NoSuchElementException} genérico.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("Usuario no encontrado: " + userId);
    }
}
