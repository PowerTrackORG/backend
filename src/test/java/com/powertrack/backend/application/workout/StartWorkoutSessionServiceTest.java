package com.powertrack.backend.application.workout;

import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.application.workout.exception.RoutineDayNotFoundException;
import com.powertrack.backend.application.workout.port.in.StartWorkoutSessionUseCase.StartSessionCommand;
import com.powertrack.backend.application.workout.port.in.StartWorkoutSessionUseCase.StartedSessionResult;
import com.powertrack.backend.application.workout.port.out.WorkoutSessionRepositoryPort;
import com.powertrack.backend.domain.workout.WorkoutSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartWorkoutSessionServiceTest {

    @Mock
    private WorkoutSessionRepositoryPort workoutSessionRepository;
    @Mock
    private RoutineRepositoryPort routineRepository;

    private StartWorkoutSessionService service;

    @BeforeEach
    void setUp() {
        service = new StartWorkoutSessionService(workoutSessionRepository, routineRepository);
    }

    @Test
    void iniciaUnaSesionCuandoElDiaDeRutinaPerteneceAlUsuario() {
        UUID userId = UUID.randomUUID();
        UUID routineDayId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Instant startTime = Instant.now();

        when(routineRepository.existsRoutineDayOwnedByUser(routineDayId, userId)).thenReturn(true);
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StartedSessionResult result = service.start(new StartSessionCommand(sessionId, userId, routineDayId, startTime));

        ArgumentCaptor<WorkoutSession> captor = ArgumentCaptor.forClass(WorkoutSession.class);
        verify(workoutSessionRepository).save(captor.capture());
        WorkoutSession savedSession = captor.getValue();

        assertThat(savedSession.getId()).isEqualTo(sessionId);
        assertThat(savedSession.getUserId()).isEqualTo(userId);
        assertThat(savedSession.getRoutineDayId()).isEqualTo(routineDayId);
        assertThat(savedSession.getStartTime()).isEqualTo(startTime);
        assertThat(savedSession.getStatus().name()).isEqualTo("IN_PROGRESS");

        assertThat(result.routineDayId()).isEqualTo(routineDayId);
        assertThat(result.sessionId()).isEqualTo(sessionId);
    }

    @Test
    void rechazaElInicioSiElDiaDeRutinaNoPerteneceAlUsuarioYNoGuardaNada() {
        UUID userId = UUID.randomUUID();
        UUID routineDayId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        when(routineRepository.existsRoutineDayOwnedByUser(routineDayId, userId)).thenReturn(false);

        assertThatThrownBy(() -> service.start(new StartSessionCommand(sessionId, userId, routineDayId, Instant.now())))
                .isInstanceOf(RoutineDayNotFoundException.class);

        verify(workoutSessionRepository, never()).save(any());
    }

    @Test
    void reintentoDeSyncConElMismoSessionIdDevuelveLaSesionExistenteSinReinsertar() {
        UUID userId = UUID.randomUUID();
        UUID routineDayId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Instant startTime = Instant.now();
        WorkoutSession existingSession = WorkoutSession.start(sessionId, userId, routineDayId, startTime);

        when(workoutSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(existingSession));

        StartedSessionResult result = service.start(new StartSessionCommand(sessionId, userId, routineDayId, startTime));

        assertThat(result.sessionId()).isEqualTo(sessionId);
        verify(workoutSessionRepository, never()).save(any());
    }

    @Test
    void rechazaElInicioSiElSessionIdYaPerteneceAOtroUsuario() {
        UUID userId = UUID.randomUUID();
        UUID routineDayId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        when(workoutSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.empty());
        when(workoutSessionRepository.existsById(sessionId)).thenReturn(true);

        assertThatThrownBy(() -> service.start(new StartSessionCommand(sessionId, userId, routineDayId, Instant.now())))
                .isInstanceOf(IllegalArgumentException.class);

        verify(workoutSessionRepository, never()).save(any());
    }
}
