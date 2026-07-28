package com.powertrack.backend.application.analytics;

import com.powertrack.backend.application.analytics.port.in.GetExerciseAnalyticsUseCase;
import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort;
import com.powertrack.backend.application.routine.exception.ExerciseNotFoundException;
import com.powertrack.backend.application.routine.port.out.ExerciseRepositoryPort;
import com.powertrack.backend.domain.analytics.SessionAggregate;
import com.powertrack.backend.domain.analytics.SessionPerformanceAggregator;
import com.powertrack.backend.domain.analytics.SetSample;
import com.powertrack.backend.domain.routine.Exercise;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class GetExerciseAnalyticsService implements GetExerciseAnalyticsUseCase {

    private final ExerciseRepositoryPort exerciseRepository;
    private final AnalyticsRepositoryPort analyticsRepository;

    public GetExerciseAnalyticsService(ExerciseRepositoryPort exerciseRepository, AnalyticsRepositoryPort analyticsRepository) {
        this.exerciseRepository = exerciseRepository;
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    public ExerciseAnalyticsResult getExerciseAnalytics(UUID exerciseId, UUID userId, Instant from, Instant to) {
        // El catálogo de ejercicios no tiene dueño único (ver Exercise#isPredefined):
        // basta con que el ejercicio exista, no se valida ownership acá.
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        Instant effectiveFrom = from != null ? from : Instant.EPOCH;
        Instant effectiveTo = to != null ? to : Instant.now();

        List<AnalyticsRepositoryPort.ExerciseSessionSets> sessions = analyticsRepository
                .findCompletedSessionSetsForExercise(exerciseId, userId, effectiveFrom, effectiveTo);

        List<SessionPointResult> points = sessions.stream()
                .map(this::toPoint)
                .toList();

        return new ExerciseAnalyticsResult(exerciseId, exercise.getName(), points);
    }

    private SessionPointResult toPoint(AnalyticsRepositoryPort.ExerciseSessionSets session) {
        List<SetSample> samples = session.sets().stream()
                .map(s -> new SetSample(s.weightKg(), s.repsCompleted(), s.rpe()))
                .toList();
        SessionAggregate aggregate = SessionPerformanceAggregator.aggregate(samples);
        ReferenceSetResult referenceSet = new ReferenceSetResult(aggregate.referenceSet().weightKg(),
                aggregate.referenceSet().repsCompleted(), aggregate.referenceSet().rpe());
        return new SessionPointResult(session.sessionEndTime(), aggregate.estimated1RM(), aggregate.maxWeightKg(),
                aggregate.volumeKg(), referenceSet, aggregate.avgRpe(), aggregate.avgReps());
    }
}
