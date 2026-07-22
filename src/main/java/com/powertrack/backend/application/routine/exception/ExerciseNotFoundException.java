package com.powertrack.backend.application.routine.exception;

import java.util.UUID;

public class ExerciseNotFoundException extends RuntimeException {

    public ExerciseNotFoundException(UUID exerciseId) {
        super("Ejercicio no encontrado: " + exerciseId);
    }
}
