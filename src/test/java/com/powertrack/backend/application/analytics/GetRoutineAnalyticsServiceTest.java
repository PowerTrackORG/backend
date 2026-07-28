package com.powertrack.backend.application.analytics;

import com.powertrack.backend.application.analytics.port.in.GetRoutineAnalyticsUseCase.ExerciseProgressResult;
import com.powertrack.backend.application.analytics.port.in.GetRoutineAnalyticsUseCase.RoutineAnalyticsResult;
import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort;
import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort.ExerciseSessionSets;
import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort.SetData;
import com.powertrack.backend.application.routine.exception.RoutineNotFoundException;
import com.powertrack.backend.application.routine.port.out.ExerciseRepositoryPort;
import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.domain.analytics.ProgressStatus;
import com.powertrack.backend.domain.routine.Exercise;
import com.powertrack.backend.domain.routine.Routine;
import com.powertrack.backend.domain.routine.RoutineDay;
import com.powertrack.backend.domain.routine.RoutineExercise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRoutineAnalyticsServiceTest {

    @Mock
    private RoutineRepositoryPort routineRepository;
    @Mock
    private ExerciseRepositoryPort exerciseRepository;
    @Mock
    private AnalyticsRepositoryPort analyticsRepository;

    private GetRoutineAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new GetRoutineAnalyticsService(routineRepository, exerciseRepository, analyticsRepository);
    }

    @Test
    void lanza404CuandoLaRutinaNoExisteOPerteneceAOtroUsuario() {
        UUID routineId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(routineRepository.findByIdAndUserId(routineId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRoutineAnalytics(routineId, userId, null, null))
                .isInstanceOf(RoutineNotFoundException.class);
    }

    @Test
    void recorreTodosLosDiasYEjerciciosConfiguradosMarcandoInsufficientDataSinHistorico() {
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();
        UUID exerciseWithHistoryId = UUID.randomUUID();
        UUID exerciseWithoutHistoryId = UUID.randomUUID();

        RoutineExercise withHistory = RoutineExercise.create(exerciseWithHistoryId, 0, 4, 8, 12, 90, null);
        RoutineExercise withoutHistory = RoutineExercise.create(exerciseWithoutHistoryId, 1, 4, 8, 12, 90, null);
        RoutineDay day = RoutineDay.create("Día A", 0, List.of(withHistory, withoutHistory));
        Routine routine = Routine.rehydrate(routineId, userId, "Full Body", "desc", Instant.now(), List.of(day));

        when(routineRepository.findByIdAndUserId(routineId, userId)).thenReturn(Optional.of(routine));
        when(exerciseRepository.findAllByIds(any())).thenReturn(Map.of(
                exerciseWithHistoryId, Exercise.rehydrate(exerciseWithHistoryId, "Sentadilla", "Piernas", "Empuje", null),
                exerciseWithoutHistoryId, Exercise.rehydrate(exerciseWithoutHistoryId, "Curl", "Biceps", "Tracción", null)));

        Instant lastEnd = Instant.now();
        Instant previousEnd = lastEnd.minusSeconds(7 * 86_400L);
        ExerciseSessionSets lastSession = new ExerciseSessionSets(UUID.randomUUID(), lastEnd,
                List.of(new SetData(BigDecimal.valueOf(100), 8, 7)));
        ExerciseSessionSets previousSession = new ExerciseSessionSets(UUID.randomUUID(), previousEnd,
                List.of(new SetData(BigDecimal.valueOf(90), 8, 7)));

        when(analyticsRepository.findLastTwoSessionsPerRoutineExercise(any(), eq(userId)))
                .thenReturn(Map.of(withHistory.getId(), List.of(lastSession, previousSession)));
        when(analyticsRepository.sumVolumeForRoutine(eq(routineId), eq(userId), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000));
        when(analyticsRepository.findCompletedSessionEndTimesForRoutine(eq(routineId), eq(userId), any(), any()))
                .thenReturn(List.of(previousEnd, lastEnd));

        RoutineAnalyticsResult result = service.getRoutineAnalytics(routineId, userId, null, null);

        assertThat(result.byDay()).hasSize(1);
        List<ExerciseProgressResult> exercises = result.byDay().get(0).exercises();
        assertThat(exercises).hasSize(2);

        ExerciseProgressResult withHistoryResult = exercises.stream()
                .filter(e -> e.routineExerciseId().equals(withHistory.getId())).findFirst().orElseThrow();
        ExerciseProgressResult withoutHistoryResult = exercises.stream()
                .filter(e -> e.routineExerciseId().equals(withoutHistory.getId())).findFirst().orElseThrow();

        assertThat(withHistoryResult.progressStatus()).isEqualTo(ProgressStatus.PROGRESSED);
        assertThat(withHistoryResult.lastSession()).isNotNull();
        assertThat(withHistoryResult.previousSession()).isNotNull();

        assertThat(withoutHistoryResult.progressStatus()).isEqualTo(ProgressStatus.INSUFFICIENT_DATA);
        assertThat(withoutHistoryResult.lastSession()).isNull();
        assertThat(withoutHistoryResult.previousSession()).isNull();

        assertThat(result.summary().progressed()).isEqualTo(1);
        assertThat(result.summary().insufficientData()).isEqualTo(1);
        assertThat(result.totalVolumeKg()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void averageDaysBetweenSessionsEsNuloConMenosDeDosSesionesEnLaVentana() {
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();
        Routine routine = Routine.rehydrate(routineId, userId, "Rutina vacía", null, Instant.now(), List.of());

        when(routineRepository.findByIdAndUserId(routineId, userId)).thenReturn(Optional.of(routine));
        when(exerciseRepository.findAllByIds(any())).thenReturn(Map.of());
        when(analyticsRepository.findLastTwoSessionsPerRoutineExercise(any(), eq(userId))).thenReturn(Map.of());
        when(analyticsRepository.sumVolumeForRoutine(eq(routineId), eq(userId), any(), any())).thenReturn(BigDecimal.ZERO);
        when(analyticsRepository.findCompletedSessionEndTimesForRoutine(eq(routineId), eq(userId), any(), any()))
                .thenReturn(List.of(Instant.now()));

        RoutineAnalyticsResult result = service.getRoutineAnalytics(routineId, userId, null, null);

        assertThat(result.window().averageDaysBetweenSessions()).isNull();
        assertThat(result.window().completedSessions()).isEqualTo(1);
    }

    @Test
    void calculaCorrectamenteElPromedioDeDiasEntreSesionesConTresOMasSesiones() {
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();
        Routine routine = Routine.rehydrate(routineId, userId, "Rutina", null, Instant.now(), List.of());

        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = t1.plusSeconds(2 * 86_400L);
        Instant t3 = t2.plusSeconds(4 * 86_400L);
        // gaps: 2 días, luego 4 días -> promedio 3 días.

        when(routineRepository.findByIdAndUserId(routineId, userId)).thenReturn(Optional.of(routine));
        when(exerciseRepository.findAllByIds(any())).thenReturn(Map.of());
        when(analyticsRepository.findLastTwoSessionsPerRoutineExercise(any(), eq(userId))).thenReturn(Map.of());
        when(analyticsRepository.sumVolumeForRoutine(eq(routineId), eq(userId), any(), any())).thenReturn(BigDecimal.ZERO);
        when(analyticsRepository.findCompletedSessionEndTimesForRoutine(eq(routineId), eq(userId), any(), any()))
                .thenReturn(List.of(t1, t2, t3));

        RoutineAnalyticsResult result = service.getRoutineAnalytics(routineId, userId, null, null);

        assertThat(result.window().averageDaysBetweenSessions()).isEqualTo(3.0);
        assertThat(result.window().completedSessions()).isEqualTo(3);
    }

    @Test
    void aplicaVentanaPorDefectoDeTreintaDiasCuandoNoSePasanFromNiTo() {
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();
        Routine routine = Routine.rehydrate(routineId, userId, "Rutina", null, Instant.now(), List.of());

        when(routineRepository.findByIdAndUserId(routineId, userId)).thenReturn(Optional.of(routine));
        when(exerciseRepository.findAllByIds(any())).thenReturn(Map.of());
        when(analyticsRepository.findLastTwoSessionsPerRoutineExercise(any(), eq(userId))).thenReturn(Map.of());
        when(analyticsRepository.sumVolumeForRoutine(eq(routineId), eq(userId), any(), any())).thenReturn(BigDecimal.ZERO);
        when(analyticsRepository.findCompletedSessionEndTimesForRoutine(eq(routineId), eq(userId), any(), any()))
                .thenReturn(List.of());

        service.getRoutineAnalytics(routineId, userId, null, null);

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(analyticsRepository).sumVolumeForRoutine(eq(routineId), eq(userId), fromCaptor.capture(), toCaptor.capture());

        Duration window = Duration.between(fromCaptor.getValue(), toCaptor.getValue());
        assertThat(window).isCloseTo(Duration.ofDays(30), Duration.ofSeconds(5));
    }
}
