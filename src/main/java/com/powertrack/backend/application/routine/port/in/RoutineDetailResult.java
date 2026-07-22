package com.powertrack.backend.application.routine.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Resultado compartido por {@link CreateRoutineUseCase} y {@link GetRoutineDetailUseCase}:
 * el detalle completo de una rutina (días y ejercicios con sus targets), enriquecido con
 * el nombre de cada ejercicio del catálogo.
 */
public record RoutineDetailResult(UUID id, UUID userId, String name, String description, Instant createdAt,
                                   List<RoutineDayResult> days) {

    public record RoutineDayResult(UUID id, String dayName, int orderIndex, List<RoutineExerciseResult> exercises) {
    }

    public record RoutineExerciseResult(UUID id, UUID exerciseId, String exerciseName, int orderIndex,
                                         int targetSets, int targetRepMin, int targetRepMax,
                                         Integer restSeconds, String notes) {
    }
}
