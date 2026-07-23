package com.powertrack.backend.application.workout.port.in;

import java.time.Instant;
import java.util.UUID;

public interface StartWorkoutSessionUseCase {

    StartedSessionResult start(StartSessionCommand command);

    /**
     * {@code sessionId} y {@code startTime} los reporta el cliente (no el servidor): ver
     * {@link com.powertrack.backend.domain.workout.WorkoutSession#start}.
     */
    record StartSessionCommand(UUID sessionId, UUID userId, UUID routineDayId, Instant startTime) {
    }

    record StartedSessionResult(UUID sessionId, UUID routineDayId, Instant startTime) {
    }
}
