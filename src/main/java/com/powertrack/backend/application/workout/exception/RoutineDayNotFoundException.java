package com.powertrack.backend.application.workout.exception;

import java.util.UUID;

/**
 * Se lanza al iniciar una sesión sobre un {@code routineDayId} que no existe o que
 * pertenece a una rutina de otro usuario (ownership vía
 * {@code RoutineRepositoryPort#existsRoutineDayOwnedByUser}). 404 en ambos casos, mismo
 * criterio de no-filtrado que el resto del módulo.
 */
public class RoutineDayNotFoundException extends RuntimeException {

    public RoutineDayNotFoundException(UUID routineDayId) {
        super("Día de rutina no encontrado: " + routineDayId);
    }
}
