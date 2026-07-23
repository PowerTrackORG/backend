package com.powertrack.backend.application.routine.port.in;

import java.util.List;
import java.util.UUID;

public interface CreateRoutineUseCase {

    RoutineDetailResult create(CreateRoutineCommand command);

    /**
     * {@code routineId} lo genera el cliente (no el servidor) para que crear una rutina
     * sea idempotente al reintentar una sincronización offline.
     */
    record CreateRoutineCommand(UUID routineId, UUID userId, String name, String description,
                                 List<RoutineDayCommand> days) {
    }

    record RoutineDayCommand(String dayName, int orderIndex, List<RoutineExerciseCommand> exercises) {
    }

    record RoutineExerciseCommand(UUID exerciseId, int orderIndex, int targetSets, int targetRepMin,
                                   int targetRepMax, Integer restSeconds, String notes) {
    }
}
