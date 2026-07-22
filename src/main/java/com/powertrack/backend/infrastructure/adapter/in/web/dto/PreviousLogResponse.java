package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.workout.port.in.GetPreviousLogUseCase.LogSetResult;
import com.powertrack.backend.application.workout.port.in.GetPreviousLogUseCase.PreviousLogResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PreviousLogResponse(UUID workoutLogId, UUID routineExerciseId, Instant performedAt, String notes,
                                   List<LogSetResponse> sets) {

    public static PreviousLogResponse from(PreviousLogResult result) {
        List<LogSetResponse> sets = result.sets().stream().map(LogSetResponse::from).toList();
        return new PreviousLogResponse(result.workoutLogId(), result.routineExerciseId(), result.performedAt(),
                result.notes(), sets);
    }

    public record LogSetResponse(int setNumber, BigDecimal weightKg, int repsCompleted, int rpe) {
        public static LogSetResponse from(LogSetResult result) {
            return new LogSetResponse(result.setNumber(), result.weightKg(), result.repsCompleted(), result.rpe());
        }
    }
}
