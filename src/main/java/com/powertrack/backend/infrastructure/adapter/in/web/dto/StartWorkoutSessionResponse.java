package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.workout.port.in.StartWorkoutSessionUseCase.StartedSessionResult;

import java.time.Instant;
import java.util.UUID;

public record StartWorkoutSessionResponse(UUID sessionId, UUID routineDayId, Instant startTime) {

    public static StartWorkoutSessionResponse from(StartedSessionResult result) {
        return new StartWorkoutSessionResponse(result.sessionId(), result.routineDayId(), result.startTime());
    }
}
