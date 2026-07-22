package com.powertrack.backend.application.workout;

import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.application.workout.exception.RoutineDayNotFoundException;
import com.powertrack.backend.application.workout.port.in.StartWorkoutSessionUseCase;
import com.powertrack.backend.application.workout.port.out.WorkoutSessionRepositoryPort;
import com.powertrack.backend.domain.workout.WorkoutSession;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
    public StartedSessionResult start(UUID userId, UUID routineDayId) {
        // Reutiliza el ownership check ya existente del módulo de Rutinas en vez de
        // duplicar la lógica de pertenencia (routineDay -> routine -> userId).
        if (!routineRepository.existsRoutineDayOwnedByUser(routineDayId, userId)) {
            throw new RoutineDayNotFoundException(routineDayId);
        }

        WorkoutSession session = WorkoutSession.start(userId, routineDayId);
        WorkoutSession saved = workoutSessionRepository.save(session);

        return new StartedSessionResult(saved.getId(), saved.getRoutineDayId(), saved.getStartTime());
    }
}
