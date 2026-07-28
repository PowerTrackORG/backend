package com.powertrack.backend.infrastructure.adapter.out.persistence;

import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AnalyticsPersistenceAdapter implements AnalyticsRepositoryPort {

    /**
     * Cuántas sesiones históricas más recientes se conservan por {@code routineExerciseId}
     * en {@link #findLastTwoSessionsPerRoutineExercise}: la clasificación de progreso
     * (módulo Analytics) solo necesita comparar la última contra la inmediatamente
     * anterior.
     */
    private static final int MAX_SESSIONS_PER_ROUTINE_EXERCISE = 2;

    private final WorkoutLogJpaRepository workoutLogJpaRepository;
    private final WorkoutSessionJpaRepository workoutSessionJpaRepository;

    public AnalyticsPersistenceAdapter(WorkoutLogJpaRepository workoutLogJpaRepository,
                                        WorkoutSessionJpaRepository workoutSessionJpaRepository) {
        this.workoutLogJpaRepository = workoutLogJpaRepository;
        this.workoutSessionJpaRepository = workoutSessionJpaRepository;
    }

    @Override
    public List<ExerciseSessionSets> findCompletedSessionSetsForExercise(UUID exerciseId, UUID userId, Instant from, Instant to) {
        List<WorkoutLogJpaEntity> logs = workoutLogJpaRepository.findCompletedLogsForExercise(exerciseId, userId, from, to);

        // LinkedHashMap para preservar el orden de llegada (la query ya viene ordenada
        // por endTime ASC): si el mismo exerciseId aparece en 2+ routineExercise
        // distintos dentro de la misma sesión, sus series se combinan en un solo punto.
        Map<UUID, List<WorkoutLogJpaEntity>> logsBySession = new LinkedHashMap<>();
        for (WorkoutLogJpaEntity log : logs) {
            logsBySession.computeIfAbsent(log.getSession().getId(), id -> new ArrayList<>()).add(log);
        }

        List<ExerciseSessionSets> result = new ArrayList<>();
        for (Map.Entry<UUID, List<WorkoutLogJpaEntity>> entry : logsBySession.entrySet()) {
            List<WorkoutLogJpaEntity> sessionLogs = entry.getValue();
            Instant sessionEndTime = sessionLogs.get(0).getSession().getEndTime();
            List<SetData> sets = sessionLogs.stream()
                    .flatMap(log -> log.getSets().stream())
                    .map(this::toSetData)
                    .toList();
            result.add(new ExerciseSessionSets(entry.getKey(), sessionEndTime, sets));
        }
        return result;
    }

    @Override
    public List<MuscleGroupVolume> sumVolumeByMuscleGroup(UUID userId, Instant from, Instant to) {
        return workoutSessionJpaRepository.sumVolumeByMuscleGroup(userId, from, to).stream()
                .map(p -> new MuscleGroupVolume(p.getTargetMuscle(), p.getTotalVolumeKg()))
                .toList();
    }

    @Override
    public BigDecimal sumVolumeForRoutine(UUID routineId, UUID userId, Instant from, Instant to) {
        return workoutSessionJpaRepository.sumVolumeForRoutine(routineId, userId, from, to);
    }

    @Override
    public List<Instant> findCompletedSessionEndTimesForRoutine(UUID routineId, UUID userId, Instant from, Instant to) {
        return workoutSessionJpaRepository.findCompletedSessionEndTimesForRoutine(routineId, userId, from, to);
    }

    @Override
    public Map<UUID, List<ExerciseSessionSets>> findLastTwoSessionsPerRoutineExercise(Collection<UUID> routineExerciseIds, UUID userId) {
        if (routineExerciseIds.isEmpty()) {
            return Map.of();
        }
        List<WorkoutLogJpaEntity> logs = workoutLogJpaRepository.findCompletedLogsForRoutineExercises(routineExerciseIds, userId);

        // LinkedHashMap para preservar el orden de llegada (la query ya viene ordenada
        // por routineExerciseId ASC, endTime DESC): los primeros 2 elementos de cada
        // grupo son, por construcción, las 2 sesiones más recientes.
        Map<UUID, List<WorkoutLogJpaEntity>> logsByRoutineExercise = new LinkedHashMap<>();
        for (WorkoutLogJpaEntity log : logs) {
            logsByRoutineExercise.computeIfAbsent(log.getRoutineExerciseId(), id -> new ArrayList<>()).add(log);
        }

        Map<UUID, List<ExerciseSessionSets>> result = new LinkedHashMap<>();
        for (Map.Entry<UUID, List<WorkoutLogJpaEntity>> entry : logsByRoutineExercise.entrySet()) {
            List<ExerciseSessionSets> sessions = entry.getValue().stream()
                    .limit(MAX_SESSIONS_PER_ROUTINE_EXERCISE)
                    .map(log -> new ExerciseSessionSets(log.getSession().getId(), log.getSession().getEndTime(),
                            log.getSets().stream().map(this::toSetData).toList()))
                    .toList();
            result.put(entry.getKey(), sessions);
        }
        return result;
    }

    private SetData toSetData(LogSetJpaEntity set) {
        return new SetData(set.getWeightKg(), set.getRepsCompleted(), set.getRpe());
    }
}
