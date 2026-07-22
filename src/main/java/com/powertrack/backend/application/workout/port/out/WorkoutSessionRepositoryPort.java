package com.powertrack.backend.application.workout.port.out;

import com.powertrack.backend.domain.workout.WorkoutSession;

import java.util.Optional;
import java.util.UUID;

public interface WorkoutSessionRepositoryPort {

    WorkoutSession save(WorkoutSession session);

    /**
     * Filtra por dueño a nivel de query: si la sesión existe pero es de otro usuario, no
     * se encuentra y se trata igual que "no existe" (404), mismo criterio que
     * {@code RoutineRepositoryPort#findByIdAndUserId}.
     */
    Optional<WorkoutSession> findByIdAndUserId(UUID id, UUID userId);
}
