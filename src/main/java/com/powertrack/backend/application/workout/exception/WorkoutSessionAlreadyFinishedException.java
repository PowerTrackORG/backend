package com.powertrack.backend.application.workout.exception;

import java.util.UUID;

/**
 * Se lanza al intentar finalizar una sesión que ya está en estado COMPLETED. Se mapea a
 * 409 Conflict (en vez de 400) porque el recurso existe y pertenece al usuario, pero su
 * estado actual no permite la operación solicitada.
 */
public class WorkoutSessionAlreadyFinishedException extends RuntimeException {

    public WorkoutSessionAlreadyFinishedException(UUID sessionId) {
        super("La sesión de entrenamiento ya fue finalizada: " + sessionId);
    }
}
