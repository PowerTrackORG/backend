package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.routine.port.in.RoutineDetailResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoutineDetailResponse(UUID id, UUID userId, String name, String description, Instant createdAt,
                                     List<RoutineDayResponse> days) {

    public static RoutineDetailResponse from(RoutineDetailResult result) {
        List<RoutineDayResponse> days = result.days().stream().map(RoutineDayResponse::from).toList();
        return new RoutineDetailResponse(result.id(), result.userId(), result.name(), result.description(),
                result.createdAt(), days);
    }

    public record RoutineDayResponse(UUID id, String dayName, int orderIndex, List<RoutineExerciseResponse> exercises) {
        public static RoutineDayResponse from(RoutineDetailResult.RoutineDayResult day) {
            List<RoutineExerciseResponse> exercises = day.exercises().stream().map(RoutineExerciseResponse::from).toList();
            return new RoutineDayResponse(day.id(), day.dayName(), day.orderIndex(), exercises);
        }
    }

    public record RoutineExerciseResponse(UUID id, UUID exerciseId, String exerciseName, int orderIndex,
                                           int targetSets, int targetRepMin, int targetRepMax,
                                           Integer restSeconds, String notes) {
        public static RoutineExerciseResponse from(RoutineDetailResult.RoutineExerciseResult exercise) {
            return new RoutineExerciseResponse(exercise.id(), exercise.exerciseId(), exercise.exerciseName(),
                    exercise.orderIndex(), exercise.targetSets(), exercise.targetRepMin(), exercise.targetRepMax(),
                    exercise.restSeconds(), exercise.notes());
        }
    }
}
