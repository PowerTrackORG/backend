package com.powertrack.backend.application.workout.port.in;

import com.powertrack.backend.domain.workout.OverallFeeling;
import com.powertrack.backend.domain.workout.Recommendation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FinishWorkoutSessionUseCase {

    FinishSessionResult finish(FinishSessionCommand command);

    record FinishSessionCommand(UUID sessionId, UUID userId, OverallFeeling overallFeeling,
                                 List<ExerciseLogCommand> exerciseLogs) {
    }

    record ExerciseLogCommand(UUID routineExerciseId, String notes, List<SetCommand> sets) {
    }

    record SetCommand(int setNumber, BigDecimal weightKg, int repsCompleted, int rpe) {
    }

    record FinishSessionResult(UUID sessionId, Instant startTime, Instant endTime, OverallFeeling overallFeeling,
                                List<ExerciseSuggestionResult> suggestions) {
    }

    record ExerciseSuggestionResult(UUID routineExerciseId, Recommendation recommendation, String message) {
    }
}
