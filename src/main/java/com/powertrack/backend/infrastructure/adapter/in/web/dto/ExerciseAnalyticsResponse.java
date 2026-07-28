package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.analytics.port.in.GetExerciseAnalyticsUseCase.ExerciseAnalyticsResult;
import com.powertrack.backend.application.analytics.port.in.GetExerciseAnalyticsUseCase.ReferenceSetResult;
import com.powertrack.backend.application.analytics.port.in.GetExerciseAnalyticsUseCase.SessionPointResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExerciseAnalyticsResponse(UUID exerciseId, String exerciseName, List<SessionPointResponse> points) {

    public static ExerciseAnalyticsResponse from(ExerciseAnalyticsResult result) {
        List<SessionPointResponse> points = result.points().stream().map(SessionPointResponse::from).toList();
        return new ExerciseAnalyticsResponse(result.exerciseId(), result.exerciseName(), points);
    }

    public record SessionPointResponse(Instant sessionDate, BigDecimal estimated1RM, BigDecimal maxWeightKg,
                                        BigDecimal volumeKg, ReferenceSetResponse referenceSet, double avgRpe,
                                        double avgReps) {
        public static SessionPointResponse from(SessionPointResult result) {
            return new SessionPointResponse(result.sessionDate(), result.estimated1RM(), result.maxWeightKg(),
                    result.volumeKg(), ReferenceSetResponse.from(result.referenceSet()), result.avgRpe(), result.avgReps());
        }
    }

    public record ReferenceSetResponse(BigDecimal weightKg, int reps, int rpe) {
        public static ReferenceSetResponse from(ReferenceSetResult result) {
            return new ReferenceSetResponse(result.weightKg(), result.reps(), result.rpe());
        }
    }
}
