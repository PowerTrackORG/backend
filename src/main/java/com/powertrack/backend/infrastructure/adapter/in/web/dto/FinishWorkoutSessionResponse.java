package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.workout.port.in.FinishWorkoutSessionUseCase.ExerciseSuggestionResult;
import com.powertrack.backend.application.workout.port.in.FinishWorkoutSessionUseCase.FinishSessionResult;
import com.powertrack.backend.domain.workout.OverallFeeling;
import com.powertrack.backend.domain.workout.Recommendation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FinishWorkoutSessionResponse(UUID sessionId, Instant startTime, Instant endTime,
                                            OverallFeeling overallFeeling,
                                            List<ExerciseSuggestionResponse> suggestions) {

    public static FinishWorkoutSessionResponse from(FinishSessionResult result) {
        List<ExerciseSuggestionResponse> suggestions = result.suggestions().stream()
                .map(ExerciseSuggestionResponse::from)
                .toList();
        return new FinishWorkoutSessionResponse(result.sessionId(), result.startTime(), result.endTime(),
                result.overallFeeling(), suggestions);
    }

    public record ExerciseSuggestionResponse(UUID routineExerciseId, Recommendation recommendation, String message) {
        public static ExerciseSuggestionResponse from(ExerciseSuggestionResult result) {
            return new ExerciseSuggestionResponse(result.routineExerciseId(), result.recommendation(), result.message());
        }
    }
}
