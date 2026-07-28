package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.analytics.port.in.GetRoutineAnalyticsUseCase.DayResult;
import com.powertrack.backend.application.analytics.port.in.GetRoutineAnalyticsUseCase.ExerciseProgressResult;
import com.powertrack.backend.application.analytics.port.in.GetRoutineAnalyticsUseCase.ReferenceSetResult;
import com.powertrack.backend.application.analytics.port.in.GetRoutineAnalyticsUseCase.RoutineAnalyticsResult;
import com.powertrack.backend.application.analytics.port.in.GetRoutineAnalyticsUseCase.SessionSnapshotResult;
import com.powertrack.backend.application.analytics.port.in.GetRoutineAnalyticsUseCase.SummaryResult;
import com.powertrack.backend.application.analytics.port.in.GetRoutineAnalyticsUseCase.WindowResult;
import com.powertrack.backend.domain.analytics.ProgressStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoutineAnalyticsResponse(UUID routineId, String routineName, WindowResponse window,
                                        BigDecimal totalVolumeKg, SummaryResponse summary, List<DayResponse> byDay) {

    public static RoutineAnalyticsResponse from(RoutineAnalyticsResult result) {
        List<DayResponse> byDay = result.byDay().stream().map(DayResponse::from).toList();
        return new RoutineAnalyticsResponse(result.routineId(), result.routineName(), WindowResponse.from(result.window()),
                result.totalVolumeKg(), SummaryResponse.from(result.summary()), byDay);
    }

    public record WindowResponse(Instant from, Instant to, long completedSessions, Double averageDaysBetweenSessions) {
        public static WindowResponse from(WindowResult result) {
            return new WindowResponse(result.from(), result.to(), result.completedSessions(),
                    result.averageDaysBetweenSessions());
        }
    }

    public record SummaryResponse(long progressed, long maintained, long regressed, long insufficientData) {
        public static SummaryResponse from(SummaryResult result) {
            return new SummaryResponse(result.progressed(), result.maintained(), result.regressed(),
                    result.insufficientData());
        }
    }

    public record DayResponse(UUID routineDayId, String dayName, List<ExerciseProgressResponse> exercises) {
        public static DayResponse from(DayResult result) {
            List<ExerciseProgressResponse> exercises = result.exercises().stream()
                    .map(ExerciseProgressResponse::from)
                    .toList();
            return new DayResponse(result.routineDayId(), result.dayName(), exercises);
        }
    }

    public record ExerciseProgressResponse(UUID routineExerciseId, UUID exerciseId, String exerciseName,
                                            String targetMuscle, ProgressStatus progressStatus,
                                            SessionSnapshotResponse lastSession, SessionSnapshotResponse previousSession) {
        public static ExerciseProgressResponse from(ExerciseProgressResult result) {
            SessionSnapshotResponse last = result.lastSession() != null ? SessionSnapshotResponse.from(result.lastSession()) : null;
            SessionSnapshotResponse previous = result.previousSession() != null
                    ? SessionSnapshotResponse.from(result.previousSession()) : null;
            return new ExerciseProgressResponse(result.routineExerciseId(), result.exerciseId(), result.exerciseName(),
                    result.targetMuscle(), result.progressStatus(), last, previous);
        }
    }

    public record SessionSnapshotResponse(Instant date, ReferenceSetResponse referenceSet) {
        public static SessionSnapshotResponse from(SessionSnapshotResult result) {
            return new SessionSnapshotResponse(result.date(), ReferenceSetResponse.from(result.referenceSet()));
        }
    }

    public record ReferenceSetResponse(BigDecimal weightKg, int reps, int rpe) {
        public static ReferenceSetResponse from(ReferenceSetResult result) {
            return new ReferenceSetResponse(result.weightKg(), result.reps(), result.rpe());
        }
    }
}
