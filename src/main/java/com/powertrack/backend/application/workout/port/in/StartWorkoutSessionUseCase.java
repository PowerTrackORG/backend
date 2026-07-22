package com.powertrack.backend.application.workout.port.in;

import java.time.Instant;
import java.util.UUID;

public interface StartWorkoutSessionUseCase {

    StartedSessionResult start(UUID userId, UUID routineDayId);

    record StartedSessionResult(UUID sessionId, UUID routineDayId, Instant startTime) {
    }
}
