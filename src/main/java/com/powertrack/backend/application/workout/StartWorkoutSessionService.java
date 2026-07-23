package com.powertrack.backend.application.workout;

import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.application.workout.exception.RoutineDayNotFoundException;
import com.powertrack.backend.application.workout.port.in.StartWorkoutSessionUseCase;
import com.powertrack.backend.application.workout.port.out.WorkoutSessionRepositoryPort;
import com.powertrack.backend.domain.workout.WorkoutSession;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StartWorkoutSessionService implements StartWorkoutSessionUseCase {

    private final WorkoutSessionRepositoryPort workoutSessionRepository;
    private final RoutineRepositoryPort routineRepository;

    public StartWorkoutSessionService(WorkoutSessionRepositoryPort workoutSessionRepository,
                                       RoutineRepositoryPort routineRepository) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.routineRepository = routineRepository;
    }

    @Override
    public StartedSessionResult start(StartSessionCommand command) {
        // Idempotencia de sync offline: si este sessionId (generado por el cliente) ya
        // fue iniciado por este mismo usuario, es un reintento — se devuelve tal cual sin
        // volver a insertar. Ver Docs/decisiones-tecnicas.md, 2026-07-23.
        Optional<WorkoutSession> existing = workoutSessionRepository.findByIdAndUserId(command.sessionId(), command.userId());
        if (existing.isPresent()) {
            WorkoutSession session = existing.get();
            return new StartedSessionResult(session.getId(), session.getRoutineDayId(), session.getStartTime());
        }
        // El sessionId existe pero pertenece a otro usuario: colisión real, se rechaza.
        if (workoutSessionRepository.existsById(command.sessionId())) {
            throw new IllegalArgumentException("sessionId ya está en uso");
        }

        // Reutiliza el ownership check ya existente del módulo de Rutinas en vez de
        // duplicar la lógica de pertenencia (routineDay -> routine -> userId).
        if (!routineRepository.existsRoutineDayOwnedByUser(command.routineDayId(), command.userId())) {
            throw new RoutineDayNotFoundException(command.routineDayId());
        }

        WorkoutSession session = WorkoutSession.start(command.sessionId(), command.userId(), command.routineDayId(),
                command.startTime());
        WorkoutSession saved = workoutSessionRepository.save(session);

        return new StartedSessionResult(saved.getId(), saved.getRoutineDayId(), saved.getStartTime());
    }
}
