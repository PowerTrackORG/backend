package com.powertrack.backend.application.routine.exception;

import java.util.UUID;

/**
 * Se lanza tanto cuando la rutina no existe como cuando existe pero pertenece a otro
 * usuario. Decisión de diseño: se devuelve 404 en ambos casos (en vez de 403 para el
 * segundo) para no filtrarle a un usuario la existencia de rutinas ajenas, siguiendo el
 * mismo criterio de no-filtrado ya usado en {@code InvalidCredentialsException} del
 * módulo Auth.
 */
public class RoutineNotFoundException extends RuntimeException {

    public RoutineNotFoundException(UUID routineId) {
        super("Rutina no encontrada: " + routineId);
    }
}
