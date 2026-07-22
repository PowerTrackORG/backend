package com.powertrack.backend.application.workout.exception;

import java.util.UUID;

/**
 * Se lanza al finalizar una sesión referenciando un {@code routineExerciseId} que no
 * existe o que pertenece a la rutina de otro usuario (ownership vía
 * {@code RoutineRepositoryPort#findRoutineExerciseTargets}). 404 en ambos casos.
 */
public class RoutineExerciseNotFoundException extends RuntimeException {

    public RoutineExerciseNotFoundException(UUID routineExerciseId) {
        super("Ejercicio de rutina no encontrado: " + routineExerciseId);
    }
}
