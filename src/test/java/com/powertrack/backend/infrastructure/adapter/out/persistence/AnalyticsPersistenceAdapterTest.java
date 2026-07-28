package com.powertrack.backend.infrastructure.adapter.out.persistence;

import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort.ExerciseSessionSets;
import com.powertrack.backend.domain.workout.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsPersistenceAdapterTest {

    @Mock
    private WorkoutLogJpaRepository workoutLogJpaRepository;
    @Mock
    private WorkoutSessionJpaRepository workoutSessionJpaRepository;

    private AnalyticsPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AnalyticsPersistenceAdapter(workoutLogJpaRepository, workoutSessionJpaRepository);
    }

    private WorkoutSessionJpaEntity session(Instant endTime) {
        return new WorkoutSessionJpaEntity(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                endTime.minusSeconds(3600), endTime, SessionStatus.COMPLETED, null);
    }

    private WorkoutLogJpaEntity logWithSets(WorkoutSessionJpaEntity session, UUID routineExerciseId, BigDecimal weight, int reps, int rpe) {
        WorkoutLogJpaEntity log = new WorkoutLogJpaEntity(UUID.randomUUID(), session, routineExerciseId, null);
        LogSetJpaEntity set = new LogSetJpaEntity(UUID.randomUUID(), log, 1, weight, reps, rpe);
        log.replaceSets(List.of(set));
        return log;
    }

    @Test
    void combinaEnUnSoloPuntoLasSeriesDeDosLogsDistintosDeLaMismaSesion() {
        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant endTime = Instant.now();
        WorkoutSessionJpaEntity sharedSession = session(endTime);

        // 2 routineExercise distintos del mismo exerciseId, dentro de la MISMA sesión.
        WorkoutLogJpaEntity firstLog = logWithSets(sharedSession, UUID.randomUUID(), BigDecimal.valueOf(100), 8, 8);
        WorkoutLogJpaEntity secondLog = logWithSets(sharedSession, UUID.randomUUID(), BigDecimal.valueOf(90), 10, 7);

        when(workoutLogJpaRepository.findCompletedLogsForExercise(any(), any(), any(), any()))
                .thenReturn(List.of(firstLog, secondLog));

        List<ExerciseSessionSets> result = adapter.findCompletedSessionSetsForExercise(
                exerciseId, userId, Instant.EPOCH, Instant.now());

        assertThat(result).hasSize(1);
        ExerciseSessionSets combined = result.get(0);
        assertThat(combined.sessionId()).isEqualTo(sharedSession.getId());
        assertThat(combined.sessionEndTime()).isEqualTo(endTime);
        assertThat(combined.sets()).hasSize(2);
    }

    @Test
    void recortaADosSesionesPorRoutineExerciseIdTomandoLasDosPrimerasDelGrupoNoLasUltimas() {
        UUID routineExerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Instant mostRecent = Instant.now();
        Instant middle = mostRecent.minusSeconds(86_400);
        Instant oldest = mostRecent.minusSeconds(2 * 86_400L);

        // La query ya viene ordenada por endTime DESC: más reciente primero.
        WorkoutLogJpaEntity mostRecentLog = logWithSets(session(mostRecent), routineExerciseId, BigDecimal.valueOf(100), 8, 8);
        WorkoutLogJpaEntity middleLog = logWithSets(session(middle), routineExerciseId, BigDecimal.valueOf(95), 8, 8);
        WorkoutLogJpaEntity oldestLog = logWithSets(session(oldest), routineExerciseId, BigDecimal.valueOf(90), 8, 8);

        when(workoutLogJpaRepository.findCompletedLogsForRoutineExercises(any(), any()))
                .thenReturn(List.of(mostRecentLog, middleLog, oldestLog));

        Map<UUID, List<ExerciseSessionSets>> result = adapter.findLastTwoSessionsPerRoutineExercise(
                List.of(routineExerciseId), userId);

        assertThat(result).containsKey(routineExerciseId);
        List<ExerciseSessionSets> sessions = result.get(routineExerciseId);
        assertThat(sessions).hasSize(2);
        assertThat(sessions.get(0).sessionEndTime()).isEqualTo(mostRecent);
        assertThat(sessions.get(1).sessionEndTime()).isEqualTo(middle);
        // La sesión más antigua queda fuera del corte de 2.
        assertThat(sessions).noneMatch(s -> s.sessionEndTime().equals(oldest));
    }
}
