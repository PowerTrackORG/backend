package com.powertrack.backend.infrastructure.adapter.out.persistence;

import com.powertrack.backend.application.workout.port.out.WorkoutLogRepositoryPort;
import com.powertrack.backend.domain.workout.HistoricalExerciseSummary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class WorkoutLogPersistenceAdapter implements WorkoutLogRepositoryPort {

    private final WorkoutLogJpaRepository jpaRepository;

    public WorkoutLogPersistenceAdapter(WorkoutLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<PreviousLog> findLatestByRoutineExerciseIdAndUserId(UUID routineExerciseId, UUID userId) {
        List<WorkoutLogJpaEntity> results = jpaRepository.findMostRecentCompletedLogs(
                routineExerciseId, userId, PageRequest.of(0, 1));
        return results.stream().findFirst().map(this::toPreviousLog);
    }

    @Override
    public List<HistoricalExerciseSummary> findRecentSessionSummaries(UUID routineExerciseId, UUID userId, int limit) {
        List<WorkoutLogJpaEntity> results = jpaRepository.findMostRecentCompletedLogs(
                routineExerciseId, userId, PageRequest.of(0, limit));
        return results.stream().map(this::toSummary).toList();
    }

    private PreviousLog toPreviousLog(WorkoutLogJpaEntity entity) {
        List<LogSetData> sets = entity.getSets().stream()
                .sorted(Comparator.comparingInt(LogSetJpaEntity::getSetNumber))
                .map(s -> new LogSetData(s.getSetNumber(), s.getWeightKg(), s.getRepsCompleted(), s.getRpe()))
                .toList();
        // Se usa el inicio de la sesión como "cuándo se entrenó" (performedAt).
        return new PreviousLog(entity.getId(), entity.getSession().getStartTime(), entity.getNotes(), sets);
    }

    private HistoricalExerciseSummary toSummary(WorkoutLogJpaEntity entity) {
        List<LogSetJpaEntity> sets = entity.getSets();
        BigDecimal maxWeight = sets.stream()
                .map(LogSetJpaEntity::getWeightKg)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        double avgReps = sets.stream().mapToInt(LogSetJpaEntity::getRepsCompleted).average().orElse(0);
        double avgRpe = sets.stream().mapToInt(LogSetJpaEntity::getRpe).average().orElse(0);
        return new HistoricalExerciseSummary(maxWeight, avgReps, avgRpe, entity.getSession().getOverallFeeling());
    }
}
