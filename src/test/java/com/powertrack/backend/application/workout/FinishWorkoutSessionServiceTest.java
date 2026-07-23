package com.powertrack.backend.application.workout;

import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort.RoutineExerciseTargets;
import com.powertrack.backend.application.workout.exception.RoutineExerciseNotFoundException;
import com.powertrack.backend.application.workout.exception.WorkoutSessionAlreadyFinishedException;
import com.powertrack.backend.application.workout.exception.WorkoutSessionNotFoundException;
import com.powertrack.backend.application.workout.port.in.FinishWorkoutSessionUseCase.ExerciseLogCommand;
import com.powertrack.backend.application.workout.port.in.FinishWorkoutSessionUseCase.FinishSessionCommand;
import com.powertrack.backend.application.workout.port.in.FinishWorkoutSessionUseCase.FinishSessionResult;
import com.powertrack.backend.application.workout.port.in.FinishWorkoutSessionUseCase.SetCommand;
import com.powertrack.backend.application.workout.port.out.WorkoutLogRepositoryPort;
import com.powertrack.backend.application.workout.port.out.WorkoutSessionRepositoryPort;
import com.powertrack.backend.domain.workout.OverallFeeling;
import com.powertrack.backend.domain.workout.Recommendation;
import com.powertrack.backend.domain.workout.WorkoutSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinishWorkoutSessionServiceTest {

    @Mock
    private WorkoutSessionRepositoryPort workoutSessionRepository;
    @Mock
    private WorkoutLogRepositoryPort workoutLogRepository;
    @Mock
    private RoutineRepositoryPort routineRepository;

    private FinishWorkoutSessionService service;

    @BeforeEach
    void setUp() {
        service = new FinishWorkoutSessionService(workoutSessionRepository, workoutLogRepository, routineRepository);
    }

    @Test
    void lanzaWorkoutSessionNotFoundCuandoLaSesionNoExisteOPerteneceAOtroUsuario() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        FinishSessionCommand command = new FinishSessionCommand(sessionId, userId, OverallFeeling.BUENA, List.of(), Instant.now());

        when(workoutSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.finish(command))
                .isInstanceOf(WorkoutSessionNotFoundException.class);

        verify(workoutSessionRepository, never()).save(any());
    }

    @Test
    void lanzaWorkoutSessionAlreadyFinishedCuandoLaSesionYaEstaCompletada() {
        UUID userId = UUID.randomUUID();
        UUID routineDayId = UUID.randomUUID();
        WorkoutSession inProgress = WorkoutSession.start(UUID.randomUUID(), userId, routineDayId, Instant.now());
        WorkoutSession alreadyFinished = inProgress.finish(OverallFeeling.BUENA, List.of(), Instant.now());

        FinishSessionCommand command = new FinishSessionCommand(alreadyFinished.getId(), userId, OverallFeeling.BUENA, List.of(), Instant.now());

        when(workoutSessionRepository.findByIdAndUserId(alreadyFinished.getId(), userId)).thenReturn(Optional.of(alreadyFinished));

        assertThatThrownBy(() -> service.finish(command))
                .isInstanceOf(WorkoutSessionAlreadyFinishedException.class);

        verify(workoutSessionRepository, never()).save(any());
    }

    @Test
    void lanzaRoutineExerciseNotFoundCuandoElEjercicioNoPerteneceAlUsuario() {
        UUID userId = UUID.randomUUID();
        UUID routineDayId = UUID.randomUUID();
        UUID routineExerciseId = UUID.randomUUID();
        WorkoutSession inProgress = WorkoutSession.start(UUID.randomUUID(), userId, routineDayId, Instant.now());

        ExerciseLogCommand exerciseLog = new ExerciseLogCommand(routineExerciseId, null,
                List.of(new SetCommand(1, BigDecimal.valueOf(100), 10, 8)));
        FinishSessionCommand command = new FinishSessionCommand(inProgress.getId(), userId, OverallFeeling.BUENA, List.of(exerciseLog), Instant.now());

        when(workoutSessionRepository.findByIdAndUserId(inProgress.getId(), userId)).thenReturn(Optional.of(inProgress));
        when(routineRepository.findRoutineExerciseTargets(routineExerciseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.finish(command))
                .isInstanceOf(RoutineExerciseNotFoundException.class);

        verify(workoutSessionRepository, never()).save(any());
    }

    @Test
    void finalizaLaSesionYDevuelveUnaSugerenciaDistintaPorEjercicioSegunElMotorDeReglas() {
        UUID userId = UUID.randomUUID();
        UUID routineDayId = UUID.randomUUID();
        WorkoutSession inProgress = WorkoutSession.start(UUID.randomUUID(), userId, routineDayId, Instant.now());

        UUID benchPressId = UUID.randomUUID();
        UUID squatId = UUID.randomUUID();

        // Press banca: todas las series >= target_max, RPE bajo, sensación buena ->
        // debe disparar la Regla 1 (Aumentar Peso).
        ExerciseLogCommand benchPressLog = new ExerciseLogCommand(benchPressId, "buena técnica",
                List.of(new SetCommand(1, BigDecimal.valueOf(100), 12, 7),
                        new SetCommand(2, BigDecimal.valueOf(100), 13, 8)));

        // Sentadilla: 3 de 4 series por debajo de target_min -> debe disparar la Regla 4
        // (Reducir Peso), incluso con la misma sensación "Buena" de la sesión.
        ExerciseLogCommand squatLog = new ExerciseLogCommand(squatId, null,
                List.of(new SetCommand(1, BigDecimal.valueOf(80), 4, 9),
                        new SetCommand(2, BigDecimal.valueOf(80), 5, 9),
                        new SetCommand(3, BigDecimal.valueOf(80), 5, 9),
                        new SetCommand(4, BigDecimal.valueOf(80), 9, 9)));

        FinishSessionCommand command = new FinishSessionCommand(inProgress.getId(), userId, OverallFeeling.BUENA,
                List.of(benchPressLog, squatLog), Instant.now());

        when(workoutSessionRepository.findByIdAndUserId(inProgress.getId(), userId)).thenReturn(Optional.of(inProgress));
        when(routineRepository.findRoutineExerciseTargets(eq(benchPressId), eq(userId)))
                .thenReturn(Optional.of(new RoutineExerciseTargets(benchPressId, 3, 8, 12)));
        when(routineRepository.findRoutineExerciseTargets(eq(squatId), eq(userId)))
                .thenReturn(Optional.of(new RoutineExerciseTargets(squatId, 4, 8, 12)));
        when(workoutLogRepository.findRecentSessionSummaries(any(UUID.class), eq(userId), anyInt()))
                .thenReturn(List.of());
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinishSessionResult result = service.finish(command);

        assertThat(result.overallFeeling()).isEqualTo(OverallFeeling.BUENA);
        assertThat(result.endTime()).isNotNull();
        assertThat(result.suggestions()).hasSize(2);

        var benchSuggestion = result.suggestions().stream()
                .filter(s -> s.routineExerciseId().equals(benchPressId)).findFirst().orElseThrow();
        var squatSuggestion = result.suggestions().stream()
                .filter(s -> s.routineExerciseId().equals(squatId)).findFirst().orElseThrow();

        assertThat(benchSuggestion.recommendation()).isEqualTo(Recommendation.INCREASE_WEIGHT);
        assertThat(squatSuggestion.recommendation()).isEqualTo(Recommendation.DECREASE_WEIGHT);

        ArgumentCaptor<WorkoutSession> captor = ArgumentCaptor.forClass(WorkoutSession.class);
        verify(workoutSessionRepository).save(captor.capture());
        WorkoutSession savedSession = captor.getValue();
        assertThat(savedSession.getStatus().name()).isEqualTo("COMPLETED");
        assertThat(savedSession.getLogs()).hasSize(2);
    }

    @Test
    void elHistoricoDeDeloadSeConsultaAntesDePersistirLaSesionActualPorLoQueLaExcluyeNaturalmente() {
        UUID userId = UUID.randomUUID();
        UUID routineDayId = UUID.randomUUID();
        UUID routineExerciseId = UUID.randomUUID();
        WorkoutSession inProgress = WorkoutSession.start(UUID.randomUUID(), userId, routineDayId, Instant.now());

        ExerciseLogCommand log = new ExerciseLogCommand(routineExerciseId, null,
                List.of(new SetCommand(1, BigDecimal.valueOf(100), 12, 7)));
        FinishSessionCommand command = new FinishSessionCommand(inProgress.getId(), userId, OverallFeeling.BUENA, List.of(log), Instant.now());

        when(workoutSessionRepository.findByIdAndUserId(inProgress.getId(), userId)).thenReturn(Optional.of(inProgress));
        when(routineRepository.findRoutineExerciseTargets(routineExerciseId, userId))
                .thenReturn(Optional.of(new RoutineExerciseTargets(routineExerciseId, 3, 8, 12)));
        when(workoutLogRepository.findRecentSessionSummaries(routineExerciseId, userId, 2)).thenReturn(List.of());
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.finish(command);

        verify(workoutLogRepository).findRecentSessionSummaries(routineExerciseId, userId, 2);
    }
}
