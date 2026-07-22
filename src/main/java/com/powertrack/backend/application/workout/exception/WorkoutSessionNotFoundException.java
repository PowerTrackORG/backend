package com.powertrack.backend.application.workout.exception;

import java.util.UUID;

/**
 * Se lanza tanto cuando la sesión no existe como cuando existe pero pertenece a otro
 * usuario. Decisión de diseño: se devuelve 404 en ambos casos (no 403), siguiendo el
 * mismo criterio de no-filtrado ya usado en {@code RoutineNotFoundException}.
 */
public class WorkoutSessionNotFoundException extends RuntimeException {

    public WorkoutSessionNotFoundException(UUID sessionId) {
        super("Sesión de entrenamiento no encontrada: " + sessionId);
    }
}
