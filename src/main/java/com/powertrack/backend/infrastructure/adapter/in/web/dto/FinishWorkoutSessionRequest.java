package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.workout.port.in.FinishWorkoutSessionUseCase.ExerciseLogCommand;
import com.powertrack.backend.application.workout.port.in.FinishWorkoutSessionUseCase.FinishSessionCommand;
import com.powertrack.backend.application.workout.port.in.FinishWorkoutSessionUseCase.SetCommand;
import com.powertrack.backend.domain.workout.OverallFeeling;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * {@code endTime} lo reporta el cliente (no el servidor), mismo criterio que
 * {@code StartWorkoutSessionRequest#startTime} — sin validación de rango, decisión
 * explícita de producto.
 */
public record FinishWorkoutSessionRequest(
        @NotNull OverallFeeling overallFeeling,
        @NotEmpty @Valid List<ExerciseLogRequest> exerciseLogs,
        @NotNull Instant endTime
) {

    public FinishSessionCommand toCommand(UUID sessionId, UUID userId) {
        List<ExerciseLogCommand> logs = exerciseLogs.stream().map(ExerciseLogRequest::toCommand).toList();
        return new FinishSessionCommand(sessionId, userId, overallFeeling, logs, endTime);
    }

    public record ExerciseLogRequest(
            @NotNull UUID routineExerciseId,
            String notes,
            @NotEmpty @Valid List<SetRequest> sets
    ) {
        public ExerciseLogCommand toCommand() {
            List<SetCommand> setCommands = sets.stream().map(SetRequest::toCommand).toList();
            return new ExerciseLogCommand(routineExerciseId, notes, setCommands);
        }
    }

    public record SetRequest(
            @NotNull @Min(1) Integer setNumber,
            @NotNull @DecimalMin(value = "0.0") BigDecimal weightKg,
            @NotNull @Min(1) Integer repsCompleted,
            @NotNull @Min(1) @Max(10) Integer rpe
    ) {
        public SetCommand toCommand() {
            return new SetCommand(setNumber, weightKg, repsCompleted, rpe);
        }
    }
}
